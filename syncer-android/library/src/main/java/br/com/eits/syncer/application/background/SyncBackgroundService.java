package br.com.eits.syncer.application.background;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.util.Log;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.eits.syncer.application.ApplicationHolder;
import br.com.eits.syncer.domain.entity.Revision;
import br.com.eits.syncer.infrastructure.dao.ORMOpenHelper;
import br.com.eits.syncer.infrastructure.dao.RevisionDao;
import br.com.eits.syncer.domain.entity.EntityUpdatedId;
import br.com.eits.syncer.domain.entity.RevisionType;
import br.com.eits.syncer.infrastructure.delegate.Syncer;

/**
 * Define a Service that returns an IBinder for the sync adapter class,
 * allowing the sync adapter framework to call onPerformSync().
 */
public class SyncBackgroundService extends JobService
{
    /**
     *
     */
    private final RevisionDao revisionDao;
    /**
     *
     */
    private final ORMOpenHelper helper = new ORMOpenHelper( ApplicationHolder.CONTEXT );

    /**
     *
     */
    public SyncBackgroundService()
    {
        this.revisionDao = new RevisionDao( this.helper.getRuntimeExceptionDao(Revision.class) );
    }

    /**
     * Override this method with the callback logic for your job. Any such logic needs to be
     * performed on a separate thread, as this function is executed on your application's main
     * thread.
     *
     * @param params Parameters specifying info about this job, including the extras bundle you
     *               optionally provided at job-creation time.
     * @return True if your service needs to process the work (on a separate thread). False if
     * there's no more work to be done for this job.
     */
    @Override
    public boolean onStartJob(JobParameters params)
    {
        Log.wtf( SyncBackgroundService.class.getSimpleName(), "onStartJob -> "+ params );

        //Note: this is preformed on the main thread.
        new UpdateAppsAsyncTask().execute(params);
        return true;
    }

    /**
     * This method is called if the system has determined that you must stop execution of your job
     * even before you've had a chance to call {@link #jobFinished(JobParameters, boolean)}.
     *
     * <p>This will happen if the requirements specified at schedule time are no longer met. For
     * example you may have requested WiFi with
     * {@link android.app.job.JobInfo.Builder#setRequiredNetworkType(int)}, yet while your
     * job was executing the user toggled WiFi. Another example is if you had specified
     * {@link android.app.job.JobInfo.Builder#setRequiresDeviceIdle(boolean)}, and the phone left its
     * idle maintenance window. You are solely responsible for the behaviour of your application
     * upon receipt of this message; your app will likely start to misbehave if you ignore it. One
     * immediate repercussion is that the system will cease holding a wakelock for you.</p>
     *
     * @param params Parameters specifying info about this job.
     * @return True to indicate to the JobManager whether you'd like to reschedule this job based
     * on the retry criteria provided at job creation-time. False to drop the job. Regardless of
     * the value returned, your job must stop executing.
     */
    @Override
    public boolean onStopJob(JobParameters params)
    {
        Log.wtf( SyncBackgroundService.class.getSimpleName(), "onStopJob -> "+ params );

        //boolean shouldReschedule = updateTask.cancel(true);
        //return shouldReschedule;

        return false;
    }

    /**
     *
     */
    private class UpdateAppsAsyncTask extends AsyncTask<JobParameters, Void, JobParameters[]>
    {
        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         *
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         *
         * @return A result, defined by the subclass of this task.
         *
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected JobParameters[] doInBackground( JobParameters... params )
        {
            Log.wtf( UpdateAppsAsyncTask.class.getSimpleName(), "doInBackground -> "+ params );

            //FIXME tratar problema de conexao, serializacao, sync das entities remote

            //-VERIFICAR A DEMORA
            //    -VERIFICAR SE OS AGENDAMENTOS SAO EM ORDEM
            //    -VERIFICAR A TRHEAD DE AGENDAMENTO
            //    -VERIFICAR QUANDO REMOVE / ALTERA
            final List<Revision> revisions = SyncBackgroundService.this.revisionDao.queryForEq("synced", false);

            //request witch entities we have to sync
            final Map<RevisionType, List<Object>> localEntities = this.listEntitiesByRevisionType( revisions );

            //sync these remotely
            final Map<RevisionType, List<Object>> remoteEntities = Syncer.instance().syncronize( localEntities );

            //save revisions synced
            for ( Revision revision : revisions )
            {
                revision.setSynced(true);
                SyncBackgroundService.this.revisionDao.update(revision);
            }

            //now we must sync the remote entities
            this.syncRemoteEntities( remoteEntities );

            return params;
        }

        /**
         *
         * @param revisions
         * @return
         */
        private Map<RevisionType, List<Object>> listEntitiesByRevisionType( List<Revision> revisions )
        {
            final Map<RevisionType, List<Object>> entitiesByRevisionType = new HashMap<>();
            entitiesByRevisionType.put( RevisionType.INSERT, new ArrayList<Object>());
            entitiesByRevisionType.put( RevisionType.UPDATE, new ArrayList<Object>());
            entitiesByRevisionType.put( RevisionType.REMOVE, new ArrayList<Object>());

            for ( Revision revision : revisions )
            {
                final RuntimeExceptionDao dao = this.createDao( revision.getEntityClassName() );

                if ( revision.getType().equals(RevisionType.REMOVE) )
                {
                    try
                    {
                        final Map<String, Object> entity = new HashMap<>();
                        entity.put("id", revision.getEntityId());
                        entity.put("entityClassName", revision.getEntityClassName() );
                        entitiesByRevisionType.get( revision.getType() ).add( entity );
                    }
                    catch( Exception e )
                    {
                        throw new RuntimeException(e);
                    }
                }
                else
                {
                    final Object entity = dao.queryForId( revision.getEntityId() );
                    entitiesByRevisionType.get( revision.getType() ).add(entity);
                }
            }

            return entitiesByRevisionType;
        }

        /**
         *
         * @param entitiesByRevisionType
         */
        private void syncRemoteEntities( Map<RevisionType, List<Object>> entitiesByRevisionType )
        {
            final List<Object> entitiesToAdd = entitiesByRevisionType.get( RevisionType.INSERT );
            for ( Object entity : entitiesToAdd )
            {
                final RuntimeExceptionDao dao = this.createDao( entity.getClass().getName() );
                dao.create(entity);
            }

            final List<Object> entitiesToUpdateId = entitiesByRevisionType.get( RevisionType.UPDATE_ID );
            for ( Object entity : entitiesToUpdateId )
            {
                final EntityUpdatedId entityUpdatedId = (EntityUpdatedId) entity;
                final RuntimeExceptionDao dao = this.createDao( entityUpdatedId.getEntity().getClass().getName() );
                dao.updateId( entityUpdatedId.getEntity(), entityUpdatedId.getNewId() );
            }

            final List<Object> entitiesToUpdate = entitiesByRevisionType.get( RevisionType.UPDATE );
            for ( Object entity : entitiesToUpdate )
            {
                final RuntimeExceptionDao dao = this.createDao( entity.getClass().getName() );
                dao.update(entity);
            }

            final List<Object> entitiesToRemove = entitiesByRevisionType.get( RevisionType.REMOVE );
            for ( Object entity : entitiesToRemove )
            {
                final RuntimeExceptionDao dao = this.createDao( entity.getClass().getName() );
                dao.delete(entity);
            }
        }

        /**
         *
         * @param entityClassName
         * @return
         */
        private RuntimeExceptionDao createDao( String entityClassName )
        {
            try
            {
                return SyncBackgroundService.this.helper.getRuntimeExceptionDao( Class.forName(entityClassName) );
            }
            catch (ClassNotFoundException e)
            {
                throw new IllegalStateException(e);
            }
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         *
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param result The result of the operation computed by {@link #doInBackground}.
         *
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute( JobParameters[] result )
        {
            Log.wtf( UpdateAppsAsyncTask.class.getSimpleName(), "onPostExecute -> "+ result );

            for ( JobParameters params : result )
            {
                SyncBackgroundService.this.jobFinished(params, false);
            }
        }
    }
}
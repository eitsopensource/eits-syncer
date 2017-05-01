package br.com.eits.syncer.application.background;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import br.com.eits.syncer.Syncer;
import br.com.eits.syncer.application.restful.ISyncResource;
import br.com.eits.syncer.domain.entity.Revision;
import br.com.eits.syncer.domain.entity.SyncData;
import br.com.eits.syncer.domain.service.Watcher;
import br.com.eits.syncer.infrastructure.dao.RevisionDao;

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
    private final ISyncResource syncResource;

    /**
     *
     */
    public SyncBackgroundService()
    {
        this.revisionDao = new RevisionDao();
        this.syncResource = Syncer.getSyncResource();
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

            try
            {
                final List<Revision<?>> revisions = SyncBackgroundService.this.revisionDao.listByUnsynced();

                //sync these remotely
                final Revision lastSyncedRevision = SyncBackgroundService.this.revisionDao.findByLastRevisionNumber();
                final long lastRevisionNumber = lastSyncedRevision != null ? (lastSyncedRevision.getRevisionNumber() + 1L) : 1L;
                final SyncData localSyncData = new SyncData( lastRevisionNumber, revisions );
                final SyncData remoteSyncData = SyncBackgroundService.this.syncResource.syncronize( localSyncData );

                //remove the local unsynced revisions
                final String[] revisionIds = new String[localSyncData.getRevisions().size()];
                for ( int i = 0; i <localSyncData.getRevisions().size(); i++ )
                {
                    final Revision<?> revision = localSyncData.getRevisions().get(i);
                    revisionIds[i] = String.valueOf( revision.getId() );
                }
                SyncBackgroundService.this.revisionDao.remove( revisionIds );

                //save remote revisions as synced
                for ( Revision<?> revision : remoteSyncData.getRevisions() )
                {
                    Revision<?> newRevision = new Revision( revision.getEntity(), revision.getType() );
                    newRevision.setRevisionNumber( revision.getRevisionNumber() );
                    newRevision.setSynced(true);
                    SyncBackgroundService.this.revisionDao.insertRevision( newRevision );
                }

                Watcher.notifyObservers();
                return params;
            }
            catch( Exception e )
            {
                e.printStackTrace();
                throw new RuntimeException(e);
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
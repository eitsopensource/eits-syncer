package br.com.eits.syncer.application.background;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.util.Log;
import br.com.eits.syncer.Syncer;
import br.com.eits.syncer.application.restful.ISyncResource;
import br.com.eits.syncer.domain.entity.Revision;
import br.com.eits.syncer.domain.entity.SyncData;
import br.com.eits.syncer.domain.entity.SyncResourceConfiguration;
import br.com.eits.syncer.domain.service.Watcher;
import br.com.eits.syncer.infrastructure.dao.RevisionDao;

import java.util.List;
import java.util.Observable;

/**
 * Define a ServiceHost that returns an IBinder for the sync adapter class,
 * allowing the sync adapter framework to call onPerformSync().
 */
public class SyncBackgroundService extends JobService
{
    /*-------------------------------------------------------------------
	 * 		 					ATTRIBUTES
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    private final RevisionDao revisionDao;
    /**
     *
     */
    private ISyncResource syncResource;
    /**
     *
     */
    private String serviceName;
    /**
     *
     */
    private static SyncTask SYNC_TASK;

    private static Observable observable;

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    public SyncBackgroundService()
    {
        this.revisionDao = new RevisionDao();
    }

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
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
    public boolean onStartJob( JobParameters params )
    {
        Log.d( SyncTask.class.getSimpleName(), "Request to START sync." );

        //create the syncResource
        this.serviceName = params.getExtras().getString( SyncResourceConfiguration.SERVICE_NAME_KEY );
        this.syncResource = Syncer.syncResourceConfiguration().getSyncResource( this.serviceName );

        //if is the first time or the running task has been finished
        if ( SYNC_TASK == null || SYNC_TASK.getStatus() == AsyncTask.Status.FINISHED )
        {
            Log.d( SyncTask.class.getSimpleName(), "Starting task to sync..." );
            SYNC_TASK = new SyncTask(this);
            SYNC_TASK.execute(params);
        }

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
    public boolean onStopJob( JobParameters params )
    {
        Log.d( SyncTask.class.getSimpleName(), "Request to STOP sync." );
        return false;//we always ignore this job
    }

    public void setObservable( Observable observable )
    {
        this.observable = observable;
    }

    /*-------------------------------------------------------------------
	 * 		 					INNER CLASSES
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    private class SyncTask extends AsyncTask<JobParameters, Void, JobParameters>
    {
        /**
         *
         */
        public static final String NEEDS_RESCHEDULE = "reschedule";

        /*-------------------------------------------------------------------
         * 		 					CONSTRUCTORS
         *-------------------------------------------------------------------*/
        /**
         *
         */
        private SyncBackgroundService syncBackgroundService;

        /*-------------------------------------------------------------------
         * 		 					CONSTRUCTORS
         *-------------------------------------------------------------------*/
        /**
         *
         * @param syncBackgroundService
         */
        public SyncTask(SyncBackgroundService syncBackgroundService )
        {
            this.syncBackgroundService = syncBackgroundService;
        }


        /*-------------------------------------------------------------------
         * 		 					BEHAVIORS
         *-------------------------------------------------------------------*/
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
        protected JobParameters doInBackground( JobParameters... params )
        {
            Log.d( SyncTask.class.getSimpleName(), "Running doInBackground" );

            final JobParameters jobParameters = params[0];

            try
            {
                final List<Revision<?>> revisions = revisionDao.listByUnsyncedByService( serviceName, 10000 );

                Log.i( SyncTask.class.getSimpleName(), revisions.size()+" revisions to sync." );

                //sync these remotely
                final Revision lastSyncedRevision = revisionDao.findByLastRevisionNumber( serviceName );
                final long lastRevisionNumber = lastSyncedRevision != null ? (lastSyncedRevision.getRevisionNumber() + 1L) : 1L;
                final SyncData localSyncData = new SyncData( lastRevisionNumber, revisions );

                Log.d( SyncTask.class.getSimpleName(), "Requesting to server to sync..." );

                final SyncData remoteSyncData = syncResource.syncronize( localSyncData );

                if(remoteSyncData != null && remoteSyncData.getRevisions() != null)
                {
                    Log.i( SyncTask.class.getSimpleName(), "Server returned "+remoteSyncData.getRevisions().size()+" revisions to sync." );
                    //remove the local unsynced revisions
                    final String[] revisionIds = new String[localSyncData.getRevisions().size()];
                    for ( int i = 0; i <localSyncData.getRevisions().size(); i++ )
                    {
                        final Revision<?> revision = localSyncData.getRevisions().get(i);
                        revisionIds[i] = String.valueOf( revision.getId() );
                    }
                    //remove unused revisions
                    revisionDao.remove( revisionIds );

                    //save remote revisions as synced
                    for ( Revision<?> revision : remoteSyncData.getRevisions() )
                    {
                        final Revision<?> newRevision = new Revision( revision.getEntity(), revision.getType(), revision.getServiceName() );
                        newRevision.setRevisionNumber( revision.getRevisionNumber() );
                        newRevision.setSynced( true );
                        revisionDao.insertRevision( newRevision );
                        revisionDao.removeOldRevisions( newRevision );
                    }

                    Log.i( SyncTask.class.getSimpleName(), "Sync finished." );

                    jobParameters.getExtras().putBoolean(NEEDS_RESCHEDULE, false);
                }
            }
            catch( Exception e )
            {
                Log.e( SyncTask.class.getSimpleName(), "Error syncing.", e );
                //ignore the reschedule and delegate to the next sync request
                jobParameters.getExtras().putBoolean(NEEDS_RESCHEDULE, false);
            }

            return jobParameters;
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         *
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param jobParameters The result of the operation computed by {@link #doInBackground}.
         *
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute( JobParameters jobParameters )
        {
            final boolean needsReschedule = jobParameters.getExtras().getBoolean(SyncTask.NEEDS_RESCHEDULE);

            //if the job exected sucessfully
            if ( !needsReschedule )
            {
                Watcher.notifyObservers();
            }
            jobFinished( jobParameters, needsReschedule );
        }
    }
}
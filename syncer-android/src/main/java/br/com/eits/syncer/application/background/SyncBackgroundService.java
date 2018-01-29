package br.com.eits.syncer.application.background;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import br.com.eits.syncer.domain.service.sync.SyncOnDemandService;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Define a ServiceHost that returns an IBinder for the sync adapter class,
 * allowing the sync adapter framework to call onPerformSync().
 */
public class SyncBackgroundService extends JobService
{
	/*-------------------------------------------------------------------
	 * 		 					ATTRIBUTES
	 *-------------------------------------------------------------------*/

	private static final String TAG = "[sync-bg]";

	private final Map<Integer, Disposable> syncSubscriptions = new HashMap<>();

	private JobParameters currentParameters;

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/

	/**
	 *
	 */
	public SyncBackgroundService()
	{

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
	public boolean onStartJob( final JobParameters params )
	{
		Log.i( TAG, "scheduled sync running" );

		Disposable disposable = SyncOnDemandService.syncAllNow()
				.subscribe( new Consumer<String>()
							{
								@Override
								public void accept( String serviceName ) throws Exception
								{
									Log.i( TAG, "scheduled sync done for " + serviceName );
								}
							},
						new Consumer<Throwable>()
						{
							@Override
							public void accept( Throwable throwable ) throws Exception
							{
								Log.i( TAG, "sync failed for " + throwable.getMessage() );
								jobFinished( params, true );
							}
						},
						new Action()
						{
							@Override
							public void run() throws Exception
							{
								syncSubscriptions.remove( params.getJobId() );
								Log.d( TAG, "finished scheduled sync" );
								jobFinished( params, false );
							}
						} );
		syncSubscriptions.put( params.getJobId(), disposable );
		return true;
	}

	/**
	 * This method is called if the system has determined that you must stop execution of your job
	 * even before you've had a chance to call {@link #jobFinished(JobParameters, boolean)}.
	 * <p>
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
		Log.d( TAG, "abort sync" );

		syncSubscriptions.get( params.getJobId() ).dispose();

		return true;//we always ignore this job
	}
}
package br.com.eits.syncer;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.PersistableBundle;
import android.util.Log;
import br.com.eits.syncer.application.ApplicationHolder;
import br.com.eits.syncer.application.background.SyncBackgroundService;
import br.com.eits.syncer.domain.entity.SyncResourceConfiguration;
import br.com.eits.syncer.domain.service.IRevisionService;
import br.com.eits.syncer.domain.service.RevisionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Contract;
import feign.RequestInterceptor;

import java.util.Objects;
import java.util.Set;

/**
 *
 * @author rodrigo.p.fraga
 */
public class Syncer
{
	/**
	 *
	 */
	private static final int SYNC_JOB_ID = Integer.MAX_VALUE;
	/**
	 *
	 */
	private static JobScheduler JOB_SCHEDULER;
	/**
	 *
	 */
	private static ComponentName SYNC_BACKGROUND_SERVICE_COMPONENT;
	/**
	 *
	 */
	private static final SyncResourceConfiguration RESOURCE_CONFIGURATION = new SyncResourceConfiguration();

	/**
	 *
	 */
	static
	{
		try
		{
			Syncer.SYNC_BACKGROUND_SERVICE_COMPONENT = new ComponentName(ApplicationHolder.CONTEXT, SyncBackgroundService.class);
			final ServiceInfo serviceInfo = ApplicationHolder.CONTEXT.getPackageManager().getServiceInfo(Syncer.SYNC_BACKGROUND_SERVICE_COMPONENT, PackageManager.GET_META_DATA);

			//Get the mandatory URLs
			final String urls = serviceInfo.metaData.getString("sync-urls");
			Syncer.RESOURCE_CONFIGURATION.setSyncURLs(urls);

			//Get the optional basic credentials
			final String credentials = serviceInfo.metaData.getString("sync-basic-credentials");
			if ( credentials != null )
			{
				Syncer.RESOURCE_CONFIGURATION.setBasicCredentials( credentials );
			}

			//get the job scheduler
			Syncer.JOB_SCHEDULER = ( JobScheduler ) ApplicationHolder.CONTEXT.getSystemService( Context.JOB_SCHEDULER_SERVICE );
		}
		catch (Exception e)
		{
			throw new IllegalStateException("To use Syncer, you must setup the SyncBackgroudService at least as follow: " +
					"    <service android:process=\":sync\"\n" +
					"             android:permission=\"android.permission.BIND_JOB_SERVICE\"\n" +
					"             android:name=\"br.com.eits.syncer.application.background.SyncBackgroundService\">\n" +
					"\n" +
					"        //optional: sync service with basic credentials\n" +
					"        <meta-data android:name=\"sync-basic-credentials\"\n" +
					"                   android:value=\"admin:pass\"/>\n" +
					"\n" +
					"        //mandatory: sync service urls\n" +
					"        <meta-data android:name=\"sync-urls\"\n" +
					"                   android:value='{\"default\":\"http://host1.com\", \"service2\":\"http://host2.com\"}'/>\n" +
					"    </service>");
		}
	}

	//-----------
	// Configurations
	//-----------

	/**
	 *
	 * @param requestInterceptor
	 */
	public static void withRequestInterceptor( RequestInterceptor requestInterceptor )
	{
		Syncer.RESOURCE_CONFIGURATION.setRequestInterceptor( requestInterceptor );
	}

	/**
	 *
	 * @param contract
	 */
	public static void withContract( Contract contract )
	{
		Syncer.RESOURCE_CONFIGURATION.setContract(contract);
	}

	/**
	 *
	 * @param objectMapper
	 * @return
	 */
	public static void withMapper( ObjectMapper objectMapper )
	{
		Objects.requireNonNull( objectMapper, "The mapper must be not null." );
		Syncer.RESOURCE_CONFIGURATION.setObjectMapper( objectMapper );
	}

    /*-------------------------------------------------------------------
	 * 		 						BEHAVIORS
	 *-------------------------------------------------------------------*/
	/**
	 *
	 * @param entityClass
	 * @return
	 */
	public static <T> IRevisionService<T> of(Class<T> entityClass )
	{
		return new RevisionService<>( entityClass );
	}

	/**
	 * Schedule a sync for each service configurated.
	 */
	public static void requestSync()
	{
		final Set<String> serviceNames = Syncer.syncResourceConfiguration().getServiceNames();

		for ( String serviceName : serviceNames )
		{
			final PersistableBundle extras = new PersistableBundle();
			extras.putString(SyncResourceConfiguration.SERVICE_NAME_KEY, serviceName);
			Syncer.requestSync( extras );
		}
	}

	/**
	 *
	 */
	public static void requestSync( PersistableBundle extras )
	{
		if ( extras == null || extras.isEmpty() || !extras.containsKey(SyncResourceConfiguration.SERVICE_NAME_KEY) )
		{
			throw new IllegalArgumentException("The extras param must at least have the serviceName param.");
		}

		//we create an unique job Id using the service name value
		final int jobId = Syncer.SYNC_JOB_ID + extras.getString(SyncResourceConfiguration.SERVICE_NAME_KEY).hashCode();

		final JobInfo jobInfo = new JobInfo.Builder( jobId, Syncer.SYNC_BACKGROUND_SERVICE_COMPONENT )
				.setRequiredNetworkType( JobInfo.NETWORK_TYPE_ANY )
				.setRequiresDeviceIdle( false )
				.setRequiresCharging( false )
				.setExtras(extras)
				.setPersisted( true )
				.build();

		final Integer result = Syncer.JOB_SCHEDULER.schedule( jobInfo );

		if ( result != JobScheduler.RESULT_SUCCESS )
		{
			throw new IllegalArgumentException( "Error doing the local operation. Was not possible to schedule for sync." );
		}
		else
		{
			Log.d( Syncer.class.getSimpleName(), "Job scheduled successfully for revision" );
		}
	}

	/**
	 *
	 */
	public static void cancelSync()
	{
		Syncer.JOB_SCHEDULER.cancelAll();
	}

	/*-------------------------------------------------------------------
	 * 		 				  GETTERS AND SETTERS
	 *-------------------------------------------------------------------*/
	/**
	 *
	 */
	public static ObjectMapper getMapper()
	{
		return Syncer.RESOURCE_CONFIGURATION.getObjectMapper();
	}

	/**
	 *
	 */
	public static SyncResourceConfiguration syncResourceConfiguration()
	{
		return Syncer.RESOURCE_CONFIGURATION;
	}
}

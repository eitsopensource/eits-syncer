package br.com.eits.syncer;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

import br.com.eits.syncer.application.ApplicationHolder;
import br.com.eits.syncer.application.background.SyncBackgroundService;
import br.com.eits.syncer.domain.entity.SyncEntity;
import br.com.eits.syncer.domain.entity.SyncResourceConfiguration;
import br.com.eits.syncer.domain.service.IRevisionService;
import br.com.eits.syncer.domain.service.RevisionService;
import br.com.eits.syncer.domain.service.sync.SyncOnDemandService;
import io.reactivex.Observable;
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;

/**
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
			Syncer.SYNC_BACKGROUND_SERVICE_COMPONENT = new ComponentName( ApplicationHolder.CONTEXT, SyncBackgroundService.class );
			final ServiceInfo serviceInfo = ApplicationHolder.CONTEXT.getPackageManager().getServiceInfo( Syncer.SYNC_BACKGROUND_SERVICE_COMPONENT, PackageManager.GET_META_DATA );

			//Get the mandatory URLs
			final String urls = serviceInfo.metaData.getString( "sync-urls" );
			Syncer.RESOURCE_CONFIGURATION.setSyncURLs( urls );

			String serviceOrder = serviceInfo.metaData.getString( "services-sorted" );
			Syncer.RESOURCE_CONFIGURATION.setSyncOrder( serviceOrder );

			Syncer.RESOURCE_CONFIGURATION.setPostReceiveHooks( serviceInfo.metaData.getString( "post-receive-hooks" ) );

			//Get the optional basic credentials
			final String credentials = serviceInfo.metaData.getString( "sync-shared-preferences-basic" );
			if ( credentials != null )
			{
				String[] str = credentials.split( "\\." );
				Syncer.RESOURCE_CONFIGURATION.setBasicCredentials( str[0], str[1] );
			}

			final String bearerToken = serviceInfo.metaData.getString( "sync-shared-preferences-token" );
			if ( bearerToken != null )
			{
				String[] str = bearerToken.split( "\\." );
				Syncer.RESOURCE_CONFIGURATION.setBearerToken( str[0], str[1] );
			}

			final String interceptorClassName = serviceInfo.metaData.getString( "sync-interceptor-class-name" );
			if ( interceptorClassName != null )
			{
				try
				{
					Class clazz = Class.forName( interceptorClassName );
					if ( clazz != null )
					{
						Interceptor instance = (Interceptor) clazz.newInstance();
						Syncer.RESOURCE_CONFIGURATION.setRequestInterceptor( instance );
					}
				}
				catch ( Exception e )
				{
					throw new IllegalArgumentException( "Não foi possível localizar ou instanciar o interceptor " + interceptorClassName, e );
				}
			}

			final String networkInterceptorClassName = serviceInfo.metaData.getString( "sync-network-interceptor-class-name" );
			if ( networkInterceptorClassName != null )
			{
				try
				{
					Class clazz = Class.forName( networkInterceptorClassName );
					if ( clazz != null )
					{
						Interceptor instance = (Interceptor) clazz.newInstance();
						Syncer.RESOURCE_CONFIGURATION.setNetworkInterceptor( instance );
					}
				}
				catch ( Exception e )
				{
					throw new IllegalArgumentException( "Não foi possível localizar ou instanciar o networkInterceptor " + networkInterceptorClassName, e );
				}
			}

			//get the job scheduler
			Syncer.JOB_SCHEDULER = (JobScheduler) ApplicationHolder.CONTEXT.getSystemService( Context.JOB_SCHEDULER_SERVICE );
		}
		catch ( Exception e )
		{
			throw new IllegalStateException( "To use Syncer, you must setup the SyncBackgroudService at least as follow: " +
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
					"    </service>" );
		}
	}

	//-----------
	// Configurations
	//-----------

	/**
	 * @param requestInterceptor
	 */
	public static void withRequestInterceptor( Interceptor requestInterceptor )
	{
		Syncer.RESOURCE_CONFIGURATION.setRequestInterceptor( requestInterceptor );
	}

	/**
	 * @param objectMapper
	 * @return
	 */
	public static void withMapper( ObjectMapper objectMapper )
	{
		Objects.requireNonNull( objectMapper, "The mapper must be not null." );
		Syncer.RESOURCE_CONFIGURATION.setObjectMapper( objectMapper );
	}

	/**
	 * @param logLevel
	 * @return
	 */
	public static void withLogLevel( HttpLoggingInterceptor.Level logLevel )
	{
		Syncer.RESOURCE_CONFIGURATION.setLogLevel( logLevel );
	}

    /*-------------------------------------------------------------------
	 * 		 						BEHAVIORS
	 *-------------------------------------------------------------------*/

	/**
	 * @param entityClass
	 * @return
	 */
	public static <T extends SyncEntity> IRevisionService<T> of( Class<T> entityClass )
	{
		return new RevisionService<>( entityClass );
	}

	/**
	 * Schedule a sync for each service configurated.
	 */
	@SuppressLint("MissingPermission")
	public static void scheduleRecurrentSync()
	{
		Syncer.JOB_SCHEDULER.cancelAll();
		final int jobId = "eits//syncer".hashCode();
		final JobInfo jobInfo = new JobInfo.Builder( jobId, Syncer.SYNC_BACKGROUND_SERVICE_COMPONENT )
				.setRequiredNetworkType( JobInfo.NETWORK_TYPE_ANY )
				.setPeriodic( 15 * 60 * 1000 )
				.setRequiresDeviceIdle( false )
				.setRequiresCharging( false )
				.setPersisted( false )
				.build();
		if ( Syncer.JOB_SCHEDULER.schedule( jobInfo ) != JobScheduler.RESULT_SUCCESS )
		{
			Log.w( "[sync-bg]", "Could not schedule background sync" );
		}
		else
		{
			Log.i( "[sync-bg]", "Background sync scheduled." );
		}
	}

	/**
	 *
	 */
	public static void cancelScheduledSync()
	{
		Syncer.JOB_SCHEDULER.cancelAll();
	}

	/**
	 * Blocks until syncs are all finished and does not allow syncs to occur until reenabled.
	 */
	public static void disableSync()
	{
		cancelScheduledSync();
		SyncOnDemandService.disable();
	}

	public static void enableSync()
	{
		SyncOnDemandService.enable();
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

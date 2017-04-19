package br.com.eits.syncer;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Objects;

import br.com.eits.syncer.application.ApplicationHolder;
import br.com.eits.syncer.application.background.SyncBackgroundService;
import br.com.eits.syncer.application.restful.ISyncResource;
import br.com.eits.syncer.domain.service.IRevisionService;
import br.com.eits.syncer.domain.service.RevisionService;
import feign.Contract;
import feign.Feign;
import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxrs.JAXRSContract;

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
	private static RequestInterceptor REQUEST_INTERCEPTOR;
	/**
	 *
	 */
	private static ObjectMapper MAPPER = new ObjectMapper();
	/**
	 *
	 */
	private static Contract CONTRACT = new JAXRSContract();
	/**
	 *
	 */
	private static String URL;
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
	static
	{
		try
		{
			Syncer.SYNC_BACKGROUND_SERVICE_COMPONENT = new ComponentName(ApplicationHolder.CONTEXT, SyncBackgroundService.class);
			final ServiceInfo serviceInfo = ApplicationHolder.CONTEXT.getPackageManager().getServiceInfo(Syncer.SYNC_BACKGROUND_SERVICE_COMPONENT, PackageManager.GET_META_DATA);

			//Get the mandatory URL
			final String url = serviceInfo.metaData.getString("sync-url");
			Objects.requireNonNull( url, "The URL must be not null." );
			Syncer.URL = url;

			//Get the optional basic credentials
			final String credentials = serviceInfo.metaData.getString("sync-basic-credentials");
			if ( credentials != null && !credentials.isEmpty() && credentials.contains(":") )
			{
				final String username = credentials.split(":")[0];
				final String password = credentials.split(":")[1];

				Syncer.REQUEST_INTERCEPTOR = new BasicAuthRequestInterceptor( username, password );
			}

			//get the job scheduler
			Syncer.JOB_SCHEDULER = ( JobScheduler ) ApplicationHolder.CONTEXT.getSystemService( Context.JOB_SCHEDULER_SERVICE );
		}
		catch (PackageManager.NameNotFoundException e)
		{
			throw new IllegalStateException("To use Syncer, you must setup the SyncBackgroudService as follow: \n\n" +
					"        <service\n" +
					"            android:permission=\"android.permission.BIND_JOB_SERVICE\"\n" +
					"            android:name=\"br.com.eits.syncer.application.background.SyncBackgroundService\">\n" +
					"            <meta-data\n" +
					"                android:name=\"sync-url\" android:value=\"http://someUrl:8080\"/>\n" +
					"        </service>");
		}

		//configure the default objectMapper
		Syncer.MAPPER.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
		Syncer.MAPPER.configure( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false );
		Syncer.MAPPER.enableDefaultTypingAsProperty( ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "@type" );
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
		Syncer.REQUEST_INTERCEPTOR = requestInterceptor;
	}

	/**
	 *
	 * @param contract
	 */
	public static void withContract( Contract contract )
	{
		Objects.requireNonNull( contract, "The feign contract must be not null." );
		Syncer.CONTRACT = contract;
	}

	/**
	 *
	 * @param objectMapper
	 * @return
	 */
	public static void withMapper( ObjectMapper objectMapper )
	{
		Objects.requireNonNull( objectMapper, "The mapper must be not null." );
		Syncer.MAPPER = objectMapper;
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
	 *
	 */
	public static void requestSyncNow()
	{
		Log.w(Syncer.class.getName(), "The request sync now is not ready. Scheduling...");
		Syncer.requestSync();
	}

	/**
	 *
	 */
	public static void requestSync()
	{
		final JobInfo jobInfo = new JobInfo.Builder( SYNC_JOB_ID, SYNC_BACKGROUND_SERVICE_COMPONENT )
				.setRequiredNetworkType( JobInfo.NETWORK_TYPE_ANY )
				.setRequiresDeviceIdle( false )
				.setRequiresCharging( false )
				.setPersisted( true )
				.build();

		final Integer result = JOB_SCHEDULER.schedule( jobInfo );

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
		Syncer.JOB_SCHEDULER.cancel( SYNC_JOB_ID );
	}

	/**
	 *
	 * @return
	 */
	public static ISyncResource getSyncResource()
	{
		final Feign.Builder builder = Feign.builder()
				.contract( Syncer.CONTRACT )
				.encoder( new JacksonEncoder( getMapper() ) )
				.decoder( new JacksonDecoder( getMapper() ) );

		if ( REQUEST_INTERCEPTOR != null )
		{
			builder.requestInterceptor( REQUEST_INTERCEPTOR );
		}

		return builder.target( ISyncResource.class, URL );
	}

	/**
	 *
	 */
	public static ObjectMapper getMapper()
	{
		return Syncer.MAPPER;
	}
}

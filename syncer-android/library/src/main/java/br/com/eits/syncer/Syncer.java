package br.com.eits.syncer;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeId;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Objects;

import br.com.eits.syncer.application.ApplicationHolder;
import br.com.eits.syncer.application.background.SyncBackgroundService;
import br.com.eits.syncer.application.restful.ISyncResource;
import br.com.eits.syncer.domain.entity.SyncData;
import br.com.eits.syncer.domain.service.RepositoryService;
import feign.Feign;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.EncodeException;
import feign.codec.Encoder;
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
	private static final int SYNC_NOW_JOB_ID = -9999;

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
	private static String URL;

	/**
	 *
	 */
	private static JobScheduler jobScheduler;
	
	/**
	 * 
	 * @param url
	 * @return
	 */
	public static void withURL( String url )
	{
		Objects.requireNonNull( url, "The URL must be not null." );
		
		URL = url;
	}
	
	/**
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public static void withCredentials( String username, String password )
	{
		REQUEST_INTERCEPTOR = new BasicAuthRequestInterceptor( username, password );
	}

	/**
	 * 
	 * @param objectMapper
	 * @return
	 */
	public static void withMapper( ObjectMapper objectMapper )
	{
		Objects.requireNonNull( objectMapper, "The mapper must be not null." );
		
		MAPPER = objectMapper;
	}
	
	/**
	 * 
	 */
	public static void clearCredentials()
	{
		REQUEST_INTERCEPTOR = null;
	}

	/**
	 *
	 */
	public static ObjectMapper getMapper()
	{
		MAPPER.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
		MAPPER.configure( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false );
		MAPPER.configure( SerializationFeature.INDENT_OUTPUT, true );

		MAPPER.setSerializationInclusion( JsonInclude.Include.NON_NULL );

		MAPPER.enableDefaultTyping();
		MAPPER.enableDefaultTyping( ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT );

		return MAPPER;
	}

	/**
	 *
	 * @param entityClass
	 * @return
	 */
	public static <T, ID extends Serializable> RepositoryService<T> forEntity( Class<T> entityClass )
	{
//		return new RepositoryService<T, ID>( entityClass );
		return new RepositoryService<T>( entityClass );
	}

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
	/**
	 *
	 */
	public static void requestSyncNow()
	{
		Objects.requireNonNull( URL, "You must configure the URL to sync." );

		if( jobScheduler == null )
			jobScheduler = (JobScheduler) ApplicationHolder.CONTEXT.getSystemService(Context.JOB_SCHEDULER_SERVICE);

		final ComponentName serviceName = new ComponentName(ApplicationHolder.CONTEXT, SyncBackgroundService.class);
		final JobInfo jobInfo = new JobInfo.Builder(SYNC_NOW_JOB_ID, serviceName)
				.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
				.setRequiresDeviceIdle(false)
				.setRequiresCharging(false)
				.setPersisted(true)
				.build();

		Integer result = null;

        jobScheduler.cancelAll();

		try {
			result = jobScheduler.schedule(jobInfo);
		} catch (Exception e)
		{
			System.out.println("================== EXCEPTION ========== " + e);
		}


		if ( result != JobScheduler.RESULT_SUCCESS )
		{
			throw new IllegalArgumentException("Was not possible to schedule for sync.");
		}
		else
		{
			Log.d(Syncer.class.getSimpleName(), "Job scheduled successfully");
		}
	}

	/**
	 *
	 */
	public static void requestSync( long fromRevision )
	{
		Objects.requireNonNull( URL, "You must configure the URL to sync." );

        if( jobScheduler == null )
            jobScheduler = (JobScheduler) ApplicationHolder.CONTEXT.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        final ComponentName serviceName = new ComponentName(ApplicationHolder.CONTEXT, SyncBackgroundService.class);
		final JobInfo jobInfo = new JobInfo.Builder( new Long(fromRevision).intValue() , serviceName)
				.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
				.setRequiresDeviceIdle(false)
				.setRequiresCharging(false)
				.setPersisted(true)
				.build();

		Integer result = null;

        jobScheduler.cancelAll();

		try {
			result = jobScheduler.schedule(jobInfo);
		} catch (Exception e)
		{
			System.out.println("================== EXCEPTION ========== " + e);
		}

		if ( result != JobScheduler.RESULT_SUCCESS )
		{
			throw new IllegalArgumentException("Error doing the local operation. Was not possible to schedule for sync.");
		}
		else
		{
			Log.d(Syncer.class.getSimpleName(), "Job scheduled successfully for revision: "+fromRevision);
		}
	}

	/**
	 *
	 * @return
	 */
	public static ISyncResource getSyncResource()
	{
		Objects.requireNonNull( URL, "You must configure the URL to sync." );

		final Feign.Builder builder = Feign.builder()
				.contract( new JAXRSContract() )
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
	 * @return
     */
	public static SyncData syncronize( SyncData localSyncData )
	{
		Objects.requireNonNull( URL, "You must configure the URL to sync." );

		final ISyncResource syncResource = Feign.builder()
				.contract( new JAXRSContract() )
				.encoder(new Encoder()
				{
					@Override
					public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException
					{
						try
						{
							template.body( getMapper().writeValueAsString( object ) );
						}
						catch (Exception e)
						{
							e.printStackTrace();
							throw new EncodeException(e.getMessage());
						}
					}
				})
				.decoder( new JacksonDecoder( getMapper() ) )
				.requestInterceptor( REQUEST_INTERCEPTOR )
				.target(ISyncResource.class, URL);

		final SyncData syncDataServer = syncResource.syncronize(localSyncData);

		return syncDataServer;
	}

}

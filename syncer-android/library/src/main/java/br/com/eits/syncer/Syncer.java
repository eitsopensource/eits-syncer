package br.com.eits.syncer;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.util.Objects;

import br.com.eits.syncer.application.ApplicationHolder;
import br.com.eits.syncer.application.background.SyncBackgroundService;
import br.com.eits.syncer.application.restful.ISyncResource;
import br.com.eits.syncer.domain.service.RepositoryService;
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
		MAPPER.enableDefaultTyping();
		MAPPER.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		MAPPER.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

		return MAPPER;
	}

	/**
	 *
	 * @param entityClass
	 * @return
	 */
	public static <T, ID extends Serializable> RepositoryService<T> forEntity(Class<T> entityClass )
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

		final JobScheduler jobScheduler = (JobScheduler) ApplicationHolder.CONTEXT.getSystemService(Context.JOB_SCHEDULER_SERVICE);

		final ComponentName serviceName = new ComponentName(ApplicationHolder.CONTEXT, SyncBackgroundService.class);
		final JobInfo jobInfo = new JobInfo.Builder(SYNC_NOW_JOB_ID, serviceName)
				.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
				.setRequiresDeviceIdle(false)
				.setRequiresCharging(false)
				.setPersisted(true)
				.build();

		final int result = jobScheduler.schedule(jobInfo);

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

		final JobScheduler jobScheduler = (JobScheduler) ApplicationHolder.CONTEXT.getSystemService(Context.JOB_SCHEDULER_SERVICE);

		final ComponentName serviceName = new ComponentName(ApplicationHolder.CONTEXT, SyncBackgroundService.class);
		final JobInfo jobInfo = new JobInfo.Builder( new Long(fromRevision).intValue() , serviceName)
				.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
				.setRequiresDeviceIdle(false)
				.setRequiresCharging(false)
				.setPersisted(true)
				.build();

		final int result = jobScheduler.schedule(jobInfo);

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
}

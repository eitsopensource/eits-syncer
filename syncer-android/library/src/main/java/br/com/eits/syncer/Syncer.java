package br.com.eits.syncer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import br.com.eits.syncer.application.restful.ISyncResource;
import br.com.eits.syncer.domain.entity.RevisionType;
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
	 * @return
	 */
	public static Syncer instance()
	{
		return new Syncer();
	}

	/*-------------------------------------------------------------------
	 * 		 					ATTRIBUTES
	 *-------------------------------------------------------------------*/
	/**
	 * 
	 */
	protected ISyncResource syncResource;
	
	/**
	 * 
	 */
	private Syncer()
	{
		MAPPER.enableDefaultTyping();
		MAPPER.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		MAPPER.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
	}

	/*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
	/**
	 *
	 * @return
	 */
	protected void configureSyncResource()
	{
		if ( this.syncResource != null ) return;
		Objects.requireNonNull( URL, "You must configure the URL to sync." );
		
		final Feign.Builder builder = Feign.builder()
					.contract( new JAXRSContract() )
					.encoder( new JacksonEncoder( MAPPER ) )
					.decoder( new JacksonDecoder( MAPPER ) );

		if ( REQUEST_INTERCEPTOR != null )
		{
			builder.requestInterceptor( REQUEST_INTERCEPTOR );
		}

		this.syncResource = builder.target( ISyncResource.class, URL );
	}
	
	/**
	 * 
	 * @param localEntities
	 * @return
	 */
	public Map<RevisionType, List<Object>> syncronize( Map<RevisionType, List<Object>> localEntities )
	{
		this.configureSyncResource();
		return this.syncResource.syncronize( localEntities );
	}
}

package br.com.eits.syncer.domain.entity;

import android.content.SharedPreferences;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import br.com.eits.syncer.application.ApplicationHolder;
import br.com.eits.syncer.infrastructure.delegate.SyncServiceDelegate;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 *
 */
public class SyncResourceConfiguration
{
	/**
	 *
	 */
	public static final String SERVICE_NAMES_KEY = "serviceNames";
	/**
	 *
	 */
	private static final Map<String, SyncServiceDelegate> SYNC_RESOURCE_CACHE = new HashMap<>();

    /*-------------------------------------------------------------------
	 * 		 					ATTRIBUTES
	 *-------------------------------------------------------------------*/
	/**
	 *
	 */
	private Map<String, String> syncURLs = new HashMap<>();
	/**
	 *
	 */
	private Interceptor requestInterceptor;
	/**
	 *
	 */
	private Authenticator authenticator;
	/**
	 *
	 */
	private String encoding;
	/**
	 *
	 */
	private ObjectMapper objectMapper = new ObjectMapper();
	/**
	 *
	 */
	private HttpLoggingInterceptor.Level logLevel = HttpLoggingInterceptor.Level.HEADERS;

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/

	/**
	 *
	 */
	public SyncResourceConfiguration()
	{
		//configure the default objectMapper
		this.objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
		this.objectMapper.configure( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false );
		this.objectMapper.setSerializationInclusion( JsonInclude.Include.NON_NULL );//nao serializa o json com null
		this.objectMapper.enableDefaultTypingAsProperty( ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "@type" );
	}

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/

	/**
	 * @param serviceName
	 * @return
	 */
	public SyncServiceDelegate getSyncResource( String serviceName )
	{
		final String serviceUrl = this.syncURLs.get( serviceName );
		Objects.requireNonNull( serviceUrl, "An URL was not found to the service name: " + serviceName );

		SyncServiceDelegate syncDelegate = SYNC_RESOURCE_CACHE.get( serviceUrl );

		if ( syncDelegate == null )
		{
			final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
			loggingInterceptor.setLevel( this.logLevel );
			final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
					.addInterceptor( loggingInterceptor );
			if ( this.requestInterceptor != null )
			{
				clientBuilder.addInterceptor( this.requestInterceptor );
			}
			if ( this.authenticator != null )
			{
				clientBuilder.authenticator( this.authenticator );
			}
			final Retrofit.Builder builder = new Retrofit.Builder()
					.addCallAdapterFactory( RxJava2CallAdapterFactory.createAsync() )
					.addConverterFactory( JacksonConverterFactory.create( this.objectMapper ) )
					.baseUrl( serviceUrl )
					.client( clientBuilder.build() );

			syncDelegate = builder.build().create( SyncServiceDelegate.class );
			SYNC_RESOURCE_CACHE.put( serviceUrl, syncDelegate );
		}
		return syncDelegate;

	}

	/**
	 * @param credentials
	 */
	public void setBasicCredentials( String credentials )
	{
		if ( credentials != null && !credentials.isEmpty() && credentials.contains( ":" ) )
		{
			final String username = credentials.split( ":" )[0];
			final String password = credentials.split( ":" )[1];

			this.authenticator = new Authenticator()
			{
				@Override
				public Request authenticate( Route route, Response response ) throws IOException
				{
					final String credentials = Credentials.basic( username, password );
					return response.request().newBuilder().header( "Authorization", credentials ).build();
				}
			};
		}
		else
		{
			throw new IllegalArgumentException( "The basic credentials must a meta-data like: " +
					"        <meta-data android:name=\"sync-basic-credentials\"\n" +
					"                   android:value=\"username:password\"/>" );
		}
	}

	public void setBearerToken( final String preferences, final String key )
	{
		try
		{
			this.authenticator = new Authenticator()
			{
				@Override
				public Request authenticate( Route route, Response response ) throws IOException
				{
					SharedPreferences settings = ApplicationHolder.CONTEXT.getSharedPreferences( preferences, 0 );
					String token = settings.getString( key, null );
					return response.request().newBuilder().header( "Authorization", "Bearer " + token ).build();
				}
			};
		}
		catch ( Exception e )
		{
			throw new IllegalArgumentException( "The manifest must contain an entry in this format: <meta-data android:name=\"sync-shared-preferences-token\" android:value=\"preferencesName.keyName\" />",
					e );
		}
	}

	/**
	 * @param encoding
	 */
	public void setEncondig( String encoding )
	{
		if ( encoding == null || encoding.isEmpty() )
		{
			throw new IllegalArgumentException( "The enconding must be a value of gzip or/with deflate." );
		}

		this.encoding = encoding;
	}

	/**
	 * @param urls
	 */
	public void setSyncURLs( String urls )
	{
		Objects.requireNonNull( urls, "The Sync URLs must be not null." );

		try
		{
			this.syncURLs = this.objectMapper.readValue( urls, new TypeReference<Map<String, String>>()
			{
			} );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			throw new IllegalArgumentException( "The sync-urls meta-data must be set like: " +
					"<meta-data android:name=\"sync-urls\"\n" +
					"            android:value='{\"default\":\"http://host1.com\", \"service2\":\"http://host2.com\"}'/>" );
		}
	}

	/**
	 * @return
	 */
	public String getDefaultServiceName()
	{
		return (String) this.getServiceNames().toArray()[0];
	}

	/**
	 * @return
	 */
	public Set<String> getServiceNames()
	{
		if ( syncURLs.isEmpty() )
		{
			throw new IllegalStateException( "The Sync URLs is empty. Please verify you manifest." );
		}
		return this.syncURLs.keySet();
	}

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/

	/**
	 * @param requestInterceptor
	 */
	public void setRequestInterceptor( Interceptor requestInterceptor )
	{
		this.requestInterceptor = requestInterceptor;
	}

	/**
	 * @param objectMapper
	 */
	public void setObjectMapper( ObjectMapper objectMapper )
	{
		Objects.requireNonNull( objectMapper, "The objectMapper must be not null." );
		this.objectMapper = objectMapper;
	}

	/**
	 * @return
	 */
	public ObjectMapper getObjectMapper()
	{
		return this.objectMapper;
	}

	/**
	 * @param logLevel
	 */
	public void setLogLevel( HttpLoggingInterceptor.Level logLevel )
	{
		if ( logLevel == null )
		{
			logLevel = HttpLoggingInterceptor.Level.NONE;
		}
		this.logLevel = logLevel;
	}
}
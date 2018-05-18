package br.com.eits.syncer.domain.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import android.content.SharedPreferences;
import br.com.eits.syncer.application.ApplicationHolder;
import br.com.eits.syncer.infrastructure.delegate.SyncServiceDelegate;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import okhttp3.Authenticator;
import okhttp3.ConnectionSpec;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
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
	private Map<String, Object> postReceiveHooks = new HashMap<>();
	/**
	 *
	 */
	private Interceptor requestInterceptor;

	/**
	 *
	 */
	private Interceptor networkInterceptor;
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

	private List<String> syncOrder;

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
		final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
		loggingInterceptor.setLevel( this.logLevel );
		final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
		if ( this.logLevel != HttpLoggingInterceptor.Level.NONE )
		{
			clientBuilder.addInterceptor( loggingInterceptor );
		}
		clientBuilder
				.retryOnConnectionFailure( true )
				.protocols( Collections.singletonList( Protocol.HTTP_1_1 ) )
				.connectTimeout( 5, TimeUnit.SECONDS )
				.writeTimeout( 30, TimeUnit.MINUTES )
				.readTimeout( 30, TimeUnit.MINUTES );

		if ( this.requestInterceptor != null )
		{
			clientBuilder.addInterceptor( this.requestInterceptor );
		}
		if ( this.authenticator != null )
		{
			clientBuilder.authenticator( this.authenticator );
		}
		if ( this.networkInterceptor != null )
		{
			clientBuilder.addNetworkInterceptor( this.networkInterceptor );
		}
		final Retrofit.Builder builder = new Retrofit.Builder()
				.addCallAdapterFactory( RxJava2CallAdapterFactory.createAsync() )
				.addConverterFactory( JacksonConverterFactory.create( this.objectMapper ) )
				.baseUrl( serviceUrl )
				.client( clientBuilder.build() );

		return builder.build().create( SyncServiceDelegate.class );
	}

	private static class ConnectionCloseInterceptor implements Interceptor
	{

		@Override
		public Response intercept( Chain chain ) throws IOException
		{
			return chain.proceed( chain.request().newBuilder().header( "Connection", "close" ).build() );
		}
	}

	/**
	 *
	 */
	public void setBasicCredentials( final String preferences, final String key )
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
					return response.request().newBuilder().header( "Authorization", Credentials.basic( token.split( ":" )[0], token.split( ":" )[1] ) ).build();
				}
			};
		}
		catch ( Exception e )
		{
			throw new IllegalArgumentException( "The manifest must contain an entry in this format: <meta-data android:name=\"sync-shared-preferences-basic\" android:value=\"preferencesName.keyName\" />",
					e );
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

	/**
	 * @param requestInterceptor
	 */
	public void setRequestInterceptor( Interceptor requestInterceptor )
	{
		this.requestInterceptor = requestInterceptor;
	}

	/**
	 * @return
	 */
	public ObjectMapper getObjectMapper()
	{
		return this.objectMapper;
	}

	/*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/

	/**
	 * @param objectMapper
	 */
	public void setObjectMapper( ObjectMapper objectMapper )
	{
		Objects.requireNonNull( objectMapper, "The objectMapper must be not null." );
		this.objectMapper = objectMapper;
	}

	/**
	 * @param logLevel
	 */
	public void setLogLevel( String level )
	{
		if ( logLevel == null )
		{
			logLevel = HttpLoggingInterceptor.Level.NONE;
		}
		this.logLevel = HttpLoggingInterceptor.Level.valueOf( level );
	}

	public List<String> getSyncOrder()
	{
		return syncOrder;
	}

	public void setSyncOrder( String order )
	{
		if ( order == null )
		{
			syncOrder = new ArrayList<>( syncURLs.keySet() );
		}
		else
		{
			syncOrder = Arrays.asList( order.split( "\\s+" ) );
		}
	}

	public Map<String, Object> getPostReceiveHooks()
	{
		return postReceiveHooks;
	}

	public void setPostReceiveHooks( String data )
	{
		if ( data != null )
		{
			try
			{
				Map<String, String> names = this.objectMapper.readValue( data, new TypeReference<Map<String, String>>()
				{
				} );
				for ( String service : names.keySet() )
				{
					Class<?> clazz = Class.forName( names.get( service ) );
					this.postReceiveHooks.put( service, clazz.newInstance() );
				}
			}

			catch ( Exception e )
			{
				throw new IllegalArgumentException( "Could not install post-receive hooks.", e );
			}
		}
	}

	public Interceptor getNetworkInterceptor()
	{
		return networkInterceptor;
	}

	public void setNetworkInterceptor( Interceptor networkInterceptor )
	{
		this.networkInterceptor = networkInterceptor;
	}
}
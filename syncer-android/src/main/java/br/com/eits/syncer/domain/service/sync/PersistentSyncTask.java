package br.com.eits.syncer.domain.service.sync;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import br.com.eits.syncer.Syncer;
import br.com.eits.syncer.application.ApplicationHolder;
import br.com.eits.syncer.domain.entity.PostReceiveHook;
import br.com.eits.syncer.domain.entity.Revision;
import br.com.eits.syncer.domain.entity.SyncData;
import br.com.eits.syncer.domain.entity.SyncTransaction;
import br.com.eits.syncer.infrastructure.dao.RevisionDao;
import br.com.eits.syncer.infrastructure.dao.SQLiteHelper;
import br.com.eits.syncer.infrastructure.delegate.SyncServiceDelegate;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.requery.android.database.sqlite.SQLiteDatabase;

/**
 * Created by eduardo on 22/01/2018.
 */

public class PersistentSyncTask implements ObservableOnSubscribe<Void>
{
	private final RevisionDao revisionDao = new RevisionDao();

	private final String serviceName;

	private List<ObservableEmitter<Void>> subscribers = new ArrayList<>();

	private boolean running = false;

	private int consecutiveFailureCount = 0;

	private final Map<String, TxInfo> receivedPages = new HashMap<>();

	private final String tag;

	private final PostReceiveHook postReceiveHook;

	PersistentSyncTask( String serviceName )
	{
		this.serviceName = serviceName;
		this.tag = "[sync-task//" + serviceName + "]";
		this.postReceiveHook = (PostReceiveHook) Syncer.syncResourceConfiguration().getPostReceiveHooks().get( serviceName );
	}

	boolean isRunning()
	{
		return running;
	}

	@Override
	public void subscribe( ObservableEmitter<Void> e ) throws Exception
	{
		List<ObservableEmitter<Void>> currentSubscribers = new ArrayList<>();
		currentSubscribers.add( e );
		for ( ObservableEmitter<Void> subscriber : this.subscribers )
		{
			if ( !subscriber.isDisposed() )
			{
				currentSubscribers.add( subscriber );
			}
		}
		this.subscribers = currentSubscribers;
		start();
	}

	@SuppressLint("CheckResult")
	private synchronized void start()
	{
		if ( !this.running )
		{
			final SyncServiceDelegate syncResource = Syncer.syncResourceConfiguration().getSyncResource( serviceName );
			@SuppressWarnings("unchecked") final List<Revision<?>> revisions = revisionDao.listByUnsyncedByService( serviceName );
			Log.i( tag, revisions.size() + " revisions to sync." );
			final Revision lastSyncedRevision = revisionDao.findByLastRevisionNumber( serviceName );
			final long lastRevisionNumber = lastSyncedRevision != null ? (lastSyncedRevision.getRevisionNumber() + 1L) : 1L;
			final SyncData localSyncData = new SyncData( lastRevisionNumber, revisions );
			localSyncData.setTransactionId( UUID.randomUUID().toString() );
			Observable.create( new ObservableOnSubscribe<SyncState>()
			{
				@Override
				public void subscribe( ObservableEmitter<SyncState> emitter ) throws Exception
				{
					running = true;
					SQLiteDatabase database = new SQLiteHelper( ApplicationHolder.CONTEXT ).getWritableDatabase();
					try
					{
						if ( !database.isOpen() )
						{
							database = new SQLiteHelper( ApplicationHolder.CONTEXT ).getWritableDatabase();
						}
						database.beginTransaction();
						if ( consecutiveFailureCount > 0 )
						{
							int sleepSeconds = 1;
							Log.w( tag, "Previous attempt failed, waiting for " + sleepSeconds + " seconds before retrying" );
							Thread.sleep( sleepSeconds * 1000 );
						}
						if ( inAirplaneMode() )
						{
							throw new AirplaneModeEnabledException( "Airplane mode on, can't sync." );
						}
						if ( !networkConnectionActive() )
						{
							throw new NoNetworkConnectivityException( "No connections are active." );
						}
						if ( !receivedPages.containsKey( localSyncData.getTransactionId() ) )
						{
							Log.i( tag, "Sending sync request for " + serviceName );
							final SyncTransaction syncTransaction = syncResource.synchronize( localSyncData ).blockingSingle();
							receivedPages.put( syncTransaction.getTransactionId(), new TxInfo( syncTransaction.getTotalPages() ) );
						}
						else
						{
							Log.i( tag, "Already sent revisions for transaction " + localSyncData.getTransactionId() );
						}
						TxInfo transactionInfo = receivedPages.get( localSyncData.getTransactionId() );
						for ( /* nothing */; transactionInfo.received < transactionInfo.total; transactionInfo.received++ )
						{
							Log.i( tag, "[" + serviceName + "] getting page " + (transactionInfo.received + 1) + " of " + transactionInfo.total );
							final SyncData pagedData = syncResource.getPagedData( localSyncData.getTransactionId(), transactionInfo.received + 1 ).blockingSingle();
							emitter.onNext( new SyncState( database, pagedData ) );
						}
						Log.i( tag, "Informing completion of synchronization " + localSyncData.getTransactionId() + " for service " + serviceName );
						syncResource.endSynchronization( localSyncData.getTransactionId() ).subscribeOn( Schedulers.io() ).subscribe();
						receivedPages.remove( localSyncData.getTransactionId() );
						database.setTransactionSuccessful();
						emitter.onComplete();
					}
					catch ( Exception e )
					{
						consecutiveFailureCount++;
						database.endTransaction();
						database.beginTransaction();
						Log.w( tag, e.getMessage(), e );
						throw e;
					}
					finally
					{
						if ( database.isOpen() )
						{
							if ( database.inTransaction() )
							{
								database.endTransaction();
							}
							database.close();
						}
					}
				}
			} ).subscribeOn( Schedulers.io() ).observeOn( Schedulers.trampoline() ).retry( 3 ).subscribe( new Consumer<SyncState>()
			{
				@Override
				public void accept( SyncState syncData ) throws Exception
				{
					storeReceivedRevisions( syncData.database, localSyncData, syncData.syncData );
				}
			}, new Consumer<Throwable>()
			{
				@Override
				public void accept( Throwable throwable ) throws Exception
				{
					for ( ObservableEmitter<Void> subscriber : subscribers )
					{
						subscriber.tryOnError( throwable );
					}
					running = false;
				}
			}, new Action()
			{
				@Override
				public void run() throws Exception
				{
					for ( ObservableEmitter<Void> subscriber : subscribers )
					{
						subscriber.onComplete();
					}
					consecutiveFailureCount = 0;
					running = false;
				}
			} );
		}
	}

	@SuppressWarnings("unchecked")
	private void storeReceivedRevisions( SQLiteDatabase database, SyncData localSyncData, SyncData data )
	{
		Log.i( tag, "Server returned " + data.getRevisions().size() + " revisions to sync." );

		//remove the local unsynced revisions
		final String[] revisionIds = new String[localSyncData.getRevisions().size()];
		for ( int i = 0; i < localSyncData.getRevisions().size(); i++ )
		{
			final Revision<?> revision = localSyncData.getRevisions().get( i );
			revisionIds[i] = String.valueOf( revision.getId() );
		}
		//remove unused revisions
		revisionDao.remove( database, revisionIds );

		//save remote revisions as synced
		for ( Revision<?> revision : data.getRevisions() )
		{
			final Revision<?> newRevision = new Revision<>( revision.getEntity(), revision.getType(), revision.getServiceName() );
			newRevision.setRevisionNumber( revision.getRevisionNumber() );
			newRevision.setSynced( true );
			revisionDao.insertRevisionIfNotExists( database, newRevision );
			revisionDao.removeOldRevisions( database, newRevision );
			if ( postReceiveHook != null )
			{
				postReceiveHook.afterInsert( database, revision );
			}
		}
	}

	private boolean inAirplaneMode()
	{
		return Settings.Global.getInt( ApplicationHolder.CONTEXT.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0 ) != 0;
	}

	@SuppressLint("MissingPermission")
	private boolean networkConnectionActive()
	{
		try
		{
			ConnectivityManager cm = (ConnectivityManager) ApplicationHolder.CONTEXT.getSystemService( Context.CONNECTIVITY_SERVICE );
			assert cm != null;
			NetworkInfo info = cm.getNetworkInfo( 0 );
			if ( info != null && info.getState() == NetworkInfo.State.CONNECTED )
			{
				return true;
			}
			else
			{
				info = cm.getNetworkInfo( 1 );
				if ( info != null && info.getState() == NetworkInfo.State.CONNECTED )
				{
					return true;
				}
			}
			return false;
		}
		catch ( Exception e )
		{
			return false;
		}
	}

	private static class SyncState
	{
		final SQLiteDatabase database;
		final SyncData syncData;

		private SyncState( SQLiteDatabase database, SyncData syncData )
		{
			this.database = database;
			this.syncData = syncData;
		}
	}

	private static class TxInfo
	{
		private int received = 0;
		private final int total;

		private TxInfo( int total )
		{
			this.total = total;
		}
	}

	public static class AirplaneModeEnabledException extends RuntimeException
	{
		public AirplaneModeEnabledException( String message )
		{
			super( message );
		}
	}

	public static class NoNetworkConnectivityException extends RuntimeException
	{
		public NoNetworkConnectivityException( String message )
		{
			super( message );
		}
	}
}

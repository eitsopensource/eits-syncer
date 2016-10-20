package br.com.eits.androidsyncer.application.background;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

/**
 *  Handle the transfer of data between the server and an the app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter
{
    /*-------------------------------------------------------------------
    * 		 					ATTRIBUTES
    *-------------------------------------------------------------------*/

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     *
     * @param context
     * @param autoInitialize
     */
    public SyncAdapter(Context context, boolean autoInitialize )
    {
        this(context, autoInitialize, false);
    }

    /**
     * Set up the sync adapter. This form of the constructor maintains compatibility with Android 3.0 and later platform versions
     *
     * @param context
     * @param autoInitialize
     * @param allowParallelSyncs
     */
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs )
    {
        super( context, autoInitialize, allowParallelSyncs );
    }

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
    /**
     *
     * @param account
     * @param extras
     * @param authority
     * @param provider
     * @param syncResult
     */
    @Override
    public void onPerformSync( Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult )
    {
        Log.wtf("SYNCER", extras+"::AUTH="+authority+"::"+syncResult);

        /**
        try
        {
            final SyncRequest request = new SyncRequest( extras );

            ISyncable syncable = null;

            //who requested, is that a DAO?
            if ( request.getSyncableClass().getSuperclass().equals(AbstractSyncableDao.class) )
            {
                final ORMOpenHelper helper = OpenHelperManager.getHelper(this.getContext(), ORMOpenHelper.class);
                final Dao dao = helper.getDao(request.getEntityClass());
                syncable = (ISyncable) request.getSyncableClass().getConstructor(Dao.class).newInstance(dao);
            }
            else
            {
                syncable = (ISyncable) request.getSyncableClass().newInstance();
            }

            switch ( request.getType() )
            {
                case INSERT:
                {
                    syncable.onRemoteSyncInsert( request.getId() );
                    syncResult.stats.numInserts++;
                    break;
                }
                case UPDATE:
                {
                    syncable.onRemoteSyncUpdate( request.getId() );
                    syncResult.stats.numUpdates++;
                    break;
                }
                case REMOVE:
                {
                    syncable.onRemoteSyncRemove( request.getId() );
                    syncResult.stats.numDeletes++;
                    break;
                }
            }
        }
        catch (Exception e)
        {
            syncResult.stats.numParseExceptions = 0;
            syncResult.databaseError = false;

            e.printStackTrace();
            Log.getStackTraceString( e );
        }
         */
    }
}
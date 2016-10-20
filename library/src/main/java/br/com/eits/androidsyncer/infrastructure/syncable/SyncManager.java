package br.com.eits.androidsyncer.infrastructure.syncable;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import java.io.Serializable;
import java.util.List;

import br.com.eits.androidsyncer.application.ApplicationHolder;
import br.com.eits.androidsyncer.domain.entity.Revision;
import br.com.eits.androidsyncer.domain.entity.RevisionType;

/**
 *
 */
public class SyncManager
{
    /**
     *
     */
    private static final SyncManager INSTANCE = new SyncManager();

    /*-------------------------------------------------------------------
    * 		 					ATTRIBUTES
    *-------------------------------------------------------------------*/
    /**
     *
     */
    private final RevisionDao revisionDao = new RevisionDao();
    /**
     *
     */
    private Account syncAccount;
    /**
     *
     */
    private String contentAuthority;

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    private SyncManager()
    {
    }

    /**
     *
     */
    public static void setupRemoteSync( Account syncAccount, String contentAuthority )
    {
        final AccountManager accountManager = ((AccountManager) ApplicationHolder.CONTEXT.getSystemService(Context.ACCOUNT_SERVICE));

        if ( !accountManager.addAccountExplicitly(syncAccount, null, null) )
        {
            throw new IllegalStateException("Was not to possible to add a sync account.");
        }

        INSTANCE.syncAccount = syncAccount;
        INSTANCE.contentAuthority = contentAuthority;

        // Inform the system that this account supports sync
        ContentResolver.setIsSyncable( INSTANCE.syncAccount, INSTANCE.contentAuthority, 1 );

        // Inform the system that this account is eligible for auto sync when the network is up
        ContentResolver.setSyncAutomatically( INSTANCE.syncAccount, INSTANCE.contentAuthority, true );
    }

    /**
     *
     */
    public static void requestSyncNow( Bundle extras )
    {
        extras = extras == null ? new Bundle(extras) : extras;
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        ContentResolver.requestSync( INSTANCE.syncAccount,
                INSTANCE.contentAuthority,
                extras
        );
    }

    /**
     *
     */
    public static void requestSync( Bundle extras )
    {
        ContentResolver.requestSync( INSTANCE.syncAccount,
                INSTANCE.contentAuthority,
                new Bundle(extras)
        );
    }

    /**
     *
     * @param entity
     * @param id
     * @param type
     * @param <T>
     * @param <ID>
     */
    public static <T extends Serializable, ID extends Serializable> void requestSync( T entity, ID id, RevisionType type )
    {
        //-FAZER PEGAR A ID DA REVISAO DO SERVIDOR
        //-REMOVER CODIGO antigo DE SYNC
        //-FAZER DESACOPLADO
        //-MONTAR SERVICO DE SINCRONISMO NO SERVER E CLIENTE
        try
        {
            final List<Revision> revisions = INSTANCE.revisionDao.list();

            final Revision revision = new Revision();
            revision.setEntityClassName( entity.getClass().getName() );
            revision.setEntityId( id );
            revision.setType( type );
            revision.setId( 1L );

            revisions.add( revision );

            INSTANCE.revisionDao.save( revisions );
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
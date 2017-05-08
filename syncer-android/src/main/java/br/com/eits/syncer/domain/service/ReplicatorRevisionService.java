package br.com.eits.syncer.domain.service;


import android.os.PersistableBundle;
import br.com.eits.syncer.Syncer;
import br.com.eits.syncer.domain.entity.Revision;
import br.com.eits.syncer.domain.entity.SyncResourceConfiguration;

/**
 *
 */
public class ReplicatorRevisionService<T> extends RevisionService<T> implements IReplicatorRevisionService<T>
{
    /*-------------------------------------------------------------------
    * 		 					ATTRIBUTES
    *-------------------------------------------------------------------*/
    /**
     *
     */
    protected final String serviceName;

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    public ReplicatorRevisionService( Class<T> entityClass, String serviceName )
    {
        super(entityClass);

        if ( !Syncer.syncResourceConfiguration().getServiceNames().contains(serviceName) )
        {
            throw new IllegalArgumentException( "An URL was not found to the service name: "+serviceName);
        }

        this.serviceName = serviceName;
    }

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    @Override
    protected synchronized Revision insertRevisionAndSync(Revision revision )
    {
        this.revisionDao.insertRevision( revision );

        final PersistableBundle extras = new PersistableBundle();
        extras.putString(SyncResourceConfiguration.SERVICE_NAME_KEY, this.serviceName);

        Syncer.requestSync( extras );
        return revision;
    }
}
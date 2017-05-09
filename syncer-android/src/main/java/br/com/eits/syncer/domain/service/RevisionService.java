package br.com.eits.syncer.domain.service;


import android.app.Activity;
import android.os.PersistableBundle;
import br.com.eits.syncer.Syncer;
import br.com.eits.syncer.domain.entity.Revision;
import br.com.eits.syncer.domain.entity.RevisionType;
import br.com.eits.syncer.domain.entity.SyncResourceConfiguration;
import br.com.eits.syncer.infrastructure.dao.RevisionDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public class RevisionService<T> implements IRevisionService<T>
{
    /*-------------------------------------------------------------------
    * 		 					ATTRIBUTES
    *-------------------------------------------------------------------*/
    /**
     *
     */
    protected final RevisionDao<T> revisionDao = new RevisionDao<T>();
    /**
     *
     */
    protected final Class<T> entityClass;
    /**
     *
     */
    protected String serviceName;

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    public RevisionService( Class<T> entityClass )
    {
        this.entityClass = entityClass;
        this.serviceName = Syncer.syncResourceConfiguration().getDefaultServiceName();

    }

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    protected synchronized Revision insertRevisionAndSync( Revision revision )
    {
        this.revisionDao.insertRevision( revision );

        final PersistableBundle extras = new PersistableBundle();
        extras.putString(SyncResourceConfiguration.SERVICE_NAME_KEY, this.serviceName);

        Syncer.requestSync( extras );
        return revision;
    }

    /**
     *
     */
    public synchronized T insertAsSynced( T entity )
    {
        final Revision revision = new Revision( entity, RevisionType.INSERT );
        revision.setSynced( true );
        this.revisionDao.insertRevision( revision );
        return entity;
    }

    /**
     *
     */
    public synchronized T insertWithoutSync( T entity )
    {
        final Revision revision = new Revision( entity, RevisionType.INSERT );
        this.revisionDao.insertRevision( revision );
        return entity;
    }

    /**
     *
     */
    public synchronized T insert( T entity )
    {
        final Revision revision = new Revision( entity, RevisionType.INSERT );
        this.insertRevisionAndSync( revision );
        return entity;
    }

    /**
     *
     */
    public synchronized T update( T entity )
    {
        final Revision revision = new Revision( entity, RevisionType.UPDATE );
        this.insertRevisionAndSync( revision );
        return entity;
    }

    /**
     *
     */
    public synchronized void remove( T entity )
    {
        final Revision revision = new Revision( entity, RevisionType.REMOVE );
        this.insertRevisionAndSync( revision );
    }

    /**
     *
     */
    public synchronized T findByEntityId( Object entityId )
    {
        Objects.requireNonNull( entityId, "You must set a entity id" );
        final Revision<T> revision = this.revisionDao.findByEntityId( entityClass, entityId.toString() );
        return revision != null ? revision.getEntity() : null;
    }

    /**
     * @return
     */
    public synchronized List<T> listAll()
    {
        return this.listByFilters(null);
    }

    /**
     * @return
     */
    public synchronized List<T> listByFilters( String filters )
    {
        final List<T> entities = new ArrayList<T>();

        final List<Revision<T>> revisions = this.revisionDao.listByFilters( this.entityClass, filters );

        for ( Revision<T> revision : revisions )
        {
            entities.add( revision.getEntity() );
        }

        return entities;
    }

    /**
     *
     * @return
     */
    @Override
    public IQueryRevisionService query()
    {
        return new QueryRevisionService( this.entityClass );
    }

    /**
     * @param activity
     * @return
     */
    @Override
    public IWatcherRevisionService<T> watch( Activity activity )
    {
        return new WatcherRevisionService<>( this.entityClass, activity );
    }

    /**
     * @param serviceName
     * @return
     */
    @Override
    public IRevisionService<T> to( String serviceName )
    {
        if ( !Syncer.syncResourceConfiguration().getServiceNames().contains(serviceName) )
        {
            throw new IllegalArgumentException( "An URL was not found to the service name: "+serviceName);
        }

        this.serviceName = serviceName;
        return this;
    }
}
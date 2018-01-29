package br.com.eits.syncer.domain.service;


import android.app.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.eits.syncer.Syncer;
import br.com.eits.syncer.domain.entity.Revision;
import br.com.eits.syncer.domain.entity.RevisionType;
import br.com.eits.syncer.domain.service.sync.SyncOnDemandService;
import br.com.eits.syncer.infrastructure.dao.RevisionDao;

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
    protected Revision insertRevisionAndSync( Revision revision )
    {
        this.revisionDao.insertRevision( revision );
        SyncOnDemandService.syncNowBackground( this.serviceName );
        return revision;
    }

    /**
     *
     */
    public T insertAsSynced( T entity )
    {
        final Revision revision = new Revision( entity, RevisionType.INSERT, this.serviceName );
        revision.setSynced( true );
        this.revisionDao.insertRevision( revision );
        return entity;
    }

    /**
     *
     */
    public T insert( T entity )
    {
        final Revision revision = new Revision( entity, RevisionType.INSERT, this.serviceName );
        this.insertRevisionAndSync( revision );
        return entity;
    }

    /**
     *
     */
    public List<T> insert( List<T> entities )
    {
        final List<Revision<T>> revisions = new ArrayList<>();
        for( T entity : entities )
        {
            final Revision revision = new Revision( entity, RevisionType.INSERT, this.serviceName );
            revisions.add( revision );
        }
        this.revisionDao.insertRevisions( revisions );
        SyncOnDemandService.syncNowBackground( this.serviceName );
        return entities;
    }

    /**
     *
     */
    public T update( T entity )
    {
        final Revision revision = new Revision( entity, RevisionType.UPDATE, this.serviceName );
        this.insertRevisionAndSync( revision );
        return entity;
    }

    /**
     *
     */
    public List<T> update( List<T> entities )
    {
        final List<Revision<T>> revisions = new ArrayList<>();
        for( T entity : entities )
        {
            final Revision revision = new Revision( entity, RevisionType.UPDATE, this.serviceName );
            revisions.add( revision );
        }
        this.revisionDao.insertRevisions( revisions );
        SyncOnDemandService.syncNowBackground( this.serviceName );
        return entities;
    }

    /**
     *
     */
    public void remove( T entity )
    {
        final Revision revision = new Revision( entity, RevisionType.REMOVE, this.serviceName );
        this.insertRevisionAndSync( revision );
    }

    /**
     *
     */
    public void remove( List<T> entities )
    {
        final List<Revision<T>> revisions = new ArrayList<>();
        for( T entity : entities )
        {
            final Revision revision = new Revision( entity, RevisionType.REMOVE, this.serviceName );
            revisions.add( revision );
        }
        this.revisionDao.insertRevisions( revisions );
        SyncOnDemandService.syncNowBackground( this.serviceName );
    }

    /**
     *
     */
    public T findByEntityId( Object entityId )
    {
        Objects.requireNonNull( entityId, "You must set a entity id" );
        final Revision<T> revision = this.revisionDao.findByEntityId( entityClass, entityId.toString() );
        return revision != null ? revision.getEntity() : null;
    }

    /**
     * @return
     */
    public List<T> listAll()
    {
        return this.listByFilters(null);
    }

    /**
     * @return
     */
    public List<T> listByFilters( String filters )
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
package br.com.eits.syncer.domain.service;


import android.app.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.eits.syncer.Syncer;
import br.com.eits.syncer.domain.entity.Revision;
import br.com.eits.syncer.domain.entity.RevisionType;
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

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    public RevisionService( Class<T> entityClass )
    {
        this.entityClass = entityClass;
    }

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    protected synchronized Revision insertRevisionAndSync( Revision revision )
    {
        revisionDao.insertRevision( revision );
        Syncer.requestSync();
        return revision;
    }

    /**
     *
     */
    public synchronized T insertWithoutSync( T entity )
    {
        final Revision revision = new Revision( entity, RevisionType.INSERT );
        revision.setSynced(true);
        revisionDao.insertRevision( revision );
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
        final Revision<T> revision = revisionDao.findByEntityId( entityClass, entityId.toString() );
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
    public synchronized IQueryRevisionService query()
    {
        return new QueryRevisionService( this.entityClass );
    }

    /**
     * @param activity
     * @return
     */
    @Override
    public synchronized IObservableRevisionService<T> watch( Activity activity )
    {
        return new ObservableRevisionService<>(this.entityClass, activity);
    }
}
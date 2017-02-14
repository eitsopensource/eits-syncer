package br.com.eits.syncer.domain.service;


import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;
import java.util.UUID;

import br.com.eits.syncer.Syncer;
import br.com.eits.syncer.domain.entity.Revision;
import br.com.eits.syncer.domain.entity.RevisionType;
import br.com.eits.syncer.infrastructure.dao.RevisionDao;

/**
 *
 */
public class RepositoryService<T>
{
    /*-------------------------------------------------------------------
    * 		 					ATTRIBUTES
    *-------------------------------------------------------------------*/
    /**
     *
     */
    private final Class<T> entityClass;
    /**
     *
     */
    private final RevisionDao<T> revisionDao;

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    public RepositoryService( Class<T> entityClass )
    {
        this.entityClass = entityClass;
        this.revisionDao = new RevisionDao();
    }

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    public Revision insert( T entity )
    {
        final Revision revision = new Revision( entity, RevisionType.INSERT );
        revision.setEntityId(UUID.randomUUID().toString());

        this.revisionDao.open();
        this.revisionDao.insertRevision( revision );
        this.revisionDao.close();

        Syncer.requestSync( revision.getRevision() );

        return revision;
    }

    /**
     *
     */
    public T update( T entity )
    {
        final Revision revision = new Revision( entity, RevisionType.UPDATE );

        this.revisionDao.open();
        this.revisionDao.insertRevision( revision );
        this.revisionDao.close();

        Syncer.requestSync( revision.getRevision() );

        return entity;
    }

    /**
     *
     */
    public void remove( T entity )
    {
        final Revision revision = new Revision( entity, RevisionType.REMOVE );

        this.revisionDao.open();
        this.revisionDao.insertRevision( revision );
        this.revisionDao.close();

        Syncer.requestSync( revision.getRevision() );
    }

    /**
     *
     */
    public List<T> listAll()
    {
        return this.revisionDao.listAll( entityClass );
    }

    /**
     *
     */
    public T findById( JsonTypeInfo.Id id )
    {
//        COMO FAZER COM A ID DA ENTIDADE?
//            COLOCAR NO JSON? AUTO GERAR?
//        VER COMO OS NOSQL FAZEM..
//        return this.revisionDao.queryForId(id);
        return null;
    }

    /**
     *
     */
//    public QueryBuilder<Entity, ID> queryBuilder()
//    {
//        return this.dao.queryBuilder();
//    }
}
package br.com.eits.androidsyncer.domain.service;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import br.com.eits.androidsyncer.application.ApplicationHolder;
import br.com.eits.androidsyncer.domain.entity.Revision;
import br.com.eits.androidsyncer.domain.entity.RevisionType;
import br.com.eits.androidsyncer.infrastructure.dao.ORMOpenHelper;
import br.com.eits.androidsyncer.infrastructure.dao.RevisionDao;

/**
 *
 */
public class LocalRepositoryService<Entity, ID extends Serializable>
{
    /*-------------------------------------------------------------------
    * 		 					ATTRIBUTES
    *-------------------------------------------------------------------*/
    /**
     *
     */
    private final Class<Entity> entityClass;
    /**
     *
     */
    private final RevisionDao revisionDao;
    /**
     *
     */
    private final ScheduleService scheduleService;
    /**
     *
     */
    private ORMOpenHelper helper;
    /**
     *
     */
    private RuntimeExceptionDao<Entity, ID> dao;

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    public LocalRepositoryService( Class<Entity> entityClass )
    {
        this.entityClass = entityClass;
        this.helper = new ORMOpenHelper( ApplicationHolder.CONTEXT );

        this.revisionDao = new RevisionDao( this.helper.getRuntimeExceptionDao(Revision.class) );

        this.setupEntityDao();

        this.scheduleService = new ScheduleService();
    }

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/

    /**
     *
     */
    private void setupEntityDao()
    {
        if ( entityClass.equals(Revision.class) ) throw new IllegalArgumentException("Can not store the Revision entity.");

        try
        {
            //FIXME MAN! What to do when the local database must be updated?! :X
            //TODO FAZER EVITAR DAR CREATE TABLE TODA HORA
            //TODO VERIFICAR INSTANCIAS DO ORMOpenHelper
            TableUtils.createTableIfNotExists( this.helper.getConnectionSource(), this.entityClass );
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
        }

        this.dao = helper.getRuntimeExceptionDao(entityClass);
    }

    /**
     *
     */
    public Entity insert( Entity entity )
    {
        this.dao.create( entity );

        final ID id = this.dao.extractId( entity );
        this.insertRevisionAndRequestSync( id, RevisionType.INSERT );

        return entity;
    }

    /**
     *
     */
    public Entity update( Entity entity )
    {
        this.dao.update( entity );

        final ID id = this.dao.extractId( entity );
        this.insertRevisionAndRequestSync( id, RevisionType.UPDATE );

        return entity;
    }

    /**
     *
     * @param entity
     * @return
     */
    public Entity save( Entity entity )
    {
        final Dao.CreateOrUpdateStatus status = this.dao.createOrUpdate(entity);

        final ID id = this.dao.extractId(entity);
        if ( status.isCreated() )
        {
            this.insertRevisionAndRequestSync( id, RevisionType.INSERT );
        }
        else
        {
            this.insertRevisionAndRequestSync( id, RevisionType.UPDATE );
        }

        return entity;
    }

    /**
     *
     */
    public void remove( Entity entity )
    {
        this.dao.delete( entity );

        final ID id = this.dao.extractId( entity );
        this.insertRevisionAndRequestSync( id, RevisionType.REMOVE );
    }

    /**
     *
     */
    public void remove( ID id )
    {
        this.dao.deleteById( id );

        this.insertRevisionAndRequestSync( id, RevisionType.REMOVE );
    }

    /**
     *
     */
    public List<Entity> listAll()
    {
        return this.dao.queryForAll();
    }

    /**
     *
     */
    public QueryBuilder<Entity, ID> queryBuilder()
    {
        return this.dao.queryBuilder();
    }

    /**
     *
     */
    public Entity findById( ID id )
    {
        return this.dao.queryForId(id);
    }

    /**
     *
     */
    private Revision insertRevisionAndRequestSync(ID id, RevisionType revisionType )
    {
        final Revision revision = new Revision();
        revision.setEntityClassName( this.entityClass.getName() );
        revision.setEntityId(id);
        revision.setType(revisionType);
        revision.setSynced(false);

        this.revisionDao.create( revision );

        this.scheduleService.requestSync( revision );
        return revision;
    }
}
package br.com.eits.androidsyncer.infrastructure.syncable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.io.Serializable;

import br.com.eits.androidsyncer.domain.entity.RevisionType;
import br.com.eits.common.domain.entity.IEntity;

/**
 *
 */
public abstract class AbstractSyncableDao<T extends IEntity, ID extends Serializable> extends RuntimeExceptionDao<T, ID>
{
    /*-------------------------------------------------------------------
	 * 		 					ATTRIBUTES
	 *-------------------------------------------------------------------*/

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     *
     * @param dao
     */
    public AbstractSyncableDao(Dao<T, ID> dao )
    {
        super( dao );
    }

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
    /**
     *
     * @param data
     * @return
     */
    @Override
    public int create(T data)
    {
        final int rows = super.create(data);
        SyncManager.requestSync( data, data.getId(), RevisionType.INSERT );
        return rows;

    }

    /**
     *
     * @param data
     * @return
     */
    @Override
    public T createIfNotExists(T data)
    {
        data = super.createIfNotExists(data);
        SyncManager.requestSync( data, data.getId(), RevisionType.INSERT );
        return data;
    }

    /**
     *
     * @param data
     * @return
     */
    @Override
    public CreateOrUpdateStatus createOrUpdate(T data)
    {
        final CreateOrUpdateStatus status = super.createOrUpdate(data);

        if ( status.isCreated() )
        {
            SyncManager.requestSync( data, data.getId(), RevisionType.INSERT );
        }
        else
        {
            SyncManager.requestSync( data, data.getId(), RevisionType.UPDATE );
        }

        return status;
    }

    /**
     *
     * @param data
     * @return
     */
    @Override
    public int update(T data)
    {
        final int rows =  super.update(data);
        SyncManager.requestSync( data, data.getId(), RevisionType.UPDATE );
        return rows;
    }

    /**
     *
     * @param data
     * @return
     */
    @Override
    public int delete(T data)
    {
        final int rows = super.delete(data);
        SyncManager.requestSync( data, data.getId(), RevisionType.REMOVE );
        return rows;
    }

    /**
     *
     * @param id
     * @return
     */
    @Override
    public int deleteById(ID id)
    {
        final T entity = super.queryForId( id );
        final int rows = super.deleteById(id);
        SyncManager.requestSync( entity, id, RevisionType.REMOVE );
        return rows;
    }
}
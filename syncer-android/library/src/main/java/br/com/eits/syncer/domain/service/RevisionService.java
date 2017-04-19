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
    protected Revision insertRevisionAndSync( Revision revision )
    {
        revisionDao.insertRevision( revision );
        Syncer.requestSync();
        return revision;
    }

    /**
     *
     */
    public T insertWithoutSync( T entity )
    {
        final Revision revision = new Revision( entity, RevisionType.INSERT );
        revision.setSynced(true);
        revisionDao.insertRevision( revision );
        return entity;
    }

    /**
     *
     */
    public T insert( T entity )
    {
        final Revision revision = new Revision( entity, RevisionType.INSERT );
        this.insertRevisionAndSync( revision );
        return entity;
    }

    /**
     *
     */
    public T update( T entity )
    {
        final Revision revision = new Revision( entity, RevisionType.UPDATE );
        this.insertRevisionAndSync( revision );
        return entity;
    }

    /**
     *
     */
    public void remove( T entity )
    {
        final Revision revision = new Revision( entity, RevisionType.REMOVE );
        this.insertRevisionAndSync( revision );
    }

    /**
     *
     */
    public T findByEntityId( Object entityId )
    {
        Objects.requireNonNull( entityId, "You must set a entity id" );
        final Revision<T> revision = revisionDao.findByEntityId( entityClass, entityId.toString() );
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
     * @param filters
     * @param relatedEntityClass
     * @param relatedEntityId
     * @return

    public List<T> listByFiltersLookingForRelatedEntity( String filters, Class<?> relatedEntityClass, Long relatedEntityId )
    {
        List<T> entities = new ArrayList<T>();

        filters = filters == null ? "" : filters;

        final String simpleClassName = relatedEntityClass.getSimpleName().substring(0, 1).toLowerCase() + relatedEntityClass.getSimpleName().substring(1);

        String joinTable = "json_each(" + SQLiteHelper.COLUMN_ENTITY + ")";
        String[] columnsToShow = null;
        String where = SQLiteHelper.COLUMN_ENTITY_CLASSNAME + " = ? AND " +
                "json_extract("+ SQLiteHelper.COLUMN_ENTITY + ", '$." + simpleClassName + "." + SQLiteHelper.COLUMN_ENTITY_ID + "') = ? AND " +
                "json_each.type NOT IN ( 'object', 'array' ) AND json_each.value LIKE '%" + filters + "%'";

        Object [] whereArguments = new Object[] { this.entityClass.getName(), relatedEntityId };

        String groupBy = "json_extract(" + SQLiteHelper.COLUMN_ENTITY + ", '$." + SQLiteHelper.COLUMN_ENTITY_ID + "')";
        String having = SQLiteHelper.TABLE_REVISION + "." + SQLiteHelper.COLUMN_TYPE + " <> " + RevisionType.REMOVE.ordinal();
        String orderBy = SQLiteHelper.COLUMN_ID + " DESC";

        revisionDao.open();
        final List<Revision<?>> revisions = revisionDao.queryForRevisions( joinTable, columnsToShow, where, whereArguments, groupBy, having, orderBy );
        revisionDao.close();

        for( Revision revision : revisions )
        {
            entities.add( this.entityClass.cast( revision.getEntity() ) );
        }

        return entities;
    }
     */

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
    public IObservableRevisionService<T> watch( Activity activity )
    {
        return new ObservableRevisionService<>(this.entityClass, activity);
    }
}
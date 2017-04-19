package br.com.eits.syncer.domain.service;


import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import br.com.eits.syncer.Syncer;
import br.com.eits.syncer.domain.entity.Revision;
import br.com.eits.syncer.domain.entity.RevisionType;
import br.com.eits.syncer.infrastructure.dao.RevisionDao;
import br.com.eits.syncer.infrastructure.dao.SQLiteHelper;

/**
 *
 */
public class RevisionService<T> implements IRevisionService<T>
{
    /**
     *
     */
    protected static final RevisionDao REVISION_DAO = new RevisionDao();

    /*-------------------------------------------------------------------
    * 		 					ATTRIBUTES
    *-------------------------------------------------------------------*/
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
        REVISION_DAO.open();
        REVISION_DAO.insertRevision( revision );
        REVISION_DAO.close();

        Syncer.requestSync();

        return revision;
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
    public T findById( long entityId )
    {
        final String[] columnsToShow = null;

        final String where = SQLiteHelper.COLUMN_ENTITY_CLASSNAME + " = ? AND json_extract("+ SQLiteHelper.COLUMN_ENTITY + ", '$." + SQLiteHelper.COLUMN_ID + "') = ? ";
        final Object[] whereArguments = new Object[] { this.entityClass.getName(), entityId };

        final String groupBy = "json_extract(" + SQLiteHelper.COLUMN_ENTITY + ", '$." + SQLiteHelper.COLUMN_ID + "')";
        final String having = SQLiteHelper.TABLE_REVISION + "." + SQLiteHelper.COLUMN_TYPE + " <> " + RevisionType.REMOVE.ordinal();
        final String orderBy = SQLiteHelper.COLUMN_REVISION_DATE + " DESC";

        REVISION_DAO.open();
        final Revision revision = REVISION_DAO.queryForRevision( columnsToShow, where, whereArguments, groupBy, having, orderBy );
        REVISION_DAO.close();

        return revision != null ? this.entityClass.cast( revision.getEntity() ) : null;
    }

    /**
     *
     */
    public List<T> listAll()
    {
        return this.listByFilters( "" );
    }

    /**
     * @return
     */
    public List<T> listByFilters( String filters )
    {
        final List<T> entities = new ArrayList<T>();

        filters = filters == null ? "" : filters;

        final String joinTable = "json_each(" + SQLiteHelper.COLUMN_ENTITY + ")";
        final String[] columnsToShow = null;

        final String where = SQLiteHelper.COLUMN_ENTITY_CLASSNAME + " = ? AND json_each.type NOT IN ( 'object', 'array' ) AND json_each.value LIKE '%" + filters + "%'";
        final Object [] whereArguments = new Object[] { this.entityClass.getName() };

        final String groupBy = "json_extract(" + SQLiteHelper.COLUMN_ENTITY + ", '$." + SQLiteHelper.COLUMN_ID + "')";
        final String having = SQLiteHelper.TABLE_REVISION + "." + SQLiteHelper.COLUMN_TYPE + " <> " + RevisionType.REMOVE.ordinal();
        final String orderBy = SQLiteHelper.COLUMN_REVISION_DATE + " DESC";

        REVISION_DAO.open();
        final List<Revision<?>> revisions = REVISION_DAO.queryForRevisions( joinTable, columnsToShow, where, whereArguments, groupBy, having, orderBy );
        REVISION_DAO.close();

        for ( Revision revision : revisions )
        {
            entities.add( this.entityClass.cast( revision.getEntity() ) );
        }

        return entities;
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

    /**
     * @param filters
     * @param relatedEntityClass
     * @param relatedEntityId
     * @return
     */
    public List<T> listByFiltersLookingForRelatedEntity( String filters, Class<?> relatedEntityClass, Long relatedEntityId )
    {
        List<T> entities = new ArrayList<T>();

        filters = filters == null ? "" : filters;

        final String simpleClassName = relatedEntityClass.getSimpleName().substring(0, 1).toLowerCase() + relatedEntityClass.getSimpleName().substring(1);

        String joinTable = "json_each(" + SQLiteHelper.COLUMN_ENTITY + ")";
        String[] columnsToShow = null;
        String where = SQLiteHelper.COLUMN_ENTITY_CLASSNAME + " = ? AND " +
                "json_extract("+ SQLiteHelper.COLUMN_ENTITY + ", '$." + simpleClassName + "." + SQLiteHelper.COLUMN_ID + "') = ? AND " +
                "json_each.type NOT IN ( 'object', 'array' ) AND json_each.value LIKE '%" + filters + "%'";

        Object [] whereArguments = new Object[] { this.entityClass.getName(), relatedEntityId };

        String groupBy = "json_extract(" + SQLiteHelper.COLUMN_ENTITY + ", '$." + SQLiteHelper.COLUMN_ID + "')";
        String having = SQLiteHelper.TABLE_REVISION + "." + SQLiteHelper.COLUMN_TYPE + " <> " + RevisionType.REMOVE.ordinal();
        String orderBy = SQLiteHelper.COLUMN_REVISION_DATE + " DESC";

        REVISION_DAO.open();
        final List<Revision<?>> revisions = REVISION_DAO.queryForRevisions( joinTable, columnsToShow, where, whereArguments, groupBy, having, orderBy );
        REVISION_DAO.close();

        for( Revision revision : revisions )
        {
            entities.add( this.entityClass.cast( revision.getEntity() ) );
        }

        return entities;
    }

}
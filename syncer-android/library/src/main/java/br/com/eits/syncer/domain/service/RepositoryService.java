package br.com.eits.syncer.domain.service;


import android.database.Cursor;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.eits.syncer.Syncer;
import br.com.eits.syncer.domain.entity.Revision;
import br.com.eits.syncer.domain.entity.RevisionType;
import br.com.eits.syncer.infrastructure.dao.RevisionDao;
import br.com.eits.syncer.infrastructure.dao.SQLiteHelper;

/**
 *
 */
public class RepositoryService<T> {
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
    public RepositoryService(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.revisionDao = new RevisionDao();
    }

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/

    /**
     *
     */
    public Revision insert(T entity) {
        final Revision revision = new Revision(entity, RevisionType.INSERT);
        revision.setEntityId(UUID.randomUUID().toString());

        revisionDao.open();
        revisionDao.insertRevision(revision);
        revisionDao.close();

        Syncer.requestSync(revision.getRevisionDate());
        return revision;
    }

    /**
     *
     */
    public T update(T entity) {
        final Revision revision = new Revision(entity, RevisionType.UPDATE);

        this.revisionDao.open();
        this.revisionDao.insertRevision(revision);
        this.revisionDao.close();

        Syncer.requestSync(revision.getRevisionDate());

        return entity;
    }

    /**
     *
     */
    public void remove(T entity) {
        final Revision revision = new Revision(entity, RevisionType.REMOVE);

        this.revisionDao.open();
        this.revisionDao.insertRevision(revision);
        this.revisionDao.close();

        Syncer.requestSync(revision.getRevisionDate());
    }

    /**
     *
     */
    public List<T> listAll() {
        return this.revisionDao.listAll(entityClass);
    }

    /**
     * @return
     */
    public List<T> listByFilters( String filters )
    {
        List<T> entities = new ArrayList<T>();

        filters = filters == null ? "" : filters;

        String joinTable = "json_each(" + SQLiteHelper.COLUMN_ENTITY + ")";
        String[] columnsToShow = null;

        String where = SQLiteHelper.COLUMN_ENTITY_CLASSNAME + " = ? AND json_each.type NOT IN ( 'object', 'array' ) AND json_each.value LIKE '%" + filters + "%'";
        Object [] whereArguments = new Object[] { this.entityClass.getName() };

        String groupBy = "json_extract(" + SQLiteHelper.COLUMN_ENTITY + ", '$." + SQLiteHelper.COLUMN_ID + "')";
        String having = SQLiteHelper.TABLE_REVISION + "." + SQLiteHelper.COLUMN_TYPE + " <> " + RevisionType.REMOVE.ordinal();
        String orderBy = SQLiteHelper.COLUMN_REVISION_DATE + " DESC";

        this.revisionDao.open();
        List<Revision> revisions = this.revisionDao.queryForRevisions( joinTable, columnsToShow, where, whereArguments, groupBy, having, orderBy );
        this.revisionDao.close();

        for( Revision revision : revisions )
        {
            entities.add( this.entityClass.cast( revision.getEntity() ) );
        }

        return entities;
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

        String joinTable = "json_each(" + SQLiteHelper.COLUMN_ENTITY + ")";
        String[] columnsToShow = null;
        String where = SQLiteHelper.COLUMN_ENTITY_CLASSNAME + " = ? AND " +
                "json_extract("+ SQLiteHelper.COLUMN_ENTITY + ", '$." + relatedEntityClass.getSimpleName().toLowerCase() + "." + SQLiteHelper.COLUMN_ID + "') = ? AND " +
                "json_each.type NOT IN ( 'object', 'array' ) AND json_each.value LIKE '%" + filters + "%'";

        Object [] whereArguments = new Object[] { this.entityClass.getName(), relatedEntityId };

        String groupBy = "json_extract(" + SQLiteHelper.COLUMN_ENTITY + ", '$." + SQLiteHelper.COLUMN_ID + "')";
        String having = SQLiteHelper.TABLE_REVISION + "." + SQLiteHelper.COLUMN_TYPE + " <> " + RevisionType.REMOVE.ordinal();
        String orderBy = SQLiteHelper.COLUMN_REVISION_DATE + " DESC";

        this.revisionDao.open();
        List<Revision> revisions = this.revisionDao.queryForRevisions( joinTable, columnsToShow, where, whereArguments, groupBy, having, orderBy );
        this.revisionDao.close();

        for( Revision revision : revisions )
        {
            entities.add( this.entityClass.cast( revision.getEntity() ) );
        }

        return entities;
    }

    /**
     *
     */
    public T findById( Long entityId )
    {
        String[] columnsToShow = null;

        String where = SQLiteHelper.COLUMN_ENTITY_CLASSNAME + " = ? AND json_extract("+ SQLiteHelper.COLUMN_ENTITY + ", '$." + SQLiteHelper.COLUMN_ID + "') = ? ";
        Object[] whereArguments = new Object[] { this.entityClass.getName(), entityId };

        String groupBy = null;
        String having = null;
        String orderBy = SQLiteHelper.COLUMN_REVISION_DATE + " DESC";

        this.revisionDao.open();
        Revision revision = this.revisionDao.queryForRevision( columnsToShow, where, whereArguments, groupBy, having, orderBy );
        this.revisionDao.close();

        return this.entityClass.cast( revision.getEntity() );
    }
}
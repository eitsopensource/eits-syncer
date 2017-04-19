package br.com.eits.syncer.infrastructure.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import br.com.eits.syncer.Syncer;
import br.com.eits.syncer.application.ApplicationHolder;
import br.com.eits.syncer.domain.entity.Revision;
import br.com.eits.syncer.domain.entity.RevisionType;
import io.requery.android.database.sqlite.SQLiteDatabase;

/**
 * Created by rodrigo.p.fraga on 03/11/16.
 */
public class RevisionDao<T>
{
    /**
     *
     */
    private static final SQLiteHelper HELPER = new SQLiteHelper( ApplicationHolder.CONTEXT );

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    public RevisionDao()
    {
    }

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
    /**
     * @param revision
     * @return
     */
    public Revision<T> insertRevision( Revision<T> revision )
    {
        final SQLiteDatabase database = HELPER.getWritableDatabase();

        final ContentValues values = new ContentValues();

        values.put( SQLiteHelper.COLUMN_ID, revision.getId());
        values.put( SQLiteHelper.COLUMN_REVISION_NUMBER, revision.getRevisionNumber() );
        values.put( SQLiteHelper.COLUMN_SYNCED, revision.getSynced() );
        values.put( SQLiteHelper.COLUMN_TYPE, revision.getType().ordinal() );
        values.put( SQLiteHelper.COLUMN_ENTITY, this.toJSON( revision.getEntity() ) );
        values.put( SQLiteHelper.COLUMN_ENTITY_CLASSNAME, revision.getEntityClassName() );
        values.put( SQLiteHelper.COLUMN_ENTITY_ID, revision.getEntityId() );
        values.put( SQLiteHelper.COLUMN_ENTITY_ID_NAME, revision.getEntityIdName() );

        database.insert( SQLiteHelper.TABLE_REVISION, null, values );

        HELPER.close();
        return revision;
    }

    /**
     *
     * @param id
     * @return
     */
    public Revision<T> findById( long id )
    {
        final SQLiteDatabase database = HELPER.getReadableDatabase();

        final String where = SQLiteHelper.COLUMN_ID+"=?";
        final Object[] whereArguments = new Object[] { id };

        final Cursor cursor = database.query( SQLiteHelper.TABLE_REVISION, null, where, whereArguments, null, null, null);

        if ( cursor.moveToFirst() )
        {
            final Revision<T> revision = this.fromCursorToRevision(cursor);
            cursor.close();
            HELPER.close();

            return revision;
        }

        return null;
    }

    /**
     *
     * @param entityId
     * @return
     */
    public Revision<T> findByEntityId( Class<T> className, String entityId )
    {
        final SQLiteDatabase database = HELPER.getReadableDatabase();

        final String where = SQLiteHelper.COLUMN_ENTITY_ID +"=? AND "+SQLiteHelper.COLUMN_ENTITY_CLASSNAME+"=?";
        final Object[] whereArguments = new Object[] { entityId, className.getName() };

        final Cursor cursor = database.query( SQLiteHelper.TABLE_REVISION, null, where, whereArguments, null, null, null);

        if ( cursor.moveToFirst() )
        {
            final Revision<T> revision = this.fromCursorToRevision(cursor);
            cursor.close();
            HELPER.close();

            return revision;
        }

        return null;
    }

    /**
     *
     * @param className
     * @param filters
     * @return
     */
    public List<Revision<T>> listByFilters( Class<T> className, String filters )
    {
        final SQLiteDatabase database = HELPER.getReadableDatabase();

        filters = filters == null ? "" : filters;

        final String tables = SQLiteHelper.TABLE_REVISION+", json_each("+ SQLiteHelper.COLUMN_ENTITY +")";

        //NOT IN nao deixa procurar nos varios niveis do json
        final String where = SQLiteHelper.COLUMN_ENTITY_CLASSNAME + " = ? AND json_each.type NOT IN ( 'object', 'array' ) AND json_each.value LIKE '%?%'";
        final Object[] whereArguments = new Object[] { className.getName(), filters };
        final String groupBy = SQLiteHelper.COLUMN_ENTITY_ID;
        final String having = SQLiteHelper.TABLE_REVISION + "." + SQLiteHelper.COLUMN_TYPE + " <> " + RevisionType.REMOVE.ordinal();
        final String orderBy = SQLiteHelper.COLUMN_ID + " DESC";

        final Cursor cursor = database.query( tables, null, where, whereArguments, groupBy, having, orderBy);
        cursor.moveToFirst();

        final List<Revision<T>> revisions = new ArrayList<>();
        while ( !cursor.isAfterLast() )
        {
            revisions.add( this.fromCursorToRevision( cursor ) );
            cursor.moveToNext();
        }
        cursor.close();
        HELPER.close();

        return revisions;
    }

    /**
     *
     * @return
     */
    public Revision findByLastRevisionNumber()
    {
        final SQLiteDatabase database = HELPER.getReadableDatabase();

        final String orderBy = SQLiteHelper.COLUMN_REVISION_NUMBER + " DESC";
        final String limit = "1";

        final Cursor cursor = database.queryWithFactory( null, false, SQLiteHelper.TABLE_REVISION, null, null, null, null, null, orderBy, limit);
        if ( cursor.moveToFirst() )
        {
            final Revision<?> revision = this.fromCursorToRevision(cursor);
            cursor.close();
            HELPER.close();

            return revision;
        }

        return null;
    }

    /**
     *
     * @return
     */
    public List<Revision<?>> listByUnsynced()
    {
        final SQLiteDatabase database = HELPER.getReadableDatabase();

        final String where = SQLiteHelper.COLUMN_SYNCED + " = ?";
        final Object[] whereArguments = new Object[] { Boolean.FALSE };

        final Cursor cursor = database.query( SQLiteHelper.TABLE_REVISION, null, where, whereArguments, null, null, null);
        cursor.moveToFirst();

        final List<Revision<?>> revisions = new ArrayList<>();
        while ( !cursor.isAfterLast() )
        {
            revisions.add( this.fromCursorToRevision( cursor ) );
            cursor.moveToNext();
        }
        cursor.close();
        HELPER.close();

        return revisions;
    }

    /**
     *
     * @param ids
     */
    public void remove( String... ids )
    {
        final SQLiteDatabase database = HELPER.getWritableDatabase();

        final String where = SQLiteHelper.COLUMN_ID+" IN ("+TextUtils.join(",", ids)+")";
        database.delete( SQLiteHelper.TABLE_REVISION, where, null );

        HELPER.close();
    }

    /**
     *
     * @param cursor
     * @return
     */
    private Revision<T> fromCursorToRevision( Cursor cursor )
    {
        try
        {
            final Class<?> entityClass = Class.forName( cursor.getString(SQLiteHelper.COLUMN_ENTITY_CLASSNAME_INDEX) );
            final Object entity = this.toEntity( cursor.getString(SQLiteHelper.COLUMN_ENTITY_INDEX), entityClass );

            final Revision revision = new Revision(
                    cursor.getLong(SQLiteHelper.COLUMN_ID_INDEX),
                    entity,
                    RevisionType.valueOf(cursor.getInt(SQLiteHelper.COLUMN_TYPE_INDEX))
            );

            revision.setRevisionNumber( cursor.getLong( SQLiteHelper.COLUMN_REVISION_NUMBER_INDEX ) );
            revision.setSynced( cursor.getLong( SQLiteHelper.COLUMN_SYNCED_INDEX ) == 1 ? true : false );

            return revision;
        }
        catch ( ClassNotFoundException e )
        {
            e.printStackTrace();
            throw new IllegalStateException("Could not parser the persisted json to an entity instance", e);
        }
    }

    /**
     *
     * @param entity
     * @return
     */
    private String toJSON( Object entity )
    {
        try
        {
            return Syncer.getMapper().writeValueAsString( entity );
        }
        catch ( Exception e )
        {
            throw new IllegalArgumentException("Error serializing the entity", e);
        }
    }

    /**
     *
     * @param json
     * @param entityClass
     * @return
     */
    private Object toEntity( String json, Class<?> entityClass )
    {
        try
        {
            return Syncer.getMapper().readValue( json, entityClass );
        }
        catch ( Exception e )
        {
            throw new IllegalArgumentException("Error serializing the entity", e);
        }
    }
}

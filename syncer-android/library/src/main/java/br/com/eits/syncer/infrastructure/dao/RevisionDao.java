package br.com.eits.syncer.infrastructure.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

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
public class RevisionDao
{
    /**
     *
     */
    private SQLiteDatabase database;
    /**
     *
     */
    private final SQLiteHelper helper;

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    public RevisionDao()
    {
        this.helper = new SQLiteHelper( ApplicationHolder.CONTEXT );
    }

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    public void open()
    {
        this.database = this.helper.getWritableDatabase();
    }

    /**
     *
     */
    public void close()
    {
        //this.helper.close();
    }

    /**
     * @param revision
     * @return
     */
    public Revision insertRevision( Revision revision )
    {
        final ContentValues values = new ContentValues();

        values.put( SQLiteHelper.COLUMN_REVISION_DATE, revision.getRevisionDate() );
        values.put( SQLiteHelper.COLUMN_REVISION_NUMBER, revision.getRevisionNumber() );
        values.put( SQLiteHelper.COLUMN_SYNCED, revision.getSynced() );
        values.put( SQLiteHelper.COLUMN_TYPE, revision.getType().ordinal() );
        values.put( SQLiteHelper.COLUMN_ENTITY, this.toJSON( revision.getEntity() ) );
        values.put( SQLiteHelper.COLUMN_ENTITY_CLASSNAME, revision.getEntityClassName() );

        final Long insertId = this.database.insert( SQLiteHelper.TABLE_REVISION, null, values );
        Log.d( RevisionDao.class.getSimpleName(), insertId.toString() );

        return revision;
    }

    /**
     * @param columnsToShow
     * @param where
     * @param whereArguments
     * @param groupBy
     * @param having
     * @param orderBy
     * @return
     */
    public Revision queryForRevision( String[] columnsToShow, String where, Object[] whereArguments, String groupBy, String having, String orderBy )
    {
        Revision revision = null;

        final Cursor cursor = this.database.query( SQLiteHelper.TABLE_REVISION, columnsToShow, where, whereArguments, groupBy, having, orderBy );

        cursor.moveToFirst();
        if ( !cursor.isAfterLast() )
        {
            revision = this.revisionParse( cursor );
        }
        cursor.close();

        return revision;
    }

    /**
     * @param columnsToShow
     * @param where
     * @param whereArguments
     * @param groupBy
     * @param having
     * @param orderBy
     * @return
     */
    public List<Revision<?>> queryForRevisions( String joinTable, String[] columnsToShow, String where, Object[] whereArguments, String groupBy, String having, String orderBy )
    {
        String tables = SQLiteHelper.TABLE_REVISION;

        if( joinTable != null )
        {
            tables = tables.concat( ", " + joinTable );
        }

        List<Revision<?>> revisions = new ArrayList<>();
        final Cursor cursor = this.database.query( tables, columnsToShow, where, whereArguments, groupBy, having, orderBy );

        cursor.moveToFirst();
        while ( !cursor.isAfterLast() )
        {
            revisions.add( this.revisionParse( cursor ) );
            cursor.moveToNext();
        }
        cursor.close();

        return revisions;
    }

    /**
     * @param columnsToShow
     * @param where
     * @param whereArguments
     * @param groupBy
     * @param having
     * @param orderBy
     * @return
     */
    public List<Revision<?>> queryForRevisions( SQLiteDatabase.CursorFactory cursorFactory, boolean distinct, String joinTable, String[] columnsToShow, String where, Object[] whereArguments, String groupBy, String having, String orderBy, String limit )
    {
        String tables = SQLiteHelper.TABLE_REVISION;

        if( joinTable != null )
        {
            tables = tables.concat( ", " + joinTable );
        }

        final List<Revision<?>> revisions = new ArrayList<>();
        final Cursor cursor = this.database.queryWithFactory( cursorFactory, distinct, tables, columnsToShow, where, whereArguments, groupBy, having, orderBy, limit );

        cursor.moveToFirst();

        while ( !cursor.isAfterLast() )
        {
            revisions.add( this.revisionParse( cursor ) );
            cursor.moveToNext();
        }
        cursor.close();

        return revisions;
    }

    /**
     * @return
     */
    public Revision<?> findLastSyncedRevision()
    {
        String[] columnsToShow = null;
        String where = SQLiteHelper.COLUMN_SYNCED + " = ?";
        Object[] whereArguments = new Object[] {"1"};
        String groupBy = null;
        String having = null;
        String orderBy = SQLiteHelper.COLUMN_REVISION_DATE + " DESC";

        return this.queryForRevision( columnsToShow, where, whereArguments, groupBy, having, orderBy );
    }

    /**
     * @return
     */
    public List<Revision<?>> listUnsyncedRevisions()
    {
        String tables = null;
        String[] columnsToShow = null;
        String where = SQLiteHelper.COLUMN_SYNCED + " = ?";
        Object[] whereArguments = new Object[] {"0"};
        String groupBy = null;
        String having = null;
        String orderBy = null;

        return this.queryForRevisions( tables, columnsToShow, where, whereArguments, groupBy, having, orderBy );
    }

    /**
     *
     */
    public void removeAllNotSynced()
    {
        database.delete(
                SQLiteHelper.TABLE_REVISION,
                SQLiteHelper.COLUMN_SYNCED + " = ?",
                new String[]{"0"}
        );
    }

    /**
     * @param cursor
     * @return
     */
    private Revision<?> revisionParse( Cursor cursor )
    {
        final Revision revision = new Revision( cursor.getString( SQLiteHelper.COLUMN_ENTITY_INDEX ), RevisionType.getRevisionTypeByOrdinalValue( cursor.getInt( SQLiteHelper.COLUMN_TYPE_INDEX ) ) );
        revision.setRevisionDate( cursor.getLong( SQLiteHelper.COLUMN_REVISION_DATE_INDEX ) );
        revision.setRevisionNumber( cursor.getLong( SQLiteHelper.COLUMN_REVISION_NUMBER_INDEX ) );
        revision.setSynced( cursor.getLong( SQLiteHelper.COLUMN_SYNCED_INDEX ) == 1 ? true : false );
        revision.setEntityClassName( cursor.getString( SQLiteHelper.COLUMN_ENTITY_CLASSNAME_INDEX ) );

        try
        {
            Class<?> entityClass = Class.forName( revision.getEntityClassName() );
            revision.setEntity( this.toEntity( cursor.getString( SQLiteHelper.COLUMN_ENTITY_INDEX ), entityClass ) );
        }
        catch ( ClassNotFoundException e )
        {
            e.printStackTrace();
            throw new IllegalStateException("Could not parser the persisted json to an entity instance", e);
        }

        return revision;
    }

    /**
     *
     * @param entity
     * @return
     */
    public String toJSON( Object entity )
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
     * @param entityClass
     * @return
     */
    public Object toEntity( String json, Class<?> entityClass )
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

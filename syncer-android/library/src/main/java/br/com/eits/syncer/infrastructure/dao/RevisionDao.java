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
public class RevisionDao<T>
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
        this.helper.close();
    }

    /**
     * @param revision
     * @return
     */
    public Revision insertRevision( Revision<T> revision )
    {
        final ContentValues values = new ContentValues();
        
        values.put( SQLiteHelper.COLUMN_REVISION, revision.getRevision() );
        values.put( SQLiteHelper.COLUMN_ENTITY, this.toJSON( revision.getEntity() ) );
        values.put( SQLiteHelper.COLUMN_ENTITY_ID, this.toJSON( revision.getEntityId()  ) );
        values.put( SQLiteHelper.COLUMN_ENTITY_CLASSNAME, revision.getEntityClassName() );
        values.put( SQLiteHelper.COLUMN_SYNCED, revision.getSynced() );
        values.put( SQLiteHelper.COLUMN_TYPE, revision.getType().ordinal() );

        final Long insertId = this.database.insert( SQLiteHelper.TABLE_REVISION, null, values );
        Log.d( RevisionDao.class.getSimpleName(), insertId.toString() );

        return revision;
    }

    /**
     *
     * @param entityClass
     * @return
     */
    public List<T> listAll( Class<T> entityClass )
    {
        final List<T> entities = new ArrayList<>();

        final Cursor cursor = database.query(
                SQLiteHelper.TABLE_REVISION,
                new String[]{SQLiteHelper.COLUMN_ENTITY},
                SQLiteHelper.COLUMN_ENTITY_CLASSNAME + " = " + entityClass.getName(),
                null, null, null, null);

        cursor.moveToFirst();
        while ( !cursor.isAfterLast() )
        {
            final T entity = this.toEntity( cursor.getString(0), entityClass );
            entities.add( entity );
            cursor.moveToNext();
        }
        cursor.close();

        return entities;
    }


    /**
     *
     * @param column
     * @return
     */
    public List<Revision> queryForEq( String column, Object value )
    {

        final List<Revision> entities = new ArrayList<>();

        final Cursor cursor = database.query(
                SQLiteHelper.TABLE_REVISION, null,
                column + " = ?" ,
                new Object[] {value}, null, null, null);

        cursor.moveToFirst();
        while ( !cursor.isAfterLast() )
        {
            Revision entity = null;

            entity = this.revisionParse(cursor);

            entities.add( entity );
            cursor.moveToNext();
        }
        cursor.close();

        return entities;

    }

    /**
     * @param cursor
     * @return
     */
    private Revision revisionParse(Cursor cursor)
    {
        RevisionType revisionType = null;

        switch (cursor.getInt(2))
        {
            case (0):
                revisionType = RevisionType.INSERT;
                break;
            case (1):
                revisionType = RevisionType.UPDATE;
                break;
            case(2):
                revisionType = RevisionType.REMOVE;
                break;
            case(3):
                revisionType = RevisionType.UPDATE_ID;
                break;
        }

        Revision revision = new Revision( cursor.getString(3), revisionType );
        revision.setRevision(cursor.getLong(0));
        revision.setSynced(cursor.getLong(1) == 1 ? true : false );
        revision.setEntityId(cursor.getString(4));
        revision.setEntityClassName(cursor.getString(5));

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
    public T toEntity( String json, Class<T> entityClass )
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

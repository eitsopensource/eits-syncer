package br.com.eits.syncer.infrastructure.dao;

import android.content.Context;

import io.requery.android.database.sqlite.SQLiteDatabase;
import io.requery.android.database.sqlite.SQLiteOpenHelper;

/**
 *
 */
public class SQLiteHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "_syncer_.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_REVISION = "revision";
    public static final String COLUMN_REVISION = "revision";
    public static final String COLUMN_SYNCED = "synced";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_ENTITY = "entity";
    public static final String COLUMN_ENTITY_ID = "entity_id";
    public static final String COLUMN_ENTITY_CLASSNAME = "entity_class_name";

    /**
     *
     */
    public static final String[] TABLE_REVISION_COLUMNS = {
            SQLiteHelper.COLUMN_REVISION,
            SQLiteHelper.COLUMN_SYNCED,
            SQLiteHelper.COLUMN_TYPE,
            SQLiteHelper.COLUMN_ENTITY,
            SQLiteHelper.COLUMN_ENTITY_ID,
            SQLiteHelper.COLUMN_ENTITY_CLASSNAME
    };

    // Database creation sql statement
    private static final String DATABASE_CREATE =
        "CREATE TABLE "
            + TABLE_REVISION + "( "
                + COLUMN_REVISION + " INTEGER PRIMARY KEY, "
                + COLUMN_SYNCED + " BOOLEAN NOT NULL, "
                + COLUMN_TYPE + " TINYINT NOT NULL, "
                + COLUMN_ENTITY + " TEXT NOT NULL, "
                + COLUMN_ENTITY_ID + " TEXT NOT NULL, "
                + COLUMN_ENTITY_CLASSNAME + " VARCHAR(255) NOT NULL "
        +");";

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     *
     * @param context
     */
    public SQLiteHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*-------------------------------------------------------------------
	 * 		 					  BEHAVIORS
	 *-------------------------------------------------------------------*/
    /**
     *
     * @param database
     */
    @Override
    public void onCreate( SQLiteDatabase database )
    {
        database.execSQL(DATABASE_CREATE);
    }

    /**
     *
     * @param database
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
    {
    }
}
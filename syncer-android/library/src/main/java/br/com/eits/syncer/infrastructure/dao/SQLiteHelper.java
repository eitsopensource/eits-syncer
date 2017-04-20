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

    /**
     *
     */
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_REVISION_NUMBER = "revision_number";
    public static final String COLUMN_SYNCED = "synced";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_ENTITY = "entity";
    public static final String COLUMN_ENTITY_CLASSNAME = "entity_class_name";
    public static final String COLUMN_ENTITY_ID = "entity_id";
    public static final String COLUMN_ENTITY_ID_NAME = "entity_id_name";

    public static final int COLUMN_ID_INDEX = 0;
    public static final int COLUMN_REVISION_NUMBER_INDEX = 1;
    public static final int COLUMN_SYNCED_INDEX = 2;
    public static final int COLUMN_TYPE_INDEX = 3;
    public static final int COLUMN_ENTITY_INDEX = 4;
    public static final int COLUMN_ENTITY_CLASSNAME_INDEX = 5;

    // Database creation sql statement
    private static final String DATABASE_CREATE =
        "CREATE TABLE "
            + TABLE_REVISION + "( "
                + COLUMN_ID + " INTEGER PRIMARY KEY, "
                + COLUMN_REVISION_NUMBER + " INTEGER, "
                + COLUMN_SYNCED + " BOOLEAN NOT NULL, "
                + COLUMN_TYPE + " TINYINT NOT NULL, "
                + COLUMN_ENTITY + " TEXT NOT NULL, "
                + COLUMN_ENTITY_CLASSNAME + " VARCHAR(255) NOT NULL, "
                + COLUMN_ENTITY_ID + " VARCHAR(255) NOT NULL, "
                + COLUMN_ENTITY_ID_NAME + " VARCHAR(100) NOT NULL "
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
     * @param database
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
    {

    }
}
package br.com.eits.syncer.infrastructure.dao;

import android.content.Context;

import br.com.eits.syncer.domain.entity.Revision;
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

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_REVISION_DATE = "revision_date";
    public static final String COLUMN_REVISION_NUMBER = "revision_number";
    public static final String COLUMN_SYNCED = "synced";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_ENTITY = "entity";
    public static final String COLUMN_ENTITY_CLASSNAME = "entity_class_name";

    public static final int COLUMN_REVISION_DATE_INDEX = 0;
    public static final int COLUMN_REVISION_NUMBER_INDEX = 1;
    public static final int COLUMN_SYNCED_INDEX = 2;
    public static final int COLUMN_TYPE_INDEX = 3;
    public static final int COLUMN_ENTITY_INDEX = 4;
    public static final int COLUMN_ENTITY_CLASSNAME_INDEX = 5;

    /**
     *
     */
    public static final String[] TABLE_REVISION_COLUMNS = {
            SQLiteHelper.COLUMN_REVISION_DATE,
            SQLiteHelper.COLUMN_REVISION_NUMBER,
            SQLiteHelper.COLUMN_SYNCED,
            SQLiteHelper.COLUMN_TYPE,
            SQLiteHelper.COLUMN_ENTITY,
            SQLiteHelper.COLUMN_ENTITY_CLASSNAME
    };

    // Database creation sql statement
    private static final String DATABASE_CREATE =
        "CREATE TABLE "
            + TABLE_REVISION + "( "
                + COLUMN_REVISION_DATE + " INTEGER PRIMARY KEY, "
                + COLUMN_REVISION_NUMBER + " INTEGER, "
                + COLUMN_SYNCED + " BOOLEAN NOT NULL, "
                + COLUMN_TYPE + " TINYINT NOT NULL, "
                + COLUMN_ENTITY + " TEXT NOT NULL, "
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
     * @param database
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
    {

    }
}
package br.com.eits.syncer.infrastructure.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.field.DataPersisterManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import br.com.eits.syncer.domain.entity.Revision;
import br.com.eits.syncer.infrastructure.dao.types.CalendarDataType;

/**
 * @created rodrigo
 */
public class ORMOpenHelper extends OrmLiteSqliteOpenHelper
{
    /**
     *
     */
    private static final String DATABASE_NAME = "_syncer_.db";
    /**
     *
     */
    private static final int DATABASE_VERSION = 1;

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     * @param context
     */
    public ORMOpenHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.setupDataPersisters();
    }

    /*-------------------------------------------------------------------
	 * 		 					 BEHAVIORS
	 *-------------------------------------------------------------------*/

    /**
     * Custom data persisters
     */
    private void setupDataPersisters()
    {
        DataPersisterManager.registerDataPersisters(
                CalendarDataType.getSingleton()
        );
    }

    /**
     * @param database
     * @param connectionSource
     */
    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource)
    {
        try
        {
            TableUtils.createTable( connectionSource, Revision.class );
        }
        catch (SQLException e)
        {
            //TODO on exception?
            throw new RuntimeException(e);
        }
    }

    /**
     * @param database
     * @param connectionSource
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade( SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion )
    {
        //TODO onUpgrade?
        System.out.println( oldVersion );
        System.out.println( newVersion );
    }
}
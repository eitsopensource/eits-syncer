package br.com.eits.androidsyncer.domain.entity;

import android.os.Bundle;

import br.com.eits.androidsyncer.infrastructure.syncable.SyncType;

/**
 *
 */
public class SyncRequest
{
    /*-------------------------------------------------------------------
	 * 		 					ATTRIBUTES
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    private Long id;
    /**
     *
     */
    private SyncType type;
    /**
     *
     */
    private Class<?> entityClass;
    /**
     *
     */
    private Class<?> syncableClass;

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     *
     * @param type
     * @param entityClass
     * @param id
     */
    public SyncRequest(Class<?> syncableClass, SyncType type, Class<?> entityClass, Long id )
    {
        this.id = id;
        this.type = type;
        this.entityClass = entityClass;
        this.syncableClass = syncableClass;
    }

    /**
     *
     * @param extras
     */
    public SyncRequest(Bundle extras ) throws ClassNotFoundException
    {
        this.id = extras.getLong("id");
        this.type = SyncType.valueOf( extras.getString("type") );
        this.entityClass = Class.forName( extras.getString("entityClassName") );
        this.syncableClass = Class.forName( extras.getString("syncableClassName") );
    }

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
    /**
     *
     * @return
     */
    public Bundle toExtras()
    {
        final Bundle extras = new Bundle();
        extras.putLong( "id", this.id );
        extras.putString( "type", this.type.name() );
        extras.putString( "entityClassName", this.entityClass.getName() );
        extras.putString( "syncableClassName", this.syncableClass.getName() );
        return extras;
    }

    /*-------------------------------------------------------------------
	 * 		 			    GETTERS AND SETTERS
	 *-------------------------------------------------------------------*/
    /**
     *
     * @return
     */
    public SyncType getType()
    {
        return type;
    }

    /**
     *
     * @return
     */
    public Long getId()
    {
        return id;
    }

    /**
     *
     * @return
     */
    public Class<?> getEntityClass()
    {
        return this.entityClass;
    }

    /**
     *
     * @return
     */
    public Class<?> getSyncableClass()
    {
        return this.syncableClass;
    }
}

package br.com.eits.androidsyncer.domain.service;

import java.io.Serializable;

/**
 *
 */
public class SyncManager
{
    /**
     *
     */
    private static final SyncManager INSTANCE = new SyncManager();

    /*-------------------------------------------------------------------
    * 		 					ATTRIBUTES
    *-------------------------------------------------------------------*/
    /**
     *
     */
    private ScheduleService scheduleService = new ScheduleService();

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    private SyncManager()
    {
    }

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/

    /**
     *
     * @param entityClass
     * @return
     */
    public static <T, ID extends Serializable> LocalRepositoryService<T, ID> forEntity( Class<T> entityClass )
    {
        return new LocalRepositoryService<T, ID>( entityClass );
    }

    /**
     *
     */
    public static void requestSyncNow()
    {
        INSTANCE.scheduleService.requestSyncNow();
    }
}
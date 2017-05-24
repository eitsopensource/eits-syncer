package br.com.eits.syncer.domain.service;

import android.app.Activity;

import java.util.List;

/**
 * Created by rodrigo.p.fraga on 19/04/17.
 */
public interface IRevisionService<T>
{
    /**
     *
     * @param entity
     * @return
     */
    public T insert( T entity );
    /**
     *
     * @param entities
     * @return
     */
    public List<T> insert( List<T> entities );

    /**
     *
     * @param entity
     * @return
     */
    public T insertAsSynced( T entity );

    /**
     *
     * @param entity
     * @return
     */
    public T update( T entity );

    /**
     *
     * @param entities
     * @return
     */
    public List<T> update( List<T> entities );

    /**
     *
     * @param entity
     */
    public void remove( T entity );

    /**
     *
     * @param entities
     */
    public void remove( List<T> entities );

    /**
     *
     * @param entityId
     * @return
     */
    public T findByEntityId( Object entityId );

    /**
     *
     * @return
     */
    public List<T> listAll();

    /**
     *
     * @param filters
     * @return
     */
    public List<T> listByFilters( String filters );

    /**
     *
     * @return
     */
    public IQueryRevisionService query();

    /**
     *
     * @param activity
     * @return
     */
    public IWatcherRevisionService<T> watch(Activity activity );

    /**
     *
     * @param serviceName
     * @return
     */
    public IRevisionService<T> to(String serviceName );
}

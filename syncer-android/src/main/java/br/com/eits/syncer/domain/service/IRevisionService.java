package br.com.eits.syncer.domain.service;

import android.app.Activity;
import io.requery.android.database.sqlite.SQLiteDatabase;

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
	T insert( T entity );
    /**
     *
     * @param entities
     * @return
     */
	List<T> insert( List<T> entities );

	/**
	 * @param entity
	 * @return
	 */
	T insertAsSynced( T entity );

    /**
     *
     * @param entity
     * @return
     */
    T update( T entity );

	/**
     *
     * @param database
	 * @param entity
     * @return
     */
    T update( SQLiteDatabase database, T entity );

	/**
     *
     * @param entities
     * @return
     */
    List<T> update( List<T> entities );

	/**
     *
     * @param entity
     */
    void remove( T entity );

	/**
     *
     * @param entities
     */
    void remove( List<T> entities );

	/**
     *
     * @param entityId
     * @return
     */
    T findByEntityId( Object entityId );

	/**
     *
     * @return
     */
    List<T> listAll();

	/**
     *
     * @param filters
     * @return
     */
    List<T> listByFilters( String filters );

	/**
     *
     * @return
     */
    IQueryRevisionService<T> query();

	/**
     *
     * @param activity
     * @return
     */
    IWatcherRevisionService<T> watch( Activity activity );

	/**
     *
     * @param serviceName
     * @return
     */
    IRevisionService<T> to( String serviceName );
}

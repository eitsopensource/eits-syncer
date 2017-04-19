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
     * @param entity
     * @return
     */
    public T update( T entity );

    /**
     *
     * @param entity
     */
    public void remove( T entity );

    /**
     *
     * @param entityId
     * @return
     */
    public T findById( long entityId );

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
     * @param filters
     * @param relatedEntityClass
     * @param relatedEntityId
     * @return
     */
    public List<T> listByFiltersLookingForRelatedEntity( String filters, Class<?> relatedEntityClass, Long relatedEntityId );

    /**
     *
     * @param activity
     * @return
     */
    public IObservableRevisionService<T> watch( Activity activity );
}

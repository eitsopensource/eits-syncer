package br.com.eits.syncer.domain.service;


import android.app.Activity;

import java.util.List;
import java.util.Objects;
import java.util.Observer;

/**
 *
 */
public class ObservableRevisionService<T> extends RevisionService<T> implements IObservableRevisionService<T>
{
    /*-------------------------------------------------------------------
    * 		 					ATTRIBUTES
    *-------------------------------------------------------------------*/
    /**
     *
     */
    private final Activity activity;

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    public ObservableRevisionService( Class<T> entityClass, Activity activity )
    {
        super( entityClass );
        this.activity = activity;
    }

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
    /**
     *
     * @param entityId
     * @param handler
     * @return
     */
    @Override
    public T findById(long entityId, Observer handler)
    {
        Objects.requireNonNull( handler, "You must set an observer to handler" );
        return null;
    }

    /**
     *
     * @param handler
     * @return
     */
    @Override
    public List<T> listAll(Observer handler)
    {
        Objects.requireNonNull( handler, "You must set an observer to handler" );
        return null;
    }

    /**
     *
     * @param filters
     * @param handler
     * @return
     */
    @Override
    public List<T> listByFilters(String filters, Observer handler)
    {
        Objects.requireNonNull( handler, "You must set an observer to handler" );
        return null;
    }
}
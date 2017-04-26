package br.com.eits.syncer.domain.service;


import android.app.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

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
        this.activity = activity; // on finish activity remove all watchers
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
    public void findByEntityId( final long entityId, IHandler<T> handler )
    {
        Objects.requireNonNull( handler, "You must set an observer to handler" );

        final Watcher watcher = new Watcher(new Callable() {
            @Override
            public T call() throws Exception {
                return ObservableRevisionService.super.findByEntityId( entityId );
            }
        }, handler);

        Watcher.addWatcher( watcher );
        watcher.execute();
    }

    /**
     *
     * @param handler
     * @return
     */
    @Override
    public void listAll( IHandler<List<T>> handler )
    {
        Objects.requireNonNull( handler, "You must set an observer to handler" );

        final Watcher watcher = new Watcher(new Callable() {
            @Override
            public List<T> call() throws Exception {
                return ObservableRevisionService.super.listAll();
            }
        }, handler);

        Watcher.addWatcher( watcher );
        watcher.execute();
    }

    /**
     *
     * @param handler
     * @return
     */
    @Override
    public void query( final IQueryRevisionService<T> queryRevisionService, IHandler<List<T>> handler )
    {
        Objects.requireNonNull( handler, "You must set an observer to handler" );

        final Watcher watcher = new Watcher(new Callable() {
            @Override
            public List<T> call() throws Exception {
                return queryRevisionService.list();
            }
        }, handler);

        Watcher.addWatcher( watcher );
        watcher.execute();
    }
}
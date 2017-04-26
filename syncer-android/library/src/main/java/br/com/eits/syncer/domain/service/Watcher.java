package br.com.eits.syncer.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by eits on 26/04/17.
 */

public class Watcher<T>{

    /**
     *
     */
    public static final List<Watcher> watchers = new ArrayList<Watcher>();

    /*-------------------------------------------------------------------
	 * 		 					ATTRIBUTES
	 *-------------------------------------------------------------------*/

    /**
     *
     */
    private Callable<T> function;

    /**
     *
     */
    private IHandler<T> handler;

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/

    /**
     *
     * @param function
     * @param handler
     */
    public Watcher( Callable<T> function, IHandler<T> handler )
    {
        this.function = function;
        this.handler = handler;
    }

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/

    /**
     *
     */
    public void execute()
    {
        try{
            this.handler.handle( this.function.call() );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param watcher
     */
    public static void addWatcher( Watcher watcher )
    {
        if( !watchers.contains( watcher ) ) watchers.add( watcher );
    }

    /**
     *
     */
    public static void notifyObservers()
    {
        for( Watcher watcher : watchers )
        {
            watcher.execute();
        }
    }

    /*-------------------------------------------------------------------
	 * 		 				GETTERS AND SETTERS
	 *-------------------------------------------------------------------*/

    /**
     *
     * @return
     */
    public Callable<T> getFunction() {
        return function;
    }

    /**
     *
     * @param function
     */
    public void setFunction( Callable<T> function ) {
        this.function = function;
    }

    /**
     *
     * @return
     */
    public IHandler getHandler() {
        return this.handler;
    }

    /**
     *
     * @param handler
     */
    public void setHandler( IHandler handler ) {
        this.handler = handler;
    }
}

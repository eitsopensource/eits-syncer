package br.com.eits.syncer.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by eits on 26/04/17.
 */

public class Watcher<T>
{
    /**
     *
     */
    public static final List<Watcher> watchers = new ArrayList<>();

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
        try
        {
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
    public synchronized static void addWatcher( Watcher watcher )
    {
        if( !watchers.contains( watcher ) ) watchers.add( watcher );
    }

    /**
     *
     * @param watcher
     */
    public synchronized  static void removeWatcher( Watcher watcher )
    {
        if( !watchers.contains( watcher ) ) watchers.remove( watcher );
    }

    /**
     *
     */
    public synchronized static void notifyObservers()
    {
        //Concurrent exception if remove a watcher inside for each
        final Object[] watchersArray = watchers.toArray();

        for( int i = 0; i < watchersArray.length; i++ )
        {
            final Watcher watcher = (Watcher) watchersArray[i];
            watcher.execute();
        }
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Watcher<T> watcher = (Watcher<T>) o;

        if (!function.equals( watcher.function )) return false;
        return handler.equals( watcher.handler );

    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode()
    {
        int result = function.hashCode();
        result = 31 * result + handler.hashCode();
        return result;
    }

    /*-------------------------------------------------------------------
	 * 		 				GETTERS AND SETTERS
	 *-------------------------------------------------------------------*/

    /**
     *
     * @return
     */
    public Callable<T> getFunction()
    {
        return function;
    }

    /**
     *
     * @param function
     */
    public void setFunction( Callable<T> function )
    {
        this.function = function;
    }

    /**
     *
     * @return
     */
    public IHandler getHandler()
    {
        return this.handler;
    }

    /**
     *
     * @param handler
     */
    public void setHandler( IHandler handler )
    {
        this.handler = handler;
    }
}

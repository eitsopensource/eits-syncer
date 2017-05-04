package br.com.eits.syncer.domain.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import br.com.eits.syncer.application.ApplicationHolder;

/**
 * Created by eits on 26/04/17.
 */

public class Watcher<T>
{
    /**
     *
     */
    public static final List<Watcher> watchers = new ArrayList<>();

    /**
     *
     */
    private static ActivityManager activityManager;

    /**
     *
     */
    static
    {
        activityManager = ( ActivityManager ) ApplicationHolder.CONTEXT.getSystemService( Context.ACTIVITY_SERVICE );
    }

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
    private Callable<T> function;

    /**
     *
     */
    private IHandler<T> handler;

    /**
     *
     */
    private Activity activity;

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/

    /**
     * @param function
     * @param handler
     * @param activity
     */
    public Watcher( Callable<T> function, IHandler<T> handler, Activity activity )
    {
        this.id = System.currentTimeMillis();
        this.function = function;
        this.handler = handler;
        this.activity = activity;
    }

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/

    /**
     *
     */
    public void execute()
    {
        //if activity is destroyed, there is no need for watcher to exists
        if( this.activity.isDestroyed() )
        {
            removeWatcher( this.id );
            return;
        }

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
     * @param watcherId
     */
    public synchronized static void removeWatcher( Long watcherId )
    {
        Watcher watcherToRemove = null;
        for( Watcher watcher : watchers )
        {
            if( watcher.getId().equals( watcherId ) )
            {
                watcherToRemove = watcher;
                break;
            }
        }

        if( watcherToRemove != null ) watchers.remove( watcherToRemove );
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Watcher<T> watcher = (Watcher<T>) o;

        return id.equals( watcher.id );

    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /*-------------------------------------------------------------------
	 * 		 				GETTERS AND SETTERS
	 *-------------------------------------------------------------------*/

    /**
     *
     * @param id
     */
    public void setId( Long id ) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    public Long getId() {
        return id;
    }

    /**
     *
     * @return
     */
    public Activity getActivity() {
        return activity;
    }

    /**
     *
     * @param activity
     */
    public void setActivity( Activity activity ) {
        this.activity = activity;
    }

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

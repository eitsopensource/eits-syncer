package br.com.eits.androidsyncer.application.background;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Define a Service that returns an IBinder for the sync adapter class, allowing the sync adapter framework to call onPerformSync().
 */
public class SyncBackgroundService extends Service
{
    /*-------------------------------------------------------------------
    * 		 					ATTRIBUTES
    *-------------------------------------------------------------------*/
    /**
     *
     */
    private static SyncAdapter syncAdapter = null;
    /**
     *  Object to use as a thread-safe lock
     */
    private static final Object lock = new Object();

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
    /**
     * Create the sync adapter as a singleton.
     * Set the sync adapter as syncable
     * Disallow parallel syncs
     */
    @Override
    public void onCreate()
    {
        synchronized( lock )
        {
            if ( syncAdapter == null )
            {
                this.syncAdapter = new SyncAdapter( super.getApplicationContext(), true );
            }
        }
    }

    /**
     * Return an object that allows the system to invoke the sync adapter.
     *
     * Get the object that allows external processes
     * to call onPerformSync(). The object is created in the base class code when the SyncAdapter constructors call super()
     */
    @Override
    public IBinder onBind(Intent intent)
    {
        return this.syncAdapter.getSyncAdapterBinder();
    }
}
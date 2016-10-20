package br.com.eits.androidsyncer.application.background;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import br.com.eits.androidsyncer.application.security.StubAccountAuthenticator;

/**
 * A bound Service that instantiates the authenticator
 * when started.
 */
public class AuthenticatorBackgroundService extends Service
{
    /*-------------------------------------------------------------------
    * 		 					ATTRIBUTES
    *-------------------------------------------------------------------*/
    /**
     *
     */
    private StubAccountAuthenticator accountAuthenticator;

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    @Override
    public void onCreate()
    {
        this.accountAuthenticator = new StubAccountAuthenticator(this);
    }

    /**
     * When the system binds to this Service to make the RPC call return the authenticator's IBinder.
     *
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent)
    {
        return this.accountAuthenticator.getIBinder();
    }
}

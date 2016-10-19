package br.com.eits.androidsyncer.application.security;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/*
 * Implement AbstractAccountAuthenticator and stub out all of its methods
 */
public class StubAccountAuthenticator extends AbstractAccountAuthenticator
{
    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     *
     * @param context
     */
    public StubAccountAuthenticator(Context context)
    {
        super(context);
    }

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
    /**
     *
     * @param response
     * @param accountType
     * @return
     */
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType)
    {
        Log.i(this.getClass().getSimpleName(), "editProperties");
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param response
     * @param accountType
     * @param authTokenType
     * @param requiredFeatures
     * @param options
     * @return
     * @throws NetworkErrorException
     */
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException
    {
        Log.i(this.getClass().getSimpleName(), "addAccount");
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param response
     * @param account
     * @param options
     * @return
     * @throws NetworkErrorException
     */
    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException
    {
        Log.i(this.getClass().getSimpleName(), "confirmCredentials");
        return null;
    }

    /**
     *
     * @param response
     * @param account
     * @param authTokenType
     * @param options
     * @return
     * @throws NetworkErrorException
     */
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException
    {
        Log.i(this.getClass().getSimpleName(), "getAuthToken");
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param authTokenType
     * @return
     */
    @Override
    public String getAuthTokenLabel(String authTokenType)
    {
        Log.i(this.getClass().getSimpleName(), "getAuthTokenLabel");
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param response
     * @param account
     * @param authTokenType
     * @param options
     * @return
     * @throws NetworkErrorException
     */
    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException
    {
        Log.i(this.getClass().getSimpleName(), "updateCredentials");
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param response
     * @param account
     * @param features
     * @return
     * @throws NetworkErrorException
     */
    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException
    {
        Log.i(this.getClass().getSimpleName(), "hasFeatures");
        throw new UnsupportedOperationException();
    }
}

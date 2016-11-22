package br.com.eits.syncer.application;

import android.content.Context;

import java.lang.reflect.Method;

/**
 *
 */
public class ApplicationHolder
{
    public static final Context CONTEXT;

    static
    {
        try
        {
            final Class<?> clazz = Class.forName("android.app.ActivityThread");
            final Method method = clazz.getDeclaredMethod("currentApplication");

            CONTEXT = (Context) method.invoke(null);
        }
        catch (Throwable ex)
        {
            throw new AssertionError(ex);
        }
    }
}
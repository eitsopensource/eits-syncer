package br.com.eits.androidsyncer.infrastructure.syncable;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import br.com.eits.androidsyncer.domain.entity.Revision;

/**
 *
 */
class RevisionDao
{
    /**
     *
     */
    private static final String FILE_NAME = "sync.db";

    /**
     *
     */
    private static final Context CONTEXT;

    static
    {
        try
        {
            final Class<?> clazz = Class.forName("android.app.ActivityThread");
            final Method method = clazz.getDeclaredMethod("currentApplication");

            CONTEXT = (Context) method.invoke(null);

            boolean found = false;
            final String[] files = CONTEXT.fileList();
            for ( int i = 0; i < files.length; i++ )
            {
                if ( files[i] == FILE_NAME )
                {
                    found = true;
                }
            }

            if ( !found )
            {
                final FileOutputStream fileOutputStream = CONTEXT.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
                final ObjectOutputStream objectOutputStream = new ObjectOutputStream( fileOutputStream );
                objectOutputStream.writeObject( new ArrayList<Revision>() );
                objectOutputStream.close();
            }
        }
        catch (Throwable ex)
        {
            throw new AssertionError(ex);
        }
    }

    /*-------------------------------------------------------------------
    * 		 					ATTRIBUTES
    *-------------------------------------------------------------------*/

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    public RevisionDao()
    {
    }

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
    /**
     *
     * @return
     */
    public void save( List<Revision> revisions )
    {
        try
        {
            final FileOutputStream fileOutputStream = CONTEXT.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream( fileOutputStream );
            objectOutputStream.writeObject( revisions );
            objectOutputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @return
     */
    public List<Revision> list()
    {
        try
        {
            final FileInputStream fileInputStream = CONTEXT.openFileInput(FILE_NAME);
            final ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            final List<Revision> revisions = (List<Revision>) objectInputStream.readObject();
            return revisions;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
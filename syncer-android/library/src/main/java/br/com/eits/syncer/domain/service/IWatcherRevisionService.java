package br.com.eits.syncer.domain.service;

import java.util.List;
import java.util.Observer;

/**
 * Created by rodrigo.p.fraga on 19/04/17.
 */
public interface IWatcherRevisionService<T>
{
    /**
     *
     * @param handler
     */
    public void onSyncronizeFinished( IHandler<Boolean> handler );

    /**
     *
     * @param entityId
     * @param handler
     */
    public void findByEntityId( long entityId, IHandler<T> handler );

    /**
     *
     * @param handler
     */
    public void listAll( IHandler<List<T>> handler );

    /**
     *
     * @param handle
     */
    public void query( IQueryRevisionService<T> queryRevisionService, IHandler<List<T>> handle );
}

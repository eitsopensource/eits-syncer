package br.com.eits.syncer.domain.service;

import java.util.List;
import java.util.Observer;

/**
 * Created by rodrigo.p.fraga on 19/04/17.
 */
public interface IObservableRevisionService<T>
{
    /**
     *
     * @param entityId
     * @param handler
     * @return
     */
    public T findById(long entityId, Observer handler);

    /**
     *
     * @param handler
     * @return
     */
    public List<T> listAll(Observer handler);

    /**
     *
     * @param filters
     * @param handler
     * @return
     */
    public List<T> listByFilters(String filters, Observer handler);
}

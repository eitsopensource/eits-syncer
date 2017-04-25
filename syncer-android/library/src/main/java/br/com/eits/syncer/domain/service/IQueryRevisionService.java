package br.com.eits.syncer.domain.service;

import java.util.List;

/**
 * Created by rodrigo.p.fraga on 19/04/17.
 */
public interface IQueryRevisionService<T>
{
    /**
     *
     * @return
     */
    public List<T> list();
    /**
     *
     * @param field
     * @param value
     * @return
     */
    public IQueryRevisionService where( String field, String value );
    /**
     *
     * @param joinEntity
     * @return
     */
    public IQueryRevisionService join( Class<?> joinEntity, long joinEntityId );
    /**
     *
     * @return
     */
    public IQueryRevisionService filterBy( String filters );
    /**
     *
     * @return
     */
    public IQueryRevisionService and();
    /**
     *
     * @return
     */
    public IQueryRevisionService or();
}

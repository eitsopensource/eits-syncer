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
     * @return
     */
    public T singleResult();
    /**
     *
     * @param field
     * @param value
     * @return
     */
    public IQueryRevisionService where( String field, Object value );
    /**
     *
     * @param field
     * @param value
     * @param operator
     * @return
     */
    public IQueryRevisionService where( String field, Object value, String operator );

    /**
     *
     * @param field
     * @return
     */
    public IQueryRevisionService where( String field );

    /**
     *
     * @param value
     * @return
     */
    public IQueryRevisionService eq( Object value );

    /**
     *
     * @param value
     * @return
     */
    public IQueryRevisionService gt( Object value );

    /**
     *
     * @param value
     * @return
     */
    public IQueryRevisionService lt( Object value );

    /**
     *
     * @param value
     * @return
     */
    public IQueryRevisionService goe( Object value );

    /**
     *
     * @param value
     * @return
     */
    public IQueryRevisionService loe( Object value );

    /**
     *
     * @param field
     * @param castAs
     * @return
     */
    public IQueryRevisionService castAs( String field, String castAs );
    /**
     *
     * @param field
     * @param value
     * @return
     */
    public IQueryRevisionService whereLike( String field, String value );
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
    public IQueryRevisionService begin();
    /**
     *
     * @return
     */
    public IQueryRevisionService end();
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

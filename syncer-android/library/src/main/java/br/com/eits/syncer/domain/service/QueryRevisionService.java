package br.com.eits.syncer.domain.service;

import java.util.List;

/**
 * Created by rodrigo.p.fraga on 19/04/17.
 */
public class QueryRevisionService <T> extends RevisionService<T> implements IQueryRevisionService<T>
{
    /*-------------------------------------------------------------------
    * 		 					ATTRIBUTES
    *-------------------------------------------------------------------*/
    /**
     * @param entityClass
     */
    public QueryRevisionService( Class<T> entityClass )
    {
        super(entityClass);
    }

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     * @param field
     * @param value
     * @return
     */
    @Override
    public IQueryRevisionService where(String field, String value)
    {
        return this;
    }

    /**
     * @param joinEntity
     * @return
     */
    @Override
    public IQueryRevisionService join(Class<?> joinEntity)
    {
        return this;
    }

    /**
     * @return
     */
    @Override
    public List<T> list()
    {
        return super.listAll();
    }
}

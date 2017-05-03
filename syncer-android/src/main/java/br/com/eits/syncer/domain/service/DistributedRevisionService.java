package br.com.eits.syncer.domain.service;


import br.com.eits.syncer.domain.entity.Service;

/**
 *
 */
public class DistributedRevisionService<T> extends RevisionService<T> implements IDistributedRevisionService<T>
{
    /*-------------------------------------------------------------------
    * 		 					ATTRIBUTES
    *-------------------------------------------------------------------*/
    /**
     *
     */
    protected final Service service;

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    public DistributedRevisionService( Class<T> entityClass, Service service )
    {
        super(entityClass);
        this.service = service;
    }

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
}
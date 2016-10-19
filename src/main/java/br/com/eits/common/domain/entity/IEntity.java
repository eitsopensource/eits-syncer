package br.com.eits.common.domain.entity;

import java.io.Serializable;

/**
 * @author rodrigo@eits.com.br
 * @since 22/11/2012
 * @version 1.0
 */
public interface IEntity<ID extends Serializable> extends Serializable
{
	/*-------------------------------------------------------------------
	 * 		 				GETTERS AND SETTERS
	 *-------------------------------------------------------------------*/
	/**
	 * 
	 * @return
	 */
	public ID getId();

	/**
	 * 
	 * @param id
	 */
	public void setId( ID id );
}

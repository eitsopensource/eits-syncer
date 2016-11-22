package br.com.eits.syncer.domain.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author rodrigo.p.fraga
 */
public class EntityUpdatedId
{
	/*-------------------------------------------------------------------
	 *				 		     ATTRIBUTES
	 *-------------------------------------------------------------------*/
	/**
	 * 
	 */
	private Object oldId;
	/**
	 * 
	 */
	private Object newId;
	/**
	 * 
	 */
	private Object entity;

	/**
	 * 
	 * @param oldId
	 * @param newId
	 * @param entity
	 */
	@JsonCreator
	public EntityUpdatedId( @JsonProperty("oldId") Object oldId, @JsonProperty("newId") Object newId, @JsonProperty("entity") Object entity )
	{
		this.oldId = oldId;
		this.newId = newId;
		this.entity = entity;
	}

	/**
	 * 
	 * @return
	 */
	public Object getOldId()
	{
		return oldId;
	}

	/**
	 * 
	 * @return
	 */
	public Object getNewId()
	{
		return newId;
	}

	/**
	 * 
	 * @return
	 */
	public Object getEntity()
	{
		return entity;
	}
}
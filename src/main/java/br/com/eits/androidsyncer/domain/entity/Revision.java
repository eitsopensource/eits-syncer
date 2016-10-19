/**
 * 
 */
package br.com.eits.androidsyncer.domain.entity;

import java.io.Serializable;

/**
 *
 */
public class Revision implements Serializable
{

	/**
	 *
	 */
	private static final long serialVersionUID = 2518817552731435286L;

	/*-------------------------------------------------------------------
	 *				 		     ATTRIBUTES
	 *-------------------------------------------------------------------*/
	/**
	 *
	 */
	private Long id;
	/**
	 * 
	 */
	private String entityClassName;
	/**
	 *
	 */
	private Serializable entityId;
	/**
	 *
	 */
	private RevisionType type;

	/*-------------------------------------------------------------------
	 *				 		     CONSTRUCTORS
	 *-------------------------------------------------------------------*/

	/*-------------------------------------------------------------------
	 *				 		     BEHAVIORS
	 *-------------------------------------------------------------------*/
	/**
	 *
	 * @param o
	 * @return
     */
	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Revision revision = (Revision) o;

		if (id != null ? !id.equals(revision.id) : revision.id != null) return false;
		if (entityClassName != null ? !entityClassName.equals(revision.entityClassName) : revision.entityClassName != null)
			return false;
		if (entityId != null ? !entityId.equals(revision.entityId) : revision.entityId != null)
			return false;
		return type == revision.type;
	}

	/**
	 *
	 * @return
     */
	@Override
	public int hashCode()
	{
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (entityClassName != null ? entityClassName.hashCode() : 0);
		result = 31 * result + (entityId != null ? entityId.hashCode() : 0);
		result = 31 * result + (type != null ? type.hashCode() : 0);
		return result;
	}

	/*-------------------------------------------------------------------
    *				 		   GETTERS AND SETTERS
    *-------------------------------------------------------------------*/

	/**
	 *
	 * @return
     */
	public Long getId()
	{
		return id;
	}

	/**
	 *
	 * @param id
     */
	public void setId(Long id)
	{
		this.id = id;
	}

	/**
	 *
	 * @return
     */
	public String getEntityClassName()
	{
		return entityClassName;
	}

	/**
	 *
	 * @param entityClassName
     */
	public void setEntityClassName(String entityClassName)
	{
		this.entityClassName = entityClassName;
	}

	/**
	 *
	 * @return
     */
	public Serializable getEntityId()
	{
		return entityId;
	}

	/**
	 *
	 * @param entityId
     */
	public void setEntityId(Serializable entityId)
	{
		this.entityId = entityId;
	}

	/**
	 *
	 * @return
     */
	public RevisionType getType()
	{
		return type;
	}

	/**
	 *
	 * @param type
     */
	public void setType(RevisionType type)
	{
		this.type = type;
	}
}

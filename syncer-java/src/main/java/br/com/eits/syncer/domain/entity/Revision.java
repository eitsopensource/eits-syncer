package br.com.eits.syncer.domain.entity;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class Revision<T> implements Serializable
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
	private Long revisionNumber;
	/**
	 *
	 */
	private Long revisionDate;
	/**
	 *
	 */
	private Boolean synced;
	/**
	 *
	 */
	private RevisionType type;
	/**
	 *
	 */
	private T entity;

	/**
	 *
	 */
	private String entityId;
	/**
	 *
	 */
	private String entityClassName;

	/*-------------------------------------------------------------------
	 *				 		     CONSTRUCTORS
	 *-------------------------------------------------------------------*/
	/**
	 * 
	 * @param entity
	 * @param type
	 */
	@JsonCreator
	public Revision( @JsonProperty("entity") T entity, @JsonProperty("type") RevisionType type )
	{
		this.revisionDate = Calendar.getInstance().getTimeInMillis();
		this.entity = entity;
		this.entityClassName = entity.getClass().getName();
		this.type = type;
		this.synced = false;
	}

	/*-------------------------------------------------------------------
	 *				 		     BEHAVIORS
	 *-------------------------------------------------------------------*/

	/**
	 *
	 * @param o
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean equals( Object o )
	{
		if ( this == o ) return true;
		if ( o == null || getClass() != o.getClass() ) return false;

		final Revision<T> revision = ( Revision<T> ) o;
		return Objects.equals( revision, revision.revisionDate ) && Objects.equals( synced, revision.synced ) && type == revision.type && Objects.equals( entity, revision.entity ) && Objects.equals( entityClassName, revision.entityClassName );
	}

	/**
	 *
	 * @return
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash( revisionDate, revisionNumber, synced, type, entity, entityClassName );
	}

	/*-------------------------------------------------------------------
	 *				 		   GETTERS AND SETTERS
	 *-------------------------------------------------------------------*/
	/**
	 *
	 * @return
	 */
	public String getEntityClassName()
	{
		return this.entityClassName;
	}

	/**
	 * 
	 * @param entityClassName
	 */
	public void setEntityClassName( String entityClassName )
	{
		this.entityClassName = entityClassName;
	}

	/**
	 *
	 * @return
	 */
	public RevisionType getType()
	{
		return this.type;
	}

	/**
	 * 
	 */
	public void setEntity( T entity )
	{
		this.entity = entity;
	}

	/**
	 *
	 * @return
	 */
	public T getEntity()
	{
		return this.entity;
	}

	/**
	 *
	 * @return
	 */
	public Boolean getSynced()
	{
		return this.synced;
	}

	/**
	 *
	 * @param synced
	 */
	public void setSynced( Boolean synced )
	{
		this.synced = synced;
	}

	/**
	 * @param revisionDate
	 */
	public void setRevisionDate( Long revisionDate )
	{
		this.revisionDate = revisionDate;
	}

	/**
	 * @return the id
	 */
	public Long getRevisionDate()
	{
		return this.revisionDate;
	}

	/**
	 * @return the entityId
	 */
	public String getEntityId()
	{
		return entityId;
	}

	/**
	 * @param entityId the entityId to set
	 */
	public void setEntityId( String entityId )
	{
		this.entityId = entityId;
	}

	/**
	 * @return
	 */
	public Long getRevisionNumber()
	{
		return revisionNumber;
	}

	/**
	 * @param id
	 */
	public void setRevisionNumber( Long revisionNumber )
	{
		this.revisionNumber = revisionNumber;
	}
}

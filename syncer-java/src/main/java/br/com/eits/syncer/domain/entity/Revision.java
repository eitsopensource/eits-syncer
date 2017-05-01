package br.com.eits.syncer.domain.entity;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Calendar;

import javax.persistence.Id;

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
	 * Identificação da revisão local no cliente.
	 */
	private Long id;
	/**
	 * Revisão mantida pelo servidor
	 */
	private Long revisionNumber;
	/**
	 * Se a revisão local já foi sincronizada ou não.
	 */
	private Boolean synced;
	/**
	 * Tipo da Revisão
	 */
	private RevisionType type;
	/**
	 * Instancia da entidade persistida
	 */
	private T entity;
	/**
	 * Fullclassname da entidade persistida.
	 */
	private String entityClassName;

	/**
	 * Valor da id da entidade
	 */
	private String entityId;

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
		this.id = System.currentTimeMillis();
		this.type = type;
		this.synced = false;
		this.entity = entity;

		this.extractEntity();
	}

	/**
	 * 
	 * @param id
	 * @param entity
	 * @param type
	 */
	public Revision( long id, T entity, RevisionType type )
	{
		this( entity, type );
		this.id = id;
	}

	/*-------------------------------------------------------------------
	 *				 		     BEHAVIORS
	 *-------------------------------------------------------------------*/
	/**
	 * 
	 */
	private void extractEntity()
	{
		this.entityClassName = this.entity.getClass().getName();

		try
		{
			final Field entityIdField = extractEntityIdField( this.entity.getClass() );
			entityIdField.setAccessible( true );
			final Serializable entityId = entityIdField.get( this.entity ) != null ? entityIdField.get( this.entity ).toString() : Calendar.getInstance().getTimeInMillis();
			entityIdField.set( this.entity, new Long( entityId.toString() ) );
			this.entityId = entityId.toString();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			throw new IllegalStateException( "Can not extract entity." );
		}
	}

	/**
	 * 
	 * @return
	 */
	public static Field extractEntityIdField( Class<?> entityClass )
	{
		if ( entityClass == null ) throw new IllegalArgumentException( "Entity Class can not be null." );

		try
		{
			Class<?> targetClass = entityClass;

			do
			{
				final Field[] fields = targetClass.getDeclaredFields();

				for ( Field field : fields )
				{
					if ( field.getAnnotation( Id.class ) != null )
					{
						field.setAccessible( true );
						return field;
					}
				}

				targetClass = targetClass.getSuperclass();
			}
			while ( targetClass != null && targetClass != Object.class );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}

		throw new IllegalStateException( "The entity must have an @Id annotation in an attribute." );
	}

	/**
	 * 
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( entity == null ) ? 0 : entity.hashCode() );
		result = prime * result + ( ( entityClassName == null ) ? 0 : entityClassName.hashCode() );
		result = prime * result + ( ( entityId == null ) ? 0 : entityId.hashCode() );
		result = prime * result + ( ( id == null ) ? 0 : id.hashCode() );
		result = prime * result + ( ( revisionNumber == null ) ? 0 : revisionNumber.hashCode() );
		result = prime * result + ( ( synced == null ) ? 0 : synced.hashCode() );
		result = prime * result + ( ( type == null ) ? 0 : type.hashCode() );
		return result;
	}

	/**
	 * 
	 */
	@Override
	public boolean equals( Object obj )
	{
		if ( this == obj ) return true;
		if ( obj == null ) return false;
		if ( getClass() != obj.getClass() ) return false;
		final Revision<?> other = ( Revision<?> ) obj;
		if ( entity == null )
		{
			if ( other.entity != null ) return false;
		}
		else if ( !entity.equals( other.entity ) ) return false;
		if ( entityClassName == null )
		{
			if ( other.entityClassName != null ) return false;
		}
		else if ( !entityClassName.equals( other.entityClassName ) ) return false;
		if ( entityId == null )
		{
			if ( other.entityId != null ) return false;
		}
		else if ( !entityId.equals( other.entityId ) ) return false;
		if ( id == null )
		{
			if ( other.id != null ) return false;
		}
		else if ( !id.equals( other.id ) ) return false;
		if ( revisionNumber == null )
		{
			if ( other.revisionNumber != null ) return false;
		}
		else if ( !revisionNumber.equals( other.revisionNumber ) ) return false;
		if ( synced == null )
		{
			if ( other.synced != null ) return false;
		}
		else if ( !synced.equals( other.synced ) ) return false;
		if ( type != other.type ) return false;
		return true;
	}

	/*-------------------------------------------------------------------
	 *				 		   GETTERS AND SETTERS
	 *-------------------------------------------------------------------*/

	/**
	 * @return the id
	 */
	public Long getId()
	{
		return this.id;
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
	public String getEntityClassName()
	{
		return this.entityClassName;
	}

	/**
	 * 
	 * @return
	 */
	public String getEntityId()
	{
		return this.entityId;
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
}

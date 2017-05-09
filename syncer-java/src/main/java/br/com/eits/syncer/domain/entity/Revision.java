package br.com.eits.syncer.domain.entity;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Objects;

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
	/**
	 * Identificador do serviço que a revisão é mantida
	 */
	private String serviceName;

	/*-------------------------------------------------------------------
	 *				 		     CONSTRUCTORS
	 *-------------------------------------------------------------------*/

	/**
	 *
	 * @param entity
	 * @param type
	 * @param serviceName
	 */
	@JsonCreator
	public Revision( @JsonProperty("entity") T entity, @JsonProperty("type") RevisionType type, @JsonProperty("serviceName") String serviceName )
	{
		this.type = type;
		this.synced = false;
		this.entity = entity;
		this.serviceName = serviceName;
		this.extractEntity();
	}

	/**
	 *
	 * @param id
	 * @param entity
	 * @param entityId
	 * @param type
	 * @param serviceName
	 */
	public Revision( long id, T entity, String entityId, RevisionType type, String serviceName )
	{
		this.id = id;
		this.type = type;
		this.synced = false;
		this.entity = entity;
		this.serviceName = serviceName;
		this.entityId = entityId;
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
	 * @param o
	 * @return
	 */
	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Revision<?> revision = (Revision<?>) o;
		return Objects.equals(id, revision.id) &&
				Objects.equals(revisionNumber, revision.revisionNumber) &&
				Objects.equals(synced, revision.synced) &&
				type == revision.type &&
				Objects.equals(entity, revision.entity) &&
				Objects.equals(entityClassName, revision.entityClassName) &&
				Objects.equals(entityId, revision.entityId) &&
				Objects.equals(serviceName, revision.serviceName);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id, revisionNumber, synced, type, entity, entityClassName, entityId, serviceName);
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
	public String getServiceName()
	{
		return this.serviceName;
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
	 * @param revisionNumber
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

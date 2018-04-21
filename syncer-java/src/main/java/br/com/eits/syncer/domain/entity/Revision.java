package br.com.eits.syncer.domain.entity;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Objects;

import javax.persistence.Id;

import br.com.eits.syncer.infrastructure.jackson.RevisionDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 *
 */
@JsonDeserialize(using = RevisionDeserializer.class)
public class Revision<T extends SyncEntity> implements Serializable
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

	/**
	 * old entity ID
	 */
	private Long oldId;

	/*-------------------------------------------------------------------
	 *				 		     CONSTRUCTORS
	 *-------------------------------------------------------------------*/

	/**
	 * @param entity
	 * @param type
	 * @param serviceName
	 */
	public Revision( T entity, RevisionType type, String serviceName )
	{
		this.type = type;
		this.synced = false;
		this.entity = entity;
		this.serviceName = serviceName;
		this.extractEntity();
	}

	/**
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

	public Revision()
	{

	}

	/*-------------------------------------------------------------------
	 *				 		     BEHAVIORS
	 *-------------------------------------------------------------------*/

	/**
	 * @return
	 */
	public static Field extractEntityIdField( Class<?> entityClass )
	{
		if ( entityClass == null )
		{
			throw new IllegalArgumentException( "Entity Class can not be null." );
		}

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
	private void extractEntity()
	{
		this.entityClassName = this.entity.getClass().getName();

		try
		{
			final Field entityIdField = extractEntityIdField( this.entity.getClass() );
			entityIdField.setAccessible( true );
			final Serializable entityId = entityIdField.get( this.entity ) != null ? entityIdField.get( this.entity ).toString() : Calendar.getInstance().getTimeInMillis();
			entityIdField.set( this.entity, Long.valueOf( entityId.toString() ) );
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
	 */
	@Override
	public boolean equals( Object o )
	{
		if ( this == o )
		{
			return true;
		}
		if ( o == null || getClass() != o.getClass() )
		{
			return false;
		}
		Revision<?> revision = (Revision<?>) o;
		return Objects.equals( id, revision.id ) &&
				Objects.equals( revisionNumber, revision.revisionNumber ) &&
				Objects.equals( synced, revision.synced ) &&
				type == revision.type &&
				Objects.equals( entity, revision.entity ) &&
				Objects.equals( entityClassName, revision.entityClassName ) &&
				Objects.equals( entityId, revision.entityId ) &&
				Objects.equals( serviceName, revision.serviceName );
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( id, revisionNumber, synced, type, entity, entityClassName, entityId, serviceName );
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
	 * @param id
	 */
	public void setId( Long id )
	{
		this.id = id;
	}

	/**
	 * @return
	 */
	public RevisionType getType()
	{
		return this.type;
	}

	public void setType( RevisionType type )
	{
		this.type = type;
	}

	/**
	 * @return
	 */
	public T getEntity()
	{
		return this.entity;
	}

	public void setEntity( T entity )
	{
		this.entity = entity;
		this.extractEntity();
	}

	/**
	 * @return
	 */
	public String getEntityClassName()
	{
		return this.entityClassName;
	}

	public void setEntityClassName( String entityClassName )
	{
		this.entityClassName = entityClassName;
	}

	/**
	 * @return
	 */
	public String getServiceName()
	{
		return this.serviceName;
	}

	public void setServiceName( String serviceName )
	{
		this.serviceName = serviceName;
	}

	/**
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
	 * @return
	 */
	public Boolean getSynced()
	{
		return this.synced;
	}

	/**
	 * @param synced
	 */
	public void setSynced( Boolean synced )
	{
		this.synced = synced;
	}

	public Long getOldId()
	{
		return oldId;
	}

	public void setOldId( Long oldId )
	{
		this.oldId = oldId;
	}
}

/**
 * 
 */
package br.com.eits.androidsyncer.domain.entity;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Calendar;

import br.com.eits.syncer.domain.entity.RevisionType;

/**
 *
 */
@DatabaseTable(tableName = "_revision_")
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
	@DatabaseField(id = true, allowGeneratedIdInsert = false, canBeNull = false)
	private Long time;
	/**
	 * 
	 */
	@DatabaseField(canBeNull = false)
	private String entityClassName;
	/**
	 *
	 */
	@DatabaseField(canBeNull = false)
	private Boolean synced;
	/**
	 *
	 */
	@DatabaseField(canBeNull = false, dataType=DataType.SERIALIZABLE)
	private Serializable entityId;
	/**
	 *
	 */
	@DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING)
	private RevisionType type;

	/*-------------------------------------------------------------------
	 *				 		     CONSTRUCTORS
	 *-------------------------------------------------------------------*/
	/**
	 *
	 */
	public Revision()
	{
		this.time = Calendar.getInstance().getTimeInMillis();
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
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Revision revision = (Revision) o;

		if (time != null ? !time.equals(revision.time) : revision.time != null) return false;
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
		int result = time != null ? time.hashCode() : 0;
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
	public Long getTime()
	{
		return this.time;
	}

	/**
	 *
	 * @param time
     */
	public void setTime(Long time)
	{
		this.time = time;
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

	/**
	 *
	 * @return
     */
	public Boolean getSynced()
	{
		return synced;
	}

	/**
	 *
	 * @param synced
     */
	public void setSynced(Boolean synced)
	{
		this.synced = synced;
	}
}

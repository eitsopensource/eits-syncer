package br.com.eits.syncer.domain.entity;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author rodrigo.p.fraga
 */
public class SyncData
{
	/*-------------------------------------------------------------------
	 *				 		     ATTRIBUTES
	 *-------------------------------------------------------------------*/
	/**
	 * 
	 */
	private Long revision;
	/**
	 * 
	 */
	private Map<RevisionType, List<Object>> entities;

	/*-------------------------------------------------------------------
	 *				 		     CONSTRUCTOR
	 *-------------------------------------------------------------------*/
	/**
	 * 
	 * @param revision
	 * @param entities
	 */
	@JsonCreator
	public SyncData( @JsonProperty("revision") Long revision, @JsonProperty("entities") Map<RevisionType, List<Object>> entities )
	{
		this.revision = revision;
		this.entities = entities;
	}


	/*-------------------------------------------------------------------
	 *				 		     GETTERS AND SETTERS
	 *-------------------------------------------------------------------*/
	/**
	 * @return the entities
	 */
	public Map<RevisionType, List<Object>> getEntities()
	{
		return entities;
	}
	/**
	 * @return the revision
	 */
	public Long getRevision()
	{
		return revision;
	}
}
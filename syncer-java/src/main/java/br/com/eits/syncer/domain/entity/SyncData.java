package br.com.eits.syncer.domain.entity;

import java.util.LinkedHashMap;

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
	 * An ordered map of revision types and entities.
	 * The order is important to sync logic.
	 */
	private LinkedHashMap<RevisionType, Object> entitiesByRevision;

	/*-------------------------------------------------------------------
	 *				 		     CONSTRUCTOR
	 *-------------------------------------------------------------------*/
	/**
	 * 
	 * @param revision
	 * @param entities
	 */
	@JsonCreator
	public SyncData( @JsonProperty("revision") Long revision, @JsonProperty("entitiesByRevision") LinkedHashMap<RevisionType, Object> entitiesByRevision )
	{
		this.revision = revision;
		this.entitiesByRevision = entitiesByRevision;
	}


	/*-------------------------------------------------------------------
	 *				 		     GETTERS AND SETTERS
	 *-------------------------------------------------------------------*/
	/**
	 * @return the entities
	 */
	public LinkedHashMap<RevisionType, Object> getEntitiesByRevision()
	{
		return entitiesByRevision;
	}
	/**
	 * @return the revision
	 */
	public Long getRevision()
	{
		return revision;
	}
}
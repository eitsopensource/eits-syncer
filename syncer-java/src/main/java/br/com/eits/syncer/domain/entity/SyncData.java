package br.com.eits.syncer.domain.entity;

import java.util.List;

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
	private Long lastRevision;
	/**
	 * 
	 */
	private Integer version;
	/**
	 * An ordered revision list to sync logic.
	 */
	private List<Revision<?>> revisions;

	/*-------------------------------------------------------------------
	 *				 		     CONSTRUCTOR
	 *-------------------------------------------------------------------*/
	/**
	 * 
	 * @param revision
	 * @param entities
	 */
	@JsonCreator
	public SyncData( @JsonProperty("lastRevision") Long lastRevision, @JsonProperty("version") Integer version, @JsonProperty("revisions") List<Revision<?>> revisions )
	{
		this.lastRevision = lastRevision;
		this.version = version;
		this.revisions = revisions;
	}

	/*-------------------------------------------------------------------
	 *				 		     GETTERS AND SETTERS
	 *-------------------------------------------------------------------*/
	/**
	 * 
	 * @return
	 */
	public Long getLastRevision()
	{
		return this.lastRevision;
	}

	/**
	 * 
	 * @return
	 */
	public Integer getVersion()
	{
		return this.version;
	}

	/**
	 * 
	 * @return
	 */
	public List<Revision<?>> getRevisions()
	{
		return this.revisions;
	}
}
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
	private Long fromRevisionNumber;

	/**
	 * An ordered revision list to sync logic.
	 */
	private List<Object> revisions;

	/*-------------------------------------------------------------------
	 *				 		     CONSTRUCTOR
	 *-------------------------------------------------------------------*/
	
	/**
	 * @param fromRevisionNumber
	 * @param revisions
	 */
	@JsonCreator
	public SyncData( @JsonProperty("fromRevisionNumber") Long fromRevisionNumber, @JsonProperty("revisions") List<Object> revisions )
	{
		this.fromRevisionNumber = fromRevisionNumber;
		this.revisions = revisions;
	}

	/*-------------------------------------------------------------------
	 *				 		     GETTERS AND SETTERS
	 *-------------------------------------------------------------------*/
	
	/**
	 * @return
	 */
	public Long getFromRevisionNumber()
	{
		return this.fromRevisionNumber;
	}

	/**
	 * 
	 * @return
	 */
	public List<Object> getRevisions()
	{
		return this.revisions;
	}
}
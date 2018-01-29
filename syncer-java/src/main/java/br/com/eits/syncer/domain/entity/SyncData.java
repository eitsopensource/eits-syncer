package br.com.eits.syncer.domain.entity;

import java.util.List;

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
	private List<Revision<?>> revisions;

	/**
	 * Id da sincronização para paginação das revisões.
	 */
	private String transactionId;

	/**
	 * Indica se há mais dados para serem baixados do servidor.
	 */
	private int pageNumber;

	private int totalPages;

	/*-------------------------------------------------------------------
	 *				 		     CONSTRUCTOR
	 *-------------------------------------------------------------------*/
	/**
	 * @param fromRevisionNumber
	 * @param revisions
	 */
	public SyncData( long fromRevisionNumber, List<Revision<?>> revisions )
	{
		this.fromRevisionNumber = fromRevisionNumber;
		this.revisions = revisions;
	}

	public SyncData()
	{

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
	public List<Revision<?>> getRevisions()
	{
		return this.revisions;
	}

	public void setFromRevisionNumber( Long fromRevisionNumber )
	{
		this.fromRevisionNumber = fromRevisionNumber;
	}

	public void setRevisions( List<Revision<?>> revisions )
	{
		this.revisions = revisions;
	}

	public String getTransactionId()
	{
		return transactionId;
	}

	public void setTransactionId( String transactionId )
	{
		this.transactionId = transactionId;
	}

	public boolean isLastPage()
	{
		return pageNumber == totalPages;
	}

	public int getPageNumber()
	{
		return pageNumber;
	}

	public void setPageNumber( int pageNumber )
	{
		this.pageNumber = pageNumber;
	}

	public int getTotalPages()
	{
		return totalPages;
	}

	public void setTotalPages( int totalPages )
	{
		this.totalPages = totalPages;
	}
}
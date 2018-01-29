package br.com.eits.syncer.domain.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by eduardo on 24/01/2018.
 */

public class SyncTransaction
{
	private final String transactionId;

	private final int totalPages;

	@JsonCreator
	public SyncTransaction( @JsonProperty("transactionId") String transactionId, @JsonProperty("totalPages") int totalPages )
	{
		this.transactionId = transactionId;
		this.totalPages = totalPages;
	}

	public String getTransactionId()
	{
		return transactionId;
	}

	public int getTotalPages()
	{
		return totalPages;
	}
}

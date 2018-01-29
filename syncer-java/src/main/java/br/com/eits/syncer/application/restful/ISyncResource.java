package br.com.eits.syncer.application.restful;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import br.com.eits.syncer.domain.entity.SyncData;
import br.com.eits.syncer.domain.entity.SyncTransaction;

/**
 * @author rodrigo.p.fraga
 */
@Path("/syncer")
public interface ISyncResource
{
	/**
	 * @param remoteSyncData remote sync data
	 * @return local server sync data
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	SyncTransaction synchronize( SyncData remoteSyncData );

	/**
	 * @param transactionId transaction
	 * @param page          starting from 1
	 * @return revisions
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{txId}/{page}")
	SyncData getPagedData( @PathParam("txId") String transactionId, @PathParam("page") int page );

	@DELETE
	@Path("/{txId}")
	void endSynchronization( @PathParam("txId") String transactionId );
}

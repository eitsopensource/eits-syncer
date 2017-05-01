package br.com.eits.syncer.application.restful;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import br.com.eits.syncer.domain.entity.SyncData;

/**
 * 
 * @author rodrigo.p.fraga
 */
@Path("/syncer")
public interface ISyncResource
{
	/**
	 * 
	 * @param remoteSyncData
	 * @return local server sync data
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public SyncData syncronize( SyncData remoteSyncData );
}

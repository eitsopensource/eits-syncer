package br.com.eits.syncer.application.restful;

import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import br.com.eits.syncer.domain.entity.RevisionType;

/**
 * 
 * @author rodrigo.p.fraga
 */
@Path("/syncer")
public interface ISyncResource
{
	/**
	 * 
	 * @param entity
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<RevisionType, List<Object>> syncronize( Map<RevisionType, List<Object>> remoteEntities );
}

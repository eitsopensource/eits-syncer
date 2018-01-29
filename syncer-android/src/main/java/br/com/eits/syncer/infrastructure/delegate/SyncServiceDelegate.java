package br.com.eits.syncer.infrastructure.delegate;

import br.com.eits.syncer.domain.entity.SyncData;
import br.com.eits.syncer.domain.entity.SyncTransaction;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by eduardo on 18/01/2018.
 */
public interface SyncServiceDelegate
{
	@POST("syncer")
	Observable<SyncTransaction> synchronize( @Body SyncData syncData );

	@GET("syncer/{txId}/{page}")
	Observable<SyncData> getPagedData( @Path("txId") String transactionId, @Path("page") int page );

	@DELETE("syncer/{txId}")
	Observable<Void> endSynchronization( @Path("txId") String transactionId );
}

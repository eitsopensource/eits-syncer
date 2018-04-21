package br.com.eits.syncer.domain.entity;

/**
 * Created by eduardo on 05/04/18.
 */

public interface PostReceiveHook
{
	void afterInsert( Revision<?> revision );
}

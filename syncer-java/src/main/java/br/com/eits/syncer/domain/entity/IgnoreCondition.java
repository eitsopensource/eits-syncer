package br.com.eits.syncer.domain.entity;

public interface IgnoreCondition<T extends SyncEntity>
{
	boolean shouldIgnore( T entity );
}

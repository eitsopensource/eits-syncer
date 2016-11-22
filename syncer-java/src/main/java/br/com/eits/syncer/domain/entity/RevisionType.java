package br.com.eits.syncer.domain.entity;

/**
 *
 */
public enum RevisionType
{
    /**
     * Indicates that the entity was added (persisted) at that revision.
     */
    INSERT,
    /**
     * Indicates that the entity was modified (one or more of its fields) at that revision.
     */
    UPDATE,
    /**
     * Indicates that the entity was deleted (removed) at that revision.
     */
    REMOVE,
	/**
     * Indicates that the entity was remotely inserted but must update id.
     */
    UPDATE_ID;

    /**
     *
     * @param type
     */
    private RevisionType()
    {
    }
}

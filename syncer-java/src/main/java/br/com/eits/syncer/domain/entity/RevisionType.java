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
    
    /**
     * @param ordinal
     */
    public static RevisionType getRevisionTypeByOrdinalValue( int ordinal )
    {
    	switch( ordinal )
    	{
    		case 0:
    			return RevisionType.INSERT;
    		case 1:
    			return RevisionType.UPDATE;
    		case 2:
    			return RevisionType.REMOVE;
    		case 3:
    			return RevisionType.UPDATE_ID;
    		default:
    			new IllegalArgumentException( "No revision type found for ordinal" );
    	}
    	
    	return null;
    }
}

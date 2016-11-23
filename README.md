# eits-syncer

A simple sync mechanics to client in Android or iOS to servers in Java, Ruby.

### Android setup and usage
A basic setup would be like:

    public void setup()
    {
        //if the server needs authentication
        Syncer.withCredentials("user@email.com", "admin");
        //the host is mandatory
        Syncer.withURL("http://myhost:8080/api");
    }
    
Using entity classes annoted via ORMLite/JPA, you can:

    Syncer.forEntity(Customer.class).insert( customer );
    Syncer.forEntity(Customer.class).update( customer );
    Syncer.forEntity(Customer.class).save( customer );
    Syncer.forEntity(Customer.class).remove( 1L );
    Syncer.forEntity(Customer.class).findById( 1L );
    Syncer.forEntity(Plant.class).queryBuilder();
    
    
### Java Server Contract
Your java server must implements the interface:         

    br.com.eits.syncer.application.restful.ISyncResource
    
And must follow its contract. Example:

    public class SyncService implements ISyncResource
    {
    	@Override
    	public SyncData syncronize( SyncData remoteSyncData )
    	{
    	    //make your sync logic using the remoteSyncData.getEntities();
    	    ...
    	    final Map<RevisionType, List<Object>> localEntities = new HashMap<>();
    	    //create a list of entities to sync, using the remoteSyncData.getRevision()
    	    ...
    	    final long serverRevision = //return the server last revision;
    
    	    return new SyncData( serverRevision, localEntities );
    	}
    }
    
And that''s it :)
As developer, you just need to use Syncer.forEntity methods and the lib take care of rest. Considering the client battery, network etc.

Enjoy!

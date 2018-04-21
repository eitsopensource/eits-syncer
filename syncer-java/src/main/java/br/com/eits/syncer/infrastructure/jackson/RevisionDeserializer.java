package br.com.eits.syncer.infrastructure.jackson;

import java.io.IOException;

import br.com.eits.syncer.domain.entity.Revision;
import br.com.eits.syncer.domain.entity.RevisionType;
import br.com.eits.syncer.domain.entity.SyncEntity;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Created by eduardo on 31/01/2018.
 */

public class RevisionDeserializer extends StdDeserializer<Revision<?>>
{
	public RevisionDeserializer()
	{
		super( Revision.class );
	}

	@Override
	@SuppressWarnings("unchecked")
	public Revision<?> deserialize( JsonParser p, DeserializationContext ctxt ) throws IOException
	{
		try
		{
			JsonNode node = p.getCodec().readTree( p );
			Revision<SyncEntity> revision = new Revision<>();
			// setting other values first
			revision.setId( node.get( "id" ).asLong() );
			revision.setRevisionNumber( node.get( "revisionNumber" ).asLong() );
			revision.setSynced( node.get( "synced" ).asBoolean() );
			revision.setType( RevisionType.valueOf( node.get( "type" ).asText() ) );
			revision.setServiceName( node.get( "serviceName" ) != null ? node.get( "serviceName" ).asText() : null );
			// finding out what's our child's class name
			String entityClassName = node.get( "entityClassName" ) != null ? node.get( "entityClassName" ).asText() : null;
			revision.setEntityClassName( entityClassName );
			Long oldId = node.get( "oldId" ) == null ? null : node.get( "oldId" ).asLong();
			revision.setOldId( oldId );

			if ( entityClassName == null )
			{
				// hopefully this is isn't a reference because then we're screwed
				JsonNode childNode = node.get( "entity" );
				if ( childNode instanceof ObjectNode )
				{
					entityClassName = childNode.get( "@type" ).asText();
				}
			}
			Class<? extends SyncEntity> clazz = entityClassName == null ?
					SyncEntity.class :
					(Class<? extends SyncEntity>) Class.forName( entityClassName );
			JavaType javaType = ctxt.getConfig().getTypeFactory().constructType( clazz );
			JsonDeserializer<Object> deser = ctxt.findNonContextualValueDeserializer( javaType );
			JsonParser nextParser = node.get( "entity" ).traverse();
			nextParser.nextToken();
			SyncEntity entity = (SyncEntity) deser.deserialize( nextParser, ctxt );
			revision.setEntity( entity );
			return revision;
		}
		catch ( Exception e )
		{
			throw new IOException( e );
		}
	}
}

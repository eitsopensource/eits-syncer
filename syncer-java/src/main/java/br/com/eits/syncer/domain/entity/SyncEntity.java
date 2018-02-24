package br.com.eits.syncer.domain.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Created by eduardo on 31/01/2018.
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
public interface SyncEntity
{
}

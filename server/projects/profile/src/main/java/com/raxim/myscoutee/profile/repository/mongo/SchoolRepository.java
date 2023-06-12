package com.raxim.myscoutee.profile.repository.mongo;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.raxim.myscoutee.profile.data.document.mongo.School;

@RepositoryRestResource(
    collectionResourceRel = "schools",
    path = "schools"
)
public interface SchoolRepository extends MongoRepository<School, UUID> {
}

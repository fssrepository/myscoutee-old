package com.raxim.myscoutee.profile.repository.mongo;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.raxim.myscoutee.profile.data.document.mongo.Path;

@RepositoryRestResource(
    collectionResourceRel = "paths",
    path = "paths"
)
public interface PathRepository extends MongoRepository<Path, UUID> {

    // Add any additional methods or custom queries if needed
}

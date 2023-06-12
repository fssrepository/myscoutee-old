package com.raxim.myscoutee.profile.repository.mongo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.raxim.myscoutee.profile.data.document.mongo.Form;

@RepositoryRestResource(collectionResourceRel = "forms", path = "forms")
public interface FormRepository extends MongoRepository<Form, UUID> {

    @Query("{key: ?0}")
    Optional<Form> findFormByKey(String key);

    // Add any additional methods or custom queries if needed
}

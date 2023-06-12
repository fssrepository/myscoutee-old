package com.raxim.myscoutee.profile.repository.mongo;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.raxim.myscoutee.profile.data.document.mongo.Feedback;

@RepositoryRestResource(collectionResourceRel = "feedbacks", path = "feedbacks")
public interface FeedbackRepository extends MongoRepository<Feedback, UUID> {
    // Add any additional methods or custom queries if needed
}

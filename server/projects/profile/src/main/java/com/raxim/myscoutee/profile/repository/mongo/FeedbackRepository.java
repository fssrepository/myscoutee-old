package com.raxim.myscoutee.profile.repository.mongo;

import com.raxim.myscoutee.profile.data.document.mongo.Feedback;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.UUID;

@RepositoryRestResource(collectionResourceRel = "feedbacks", path = "feedbacks")
interface FeedbackRepository extends MongoRepository<Feedback, UUID>,
        QuerydslPredicateExecutor<Feedback> {

}
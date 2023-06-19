package com.raxim.myscoutee.profile.repository.mongo;

import java.util.UUID;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.User;

public class UserExtRepositoryImpl implements UserExtRepository {
    private final MongoTemplate mongoTemplate;

    public UserExtRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void addProfile(UUID userId, Profile profile) {
        Query query = new Query(Criteria.where("id").is(userId));
        Update update = new Update()
                .set("profile", profile)
                .addToSet("profiles", profile);

        mongoTemplate.upsert(query, update, User.class);
    }

}

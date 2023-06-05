package com.raxim.myscoutee.profile.repository.mongo;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.raxim.myscoutee.profile.data.document.mongo.Message;

@Repository
public interface MessageRepository extends MongoRepository<Message, UUID> {

    @Query("{lang: ?0}")
    Message findByLang(String lang);

}

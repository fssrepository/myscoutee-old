package com.raxim.myscoutee.profile.repository.mongo;

import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.raxim.myscoutee.profile.data.document.mongo.DBMessage;

public interface MessageRepository extends MongoRepository<DBMessage, UUID> {
    
}

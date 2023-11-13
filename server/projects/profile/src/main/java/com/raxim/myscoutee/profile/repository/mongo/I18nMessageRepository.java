package com.raxim.myscoutee.profile.repository.mongo;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.raxim.myscoutee.profile.data.document.mongo.I18nMessage;

public interface I18nMessageRepository extends MongoRepository<I18nMessage, UUID> {

    @Query("{lang: ?0}")
    I18nMessage findByLang(String lang);

    // Add any additional methods or custom queries if needed
}

package com.raxim.myscoutee.profile.repository.mongo;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.raxim.myscoutee.profile.data.document.mongo.ScheduleSetting;

@RepositoryRestResource(collectionResourceRel = "scheduleSettings", path = "scheduleSettings")
public interface ScheduleSettingRepository
        extends MongoRepository<ScheduleSetting, UUID>, QuerydslPredicateExecutor<ScheduleSetting> {

    @Query("{key: ?0}")
    ScheduleSetting findByKey(String key);
}

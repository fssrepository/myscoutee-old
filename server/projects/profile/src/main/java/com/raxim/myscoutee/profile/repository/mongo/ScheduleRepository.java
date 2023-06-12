package com.raxim.myscoutee.profile.repository.mongo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.raxim.myscoutee.profile.data.document.mongo.Schedule;

@RepositoryRestResource(
    collectionResourceRel = "schedules",
    path = "schedules"
)
public interface ScheduleRepository extends MongoRepository<Schedule, UUID> {

    @Query("{key: ?0}")
    Optional<Schedule> findByKey(String key);
}

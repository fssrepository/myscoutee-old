package com.raxim.myscoutee.profile.repository.mongo

import com.raxim.myscoutee.profile.data.document.mongo.Schedule
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(
    collectionResourceRel = "schedules",
    path = "schedules"
)
interface ScheduleRepository : MongoRepository<Schedule, UUID>,
    QuerydslPredicateExecutor<Schedule> {

    @Query("{key: ?0}")
    fun findByKey(key: String): Optional<Schedule>
}
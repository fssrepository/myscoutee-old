package com.raxim.myscoutee.profile.repository.mongo

import com.raxim.myscoutee.profile.data.document.mongo.ScheduleSetting
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(
    collectionResourceRel = "scheduleSettings",
    path = "scheduleSettings"
)
interface ScheduleSettingRepository : MongoRepository<ScheduleSetting, UUID>,
    QuerydslPredicateExecutor<ScheduleSetting> {

    @Query("{key: ?0}")
    fun findByKey(key: String): Optional<ScheduleSetting>
}
package com.raxim.myscoutee.profile.repository.mongo

import com.raxim.myscoutee.profile.data.document.mongo.Setting
import com.raxim.myscoutee.profile.data.document.mongo.User
import com.raxim.myscoutee.profile.data.dto.rest.Group
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(
    collectionResourceRel = "settings",
    path = "settings"
)
interface SettingRepository : MongoRepository<Setting, UUID>,
    QuerydslPredicateExecutor<Setting> {

    @Query("{profile: ?0, key: ?1}")
    fun findSettingByProfileAndKey(profileId: UUID, key: String): Setting?
}
package com.raxim.myscoutee.profile.repository.mongo

import com.raxim.myscoutee.profile.data.document.mongo.Token
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(
    collectionResourceRel = "tokens",
    path = "tokens"
)
interface TokenRepository : MongoRepository<Token, UUID>,
    QuerydslPredicateExecutor<Token> {

    @Query("{deviceKey: ?0}")
    fun findByDeviceId(deviceId: String): Token?

    @Query("{uuid: ?0}")
    fun findByUserIds(uuids: Array<UUID>): List<Token>
}
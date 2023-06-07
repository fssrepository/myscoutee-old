package com.raxim.myscoutee.profile.repository.mongo

import com.raxim.myscoutee.profile.data.document.mongo.Message
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MessageRepository : MongoRepository<Message, UUID> {

    @Query("{lang: ?0}")
    fun findByLang(lang: String): Message?

}
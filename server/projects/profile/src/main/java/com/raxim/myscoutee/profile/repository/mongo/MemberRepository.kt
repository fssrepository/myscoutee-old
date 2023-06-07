package com.raxim.myscoutee.profile.repository.mongo

import com.raxim.myscoutee.profile.data.document.mongo.Member
import com.raxim.myscoutee.profile.data.document.mongo.Path
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(
    collectionResourceRel = "members",
    path = "members"
)
interface MemberRepository : MongoRepository<Member, UUID>,
    QuerydslPredicateExecutor<Member>
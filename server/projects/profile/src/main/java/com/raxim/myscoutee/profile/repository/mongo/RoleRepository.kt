package com.raxim.myscoutee.profile.repository.mongo

import com.raxim.myscoutee.profile.data.document.mongo.Role
import com.raxim.myscoutee.profile.data.document.mongo.User
import com.raxim.myscoutee.profile.data.dto.rest.Car
import com.raxim.myscoutee.profile.data.dto.rest.Group
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RoleRepository : MongoRepository<Role, UUID> {

    @Query("{'profileId': ?0}")
    fun findRoleByProfile(profileId: UUID): List<Role>
}
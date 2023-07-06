package com.raxim.myscoutee.profile.repository.mongo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.raxim.myscoutee.profile.data.document.mongo.Role;

public interface RoleRepository extends MongoRepository<Role, UUID> {

    @Query("{'profileId': ?0}")
    List<Role> findRoleByProfile(UUID profileId);
}

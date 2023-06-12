package com.raxim.myscoutee.profile.repository.mongo;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.raxim.myscoutee.profile.data.document.mongo.Setting;

@RepositoryRestResource(
    collectionResourceRel = "settings",
    path = "settings"
)
public interface SettingRepository extends MongoRepository<Setting, UUID> {

    @Query("{profile: ?0, key: ?1}")
    Setting findSettingByProfileAndKey(
        @Param("profileId") UUID profileId, 
        @Param("key") String key);
}

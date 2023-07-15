package com.raxim.myscoutee.profile.repository.mongo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.raxim.myscoutee.profile.data.document.mongo.Topic;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;

@RepositoryRestResource(collectionResourceRel = "users", path = "users")
public interface UserRepository extends MongoRepository<User, UUID>, UserExtRepository {

        @Query("{email: ?0}")
        User findUserByEmail(String email);

        @Aggregation(pipeline = "findUsersByProfiles")
        List<User> findUsersByProfiles(
                        @Param("profileIds") List<UUID> profileIds);

        @Aggregation(pipeline = "findGroupsByUser")
        List<GroupDTO> findGroupsByUser(
                        @Param("userId") UUID userId,
                        @Param("param") PageParam pageParam);

        @Aggregation(pipeline = "findDeviceWithProfileStatusAll")
        List<Topic> findDeviceWithProfileStatusAll(
                        @Param("lastTime") String lastTime,
                        @Param("status") String[] status);
}

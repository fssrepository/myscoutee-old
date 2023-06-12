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

@RepositoryRestResource(collectionResourceRel = "users", path = "users")
public interface UserRepository extends MongoRepository<User, UUID> {

    @Query("{email: ?0}")
    User findUserByEmail(String email);

    @Aggregation(pipeline = "findGroupsByEmail")
    List<GroupDTO> findGroupsByEmail(
            @Param("email") String email,
            @Param("role") String role,
            @Param("isAdmin") boolean isAdmin,
            @Param("profile") UUID profile,
            @Param("limit") int limit,
            @Param("step") int step,
            @Param("offset") Object[] offset);

    @Aggregation(pipeline = "findAllGroupsByEmail")
    List<GroupDTO> findAllGroupsByEmail(String email);

    @Aggregation(pipeline = "findDeviceWithProfileStatusAll")
    List<Topic> findDeviceWithProfileStatusAll(
            @Param("lastTime") String lastTime,
            @Param("status") String[] status);
}

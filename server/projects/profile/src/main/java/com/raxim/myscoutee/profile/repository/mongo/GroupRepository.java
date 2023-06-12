package com.raxim.myscoutee.profile.repository.mongo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mongodb.client.model.geojson.Point;
import com.raxim.myscoutee.profile.data.document.mongo.Group;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;

@Repository
public interface GroupRepository extends MongoRepository<Group, UUID> {

    @Query("{system: true}")
    List<Group> findSystemGroups();

    @Query("{type: ?0, system: true}")
    Group findSystemGroupByType(String type);

    @Aggregation(pipeline = "findGroupByProfile")
    List<GroupDTO> findGroupByProfile(
            @Param("type") String type,
            @Param("loc") Point loc,
            @Param("limit") int limit,
            @Param("step") int step,
            @Param("uuids") List<UUID> uuids,
            @Param("offset") Object[] offset);

}

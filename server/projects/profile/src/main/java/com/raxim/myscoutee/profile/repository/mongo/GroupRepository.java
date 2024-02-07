package com.raxim.myscoutee.profile.repository.mongo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.mongodb.client.model.geojson.Point;
import com.raxim.myscoutee.profile.data.document.mongo.Group;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;

@RepositoryRestResource(collectionResourceRel = "groups", path = "groups")
public interface GroupRepository extends MongoRepository<Group, UUID> {

        @Query("{system: true}")
        List<Group> findSystemGroups();

        @Aggregation(pipeline = "findAllGroups")
        List<GroupDTO> findAllGroups(
                        @Param("param") PageParam pageParam,
                        @Param("loc") Point loc,
                        @Param("access") String access);

        @Aggregation(pipeline = "findRecGroups")
        List<GroupDTO> findRecGroups(
                        @Param("param") PageParam pageParam,
                        @Param("loc") Point loc,
                        @Param("groupId") UUID groupId);

        @Aggregation(pipeline = "findProfilesByGroup")
        List<ProfileDTO> findProfilesByGroup(
                        @Param("groupId") UUID groupId,
                        @Param("param") PageParam pageParam);

        @Aggregation(pipeline = "findEventsByGroup")
        List<EventDTO> findEventsByGroup(
                        @Param("groupId") UUID groupId,
                        @Param("param") PageParam pageParam);

}

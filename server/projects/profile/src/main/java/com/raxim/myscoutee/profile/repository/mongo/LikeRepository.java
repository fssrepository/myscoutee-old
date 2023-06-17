package com.raxim.myscoutee.profile.repository.mongo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.raxim.myscoutee.profile.data.document.mongo.Badge;
import com.raxim.myscoutee.profile.data.document.mongo.Like;
import com.raxim.myscoutee.profile.data.document.mongo.LikeGroup;
import com.raxim.myscoutee.profile.data.dto.rest.LikeDTO;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;

@RepositoryRestResource(collectionResourceRel = "likes", path = "likes")
public interface LikeRepository extends MongoRepository<Like, UUID> {

    @Aggregation(pipeline = "findByParty")
    List<LikeGroup> findByParty(@Param("currUser") UUID currUser, @Param("likes") List<LikeDTO> likes);

    @Aggregation(pipeline = "newLikesByProfile")
    List<Badge> newLikesByProfile(UUID profileId, String date);

    @Aggregation(pipeline = "findDoubleById")
    List<ProfileDTO> findDoubleById(
            @Param("currUser") UUID currUser,
            @Param("selectedUser") UUID selectedUser,
            @Param("limit") int limit,
            @Param("step") int step,
            @Param("status") String[] status,
            @Param("offset") Object[] offset);

    @Aggregation(pipeline = "findAll")
    List<LikeGroup> findAll(long lastIdx, long batchSize);

}

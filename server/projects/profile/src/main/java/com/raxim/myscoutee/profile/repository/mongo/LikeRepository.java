package com.raxim.myscoutee.profile.repository.mongo;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.raxim.myscoutee.profile.data.document.mongo.Badge;
import com.raxim.myscoutee.profile.data.document.mongo.Like;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;

@RepositoryRestResource(collectionResourceRel = "likes", path = "likes")
public interface LikeRepository extends MongoRepository<Like, UUID>,
        QuerydslPredicateExecutor<Like> {

    @Query("{$and: [{'createdBy.$id': ?0}, { 'from.$id' : { $in: [ ?1 ] }}, { 'to.$id' : { $in: [ ?2 ] }} ] }")
    List<Like> findByIds(UUID currUser, Set<UUID> from, Set<UUID> to);

    @Aggregation(pipeline = "newLikesByProfile")
    List<Badge> newLikesByProfile(UUID profileId, String date);

    @Aggregation(pipeline = "findDoubleById")
    List<ProfileDTO> findDoubleById(
            UUID currUser,
            UUID selectedUser,
            int limit,
            int step,
            @Param("status") String[] status,
            @Param("offset") Object[] offset);

    @Aggregation(pipeline = "findBothAll")
    List<Like> findBothAll(String lastTime, double type);

}

package com.raxim.myscoutee.profile.repository.mongo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.mongodb.client.model.geojson.Point;
import com.raxim.myscoutee.profile.data.document.mongo.Car;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.School;
import com.raxim.myscoutee.profile.data.dto.rest.CarDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.data.dto.rest.SchoolDTO;

@RepositoryRestResource(collectionResourceRel = "profiles", path = "profiles")
public interface ProfileRepository extends MongoRepository<Profile, UUID>,
        QuerydslPredicateExecutor<Profile> {

    @Aggregation(pipeline = "findProfile")
    List<ProfileDTO> findProfile(
            Point loc,
            @Param("offset") Object[] offset, // minDistance
            int limit,
            int step,
            UUID currentId,
            String gender,
            UUID groupId,
            @Param("type") double type,
            Integer score);

    @Aggregation(pipeline = "findProfileNoType")
    List<ProfileDTO> findProfileNoType(
            Point loc,
            @Param("offset") Object[] offset, // minDistance
            int limit,
            int step,
            UUID sProfileId,
            String gender,
            UUID groupId,
            UUID cProfileId, // curr
            @Param("type") double type);

    @Aggregation(pipeline = "findInvitationByProfile")
    List<EventDTO> findInvitationByProfile(
            UUID currentId,
            Point loc,
            int limit,
            int step,
            UUID group,
            @Param("offset") Object[] offset,
            @Param("type") double type);

    @Aggregation(pipeline = "findPeopleByProfile")
    @Deprecated
    List<ProfileDTO> findPeopleByProfile(
            UUID currentId,
            Point loc,
            int limit,
            int step,
            String gender,
            UUID groupId,
            @Param("offset") Object[] offset,
            @Param("type") double type);

    @Aggregation(pipeline = "findCarsByProfilePage")
    List<CarDTO> findCarsByProfile(
            UUID profileId,
            int limit,
            int step,
            @Param("offset") Object[] offset);

    @Aggregation(pipeline = "findCarByProfile")
    Optional<Car> findCarByProfile(UUID profileId, UUID carId);

    @Aggregation(pipeline = "findSchoolByProfile")
    Optional<School> findSchoolByProfile(UUID profileId, UUID schoolId);

    @Aggregation(pipeline = "findSchoolsByProfile")
    List<SchoolDTO> findSchoolsByProfile(
            UUID profileId,
            int limit,
            int step,
            @Param("offset") Object[] offset);

    @Aggregation(pipeline = "findAllProfiles")
    List<ProfileDTO> findAllProfiles(
            int limit,
            int step,
            @Param("offset") Object[] offset);

    @Aggregation(pipeline = "findProfilesByGroup")
    List<ProfileDTO> findProfilesByGroup(
            UUID profileId,
            UUID groupId,
            int limit,
            int step,
            @Param("offset") Object[] offset);
}

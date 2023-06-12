package com.raxim.myscoutee.profile.repository.mongo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
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
public interface ProfileRepository extends MongoRepository<Profile, UUID> {

    @Aggregation(pipeline = "findProfile")
    List<ProfileDTO> findProfile(
            @Param("loc") Point loc,
            @Param("offset") Object[] offset, // minDistance
            @Param("limit") int limit,
            @Param("step") int step,
            @Param("currentId") UUID currentId,
            @Param("gender") String gender,
            @Param("groupId") UUID groupId,
            @Param("type") double type,
            @Param("offset") Integer score);

    @Aggregation(pipeline = "findProfileNoType")
    List<ProfileDTO> findProfileNoType(
            @Param("loc") Point loc,
            @Param("offset") Object[] offset, // minDistance
            @Param("limit") int limit,
            @Param("step") int step,
            @Param("sProfileId") UUID sProfileId,
            @Param("gender") String gender,
            @Param("groupId") UUID groupId,
            @Param("cProfileId") UUID cProfileId, // curr
            @Param("type") double type);

    @Aggregation(pipeline = "findInvitationByProfile")
    List<EventDTO> findInvitationByProfile(
            @Param("currentId") UUID currentId,
            @Param("loc") Point loc,
            @Param("limit") int limit,
            @Param("step") int step,
            @Param("groupId") UUID groupId,
            @Param("offset") Object[] offset,
            @Param("type") double type);

    @Aggregation(pipeline = "findPeopleByProfile")
    @Deprecated
    List<ProfileDTO> findPeopleByProfile(
            @Param("currentId") UUID currentId,
            @Param("loc") Point loc,
            @Param("limit") int limit,
            @Param("step") int step,
            @Param("gender") String gender,
            @Param("groupId") UUID groupId,
            @Param("offset") Object[] offset,
            @Param("type") double type);

    @Aggregation(pipeline = "findCarsByProfilePage")
    List<CarDTO> findCarsByProfile(
            @Param("profileId") UUID profileId,
            @Param("limit") int limit,
            @Param("step") int step,
            @Param("offset") Object[] offset);

    @Aggregation(pipeline = "findCarByProfile")
    Optional<Car> findCarByProfile(UUID profileId, UUID carId);

    @Aggregation(pipeline = "findSchoolByProfile")
    Optional<School> findSchoolByProfile(UUID profileId, UUID schoolId);

    @Aggregation(pipeline = "findSchoolsByProfile")
    List<SchoolDTO> findSchoolsByProfile(
            @Param("profileId") UUID profileId,
            @Param("limit") int limit,
            @Param("step") int step,
            @Param("offset") Object[] offset);

    @Aggregation(pipeline = "findAllProfiles")
    List<ProfileDTO> findAllProfiles(
            @Param("limit") int limit,
            @Param("step") int step,
            @Param("offset") Object[] offset);

    @Aggregation(pipeline = "findProfilesByGroup")
    List<ProfileDTO> findProfilesByGroup(
            @Param("profileId") UUID profileId,
            @Param("groupId") UUID groupId,
            @Param("limit") int limit,
            @Param("step") int step,
            @Param("offset") Object[] offset);
}

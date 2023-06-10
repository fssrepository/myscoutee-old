package com.raxim.myscoutee.profile.repository.mongo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.mongodb.client.model.geojson.Point;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Token;
import com.raxim.myscoutee.profile.data.dto.rest.CodeDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventItemDTO;
import com.raxim.myscoutee.profile.data.dto.rest.FeedbackDTO;
import com.raxim.myscoutee.profile.data.dto.rest.MemberDTO;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;

@RepositoryRestResource(collectionResourceRel = "events", path = "events")
public interface EventRepository extends MongoRepository<Event, UUID>,
        QuerydslPredicateExecutor<Event> {

    @Query("{'status': 'P', 'ref.$id': ?0, 'info.members.profile.$id': ?1}")
    List<Event> findPendingEvents(UUID eventId, UUID profileId);

    @Query("{'status': { $in: :#{#status} }, 'ref.$id': { $in: :#{#refIds} } }")
    List<Event> findActiveEvents(
            @Param("status") String[] status,
            @Param("refIds") UUID[] refIds);

    @Aggregation(pipeline = "findEventsByStatus")
    List<EventDTO> findEventsByStatus(
            UUID group,
            int limit,
            @Param("offset") String[] offset,
            @Param("status") String status);

    @Aggregation(pipeline = "findEventsByProfile")
    List<EventDTO> findEventsByProfile(
            UUID currentId,
            Point loc,
            int limit,
            int step,
            UUID groupId,
            @Param("offset") Object[] offset);

    @Aggregation(pipeline = "findEventDown")
    List<EventDTO> findEventDown(
            UUID currentId,
            int limit,
            int step,
            String format,
            @Param("evtStatus") String[] eventStatus,
            @Param("offset") String[] offset,
            @Param("status") String status);

    @Aggregation(pipeline = "findEventUp")
    List<EventDTO> findEventUp(
            UUID currentId,
            int limit,
            int step,
            String format,
            @Param("evtStatus") String[] eventStatus,
            @Param("offset") String[] offset,
            @Param("status") String status);

    @Aggregation(pipeline = "findEventByMonth")
    List<EventDTO> findEventByMonth(
            UUID currentId,
            int limit,
            int step,
            String format,
            @Param("evtStatus") String[] eventStatus,
            @Param("until") Object until,
            @Param("offset") String[] offset,
            @Param("status") String status);

    @Aggregation(pipeline = "findProfileByEvent")
    List<ProfileDTO> findProfileByEvent(
            UUID currentId,
            int limit,
            int step,
            @Param("offset") Object[] offset,
            @Param("status") String status);

    @Aggregation(pipeline = "findItemsByEvent")
    List<EventItemDTO> findItemsByEvent(
            UUID eventId,
            int limit,
            int step,
            String format,
            UUID profileId,
            @Param("offset") Object[] offset);

    @Aggregation(pipeline = "findFeedbacksByEvent")
    List<FeedbackDTO> findFeedbacksByEvent(
            UUID eventId,
            int limit,
            int step,
            @Param("offset") Object[] offset);

    @Aggregation(pipeline = "findMembersByEvent")
    List<MemberDTO> findMembersByEvent(
            UUID eventId,
            int limit,
            int step,
            UUID profileId,
            @Param("status") String[] status,
            @Param("offset") Object[] offset);

    @Aggregation(pipeline = "findProfilesByPromotion")
    List<ProfileDTO> findProfilesByPromotion(
            UUID eventId,
            int limit,
            int step,
            UUID profileId,
            @Param("offset") Object[] offset);

    @Aggregation(pipeline = "findMemberByCode")
    Optional<MemberDTO> findMemberByCode(
            UUID eventId,
            String code);

    @Aggregation(pipeline = "findCodeByEvent")
    Optional<CodeDTO> findCodeByEvent(
            UUID eventId,
            UUID userUid);

    @Aggregation(pipeline = "findEventsByRated")
    List<EventDTO> findEventsByRated(
            UUID currentId,
            int limit,
            int step,
            String format,
            @Param("offset") Object[] offset);

    @Aggregation(pipeline = "findTokensByEvent")
    List<Token> findTokensByEvent(@Param("eventIds") UUID[] eventId);
}

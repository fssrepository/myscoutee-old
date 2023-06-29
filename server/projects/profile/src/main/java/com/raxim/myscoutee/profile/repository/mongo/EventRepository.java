package com.raxim.myscoutee.profile.repository.mongo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
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
public interface EventRepository extends MongoRepository<Event, UUID> {

        @Query("{'status': 'P', 'ref.$id': ?0, 'info.members.profile.$id': ?1}")
        List<Event> findPendingEvents(UUID eventId, UUID profileId);

        @Query("{'status': { $in: :#{#status} }, 'ref.$id': { $in: :#{#refIds} } }")
        List<Event> findActiveEvents(
                        @Param("status") String[] status,
                        @Param("refIds") UUID[] refIds);

        @Aggregation(pipeline = "findEventsByStatus")
        List<EventDTO> findEventsByStatus(
                        @Param("group") UUID group,
                        @Param("limit") int limit,
                        @Param("offset") String[] offset,
                        @Param("status") String status);

        @Aggregation(pipeline = "findEventsByProfile")
        List<EventDTO> findEventsByProfile(
                        @Param("currentId") UUID currentId,
                        @Param("loc") Point loc,
                        @Param("limit") int limit,
                        @Param("step") int step,
                        @Param("groupId") UUID groupId,
                        @Param("offset") Object[] offset);

        @Aggregation(pipeline = "findEventDown")
        List<EventDTO> findEventDown(
                        @Param("currentId") UUID currentId,
                        @Param("limit") int limit,
                        @Param("step") int step,
                        @Param("format") String format,
                        @Param("evtStatus") String[] eventStatus,
                        @Param("offset") String[] offset,
                        @Param("status") String status);

        @Aggregation(pipeline = "findEventUp")
        List<EventDTO> findEventUp(
                        @Param("currentId") UUID currentId,
                        @Param("limit") int limit,
                        @Param("step") int step,
                        @Param("format") String format,
                        @Param("evtStatus") String[] eventStatus,
                        @Param("offset") String[] offset,
                        @Param("status") String status);

        @Aggregation(pipeline = "findEventByMonth")
        List<EventDTO> findEventByMonth(
                        @Param("currentId") UUID currentId,
                        @Param("limit") int limit,
                        @Param("step") int step,
                        @Param("format") String format,
                        @Param("evtStatus") String[] eventStatus,
                        @Param("until") Object until,
                        @Param("offset") String[] offset,
                        @Param("status") String status);

        @Aggregation(pipeline = "findProfileByEvent")
        List<ProfileDTO> findProfileByEvent(
                        @Param("currentId") UUID currentId,
                        @Param("limit") int limit,
                        @Param("step") int step,
                        @Param("offset") Object[] offset,
                        @Param("status") String status);

        @Aggregation(pipeline = "findItemsByEvent")
        List<EventItemDTO> findItemsByEvent(
                        @Param("eventId") UUID eventId,
                        @Param("limit") int limit,
                        @Param("step") int step,
                        @Param("format") String format,
                        @Param("profileId") UUID profileId,
                        @Param("offset") Object[] offset);

        @Aggregation(pipeline = "findFeedbacksByEvent")
        List<FeedbackDTO> findFeedbacksByEvent(
                        @Param("eventId") UUID eventId,
                        @Param("limit") int limit,
                        @Param("step") int step,
                        @Param("offset") Object[] offset);

        @Aggregation(pipeline = "findMembersByEvent")
        List<MemberDTO> findMembersByEvent(
                        @Param("eventId") UUID eventId,
                        @Param("limit") int limit,
                        @Param("step") int step,
                        @Param("profileId") UUID profileId,
                        @Param("status") String[] status,
                        @Param("offset") Object[] offset);

        @Aggregation(pipeline = "findProfilesByPromotion")
        List<ProfileDTO> findProfilesByPromotion(
                        @Param("eventId") UUID eventId,
                        @Param("limit") int limit,
                        @Param("step") int step,
                        @Param("profileId") UUID profileId,
                        @Param("offset") Object[] offset);

        @Aggregation(pipeline = "findMemberByCode")
        Optional<MemberDTO> findMemberByCode(
                        @Param("eventId") UUID eventId,
                        @Param("code") String code);

        @Aggregation(pipeline = "findCodeByEvent")
        Optional<CodeDTO> findCodeByEvent(
                        @Param("eventId") UUID eventId,
                        @Param("userUid") UUID userUid);

        @Aggregation(pipeline = "findEventsByRated")
        List<EventDTO> findEventsByRated(
                        @Param("currentId") UUID currentId,
                        @Param("limit") int limit,
                        @Param("step") int step,
                        @Param("format") String format,
                        @Param("offset") Object[] offset);

        @Aggregation(pipeline = "findTokensByEvent")
        List<Token> findTokensByEvent(@Param("eventIds") UUID[] eventId);
}

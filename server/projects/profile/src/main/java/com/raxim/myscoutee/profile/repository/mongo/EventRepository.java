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
import com.raxim.myscoutee.profile.data.document.mongo.EventWithCandidates;
import com.raxim.myscoutee.profile.data.document.mongo.EventWithToken;
import com.raxim.myscoutee.profile.data.dto.rest.CodeDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.FeedbackDTO;
import com.raxim.myscoutee.profile.data.dto.rest.MemberDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;

@RepositoryRestResource(collectionResourceRel = "events", path = "events")
public interface EventRepository extends MongoRepository<Event, UUID> {

        @Query("{'type': ?0}")
        List<Event> findEventByType(String type);

        @Aggregation(pipeline = "findEventsWithCandidates")
        List<EventWithCandidates> findEventsWithCandidates();

        @Aggregation(pipeline = "findEvents")
        List<Event> findEvents();

        @Query("{'status': 'P', 'members.profile.$id': ?0}")
        List<Event> findPendingEvents(UUID profileId);

        @Aggregation(pipeline = "findEventDown")
        List<EventDTO> findEventDown(
                        @Param("param") PageParam param,
                        @Param("status") String[] status);

        @Aggregation(pipeline = "findTemplates")
        List<EventDTO> findTemplates(
                        @Param("param") PageParam param,
                        @Param("status") String[] status);

        @Aggregation(pipeline = "findInvitationDown")
        List<EventDTO> findInvitationDown(
                        @Param("param") PageParam param,
                        @Param("status") String[] status);

        @Aggregation(pipeline = "findRecommendation")
        List<EventDTO> findRecommendation(
                        @Param("param") PageParam param,
                        @Param("loc") Point loc,
                        @Param("groupId") UUID groupId,
                        @Param("status") String status);

        @Aggregation(pipeline = "findEventUp")
        List<EventDTO> findEventUp(
                        @Param("param") PageParam param,
                        @Param("status") String[] status);

        @Aggregation(pipeline = "findEventByMonth")
        List<EventDTO> findEventByMonth(
                        @Param("param") PageParam param,
                        @Param("status") String[] status);

        @Aggregation(pipeline = "findItemsByEvent")
        List<EventDTO> findItemsByEvent(
                        @Param("eventId") UUID eventId,
                        @Param("param") PageParam param);

        @Aggregation(pipeline = "findFeedbacksByEvent")
        List<FeedbackDTO> findFeedbacksByEvent(
                        @Param("eventId") UUID eventId,
                        @Param("limit") int limit,
                        @Param("step") int step,
                        @Param("offset") Object[] offset);

        @Aggregation(pipeline = "findMembersByEvent")
        List<MemberDTO> findMembersByEvent(
                        @Param("param") PageParam param,
                        @Param("eventId") UUID eventId,
                        @Param("status") String[] status);

        @Aggregation(pipeline = "findMemberByCode")
        Optional<MemberDTO> findMemberByCode(
                        @Param("eventId") UUID eventId,
                        @Param("code") String code);

        @Aggregation(pipeline = "findCodeByEvent")
        Optional<CodeDTO> findCodeByEvent(
                        @Param("eventId") UUID eventId,
                        @Param("userUid") UUID userUid);

        @Aggregation(pipeline = "findTokensByEvent")
        Optional<EventWithToken> findTokensByEvent(@Param("eventId") UUID eventId);

        @Aggregation(pipeline = "findParents")
        List<Event> findParents(
                        @Param("eventId") UUID eventId,
                        @Param("level") int level);

}

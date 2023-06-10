package com.raxim.myscoutee.profile.repository.mongo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mongodb.client.model.geojson.Point;
import com.raxim.myscoutee.profile.data.document.mongo.Promotion;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.MemberDTO;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PromotionDTO;

@Repository
public interface PromotionRepository
        extends MongoRepository<com.raxim.myscoutee.profile.data.document.mongo.Promotion, UUID> {

    @Query("{'events.$id' : ?0}")
    Optional<Promotion> findPromotionByEvent(UUID eventId);

    @Aggregation(pipeline = "findPromotionsByProfile")
    List<PromotionDTO> findPromotionsByProfile(
            UUID profileId,
            int limit,
            int step,
            @Param("offset") Object[] offset);

    @Aggregation(pipeline = "findEventsByPromotion")
    List<EventDTO> findEventsByPromotion(
            UUID promotionId,
            int limit,
            int step,
            String format,
            UUID profileId,
            @Param("offset") Object[] offset);

    @Aggregation(pipeline = "findProfilesByPromotion")
    List<ProfileDTO> findProfilesByPromotion(
            UUID promotionId,
            int limit,
            int step,
            UUID profileId,
            @Param("offset") Object[] offset);

    @Aggregation(pipeline = "findMembersByPromotion")
    List<MemberDTO> findMembersByPromotion(
            UUID promotionId,
            int limit,
            int step,
            @Param("status") String[] status,
            @Param("offset") Object[] offset);

    @Aggregation(pipeline = "findPromotionsByRec")
    List<PromotionDTO> findPromotionsByRec(
            UUID currentId,
            Point loc,
            int limit,
            int step,
            String type,
            UUID groupId,
            @Param("offset") Object[] offset);

    @Aggregation(pipeline = "findFullEventsByPromoter")
    List<EventDTO> findFullEventsByPromoter(
            UUID profileId,
            int limit,
            int step,
            String format,
            @Param("offset") Object[] offset);
}

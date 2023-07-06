package com.raxim.myscoutee.profile.repository.mongo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mongodb.client.model.geojson.Point;
import com.raxim.myscoutee.profile.data.document.mongo.Promotion;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.MemberDTO;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PromotionDTO;

public interface PromotionRepository
                extends MongoRepository<Promotion, UUID> {

        @Query("{'events.$id' : ?0}")
        Optional<Promotion> findPromotionByEvent(UUID eventId);

        @Aggregation(pipeline = "findPromotionsByProfile")
        List<PromotionDTO> findPromotionsByProfile(
                        @Param("profileId") UUID profileId,
                        @Param("limit") int limit,
                        @Param("step") int step,
                        @Param("offset") Object[] offset);

        @Aggregation(pipeline = "findEventsByPromotion")
        List<EventDTO> findEventsByPromotion(
                        @Param("promotionId") UUID promotionId,
                        @Param("limit") int limit,
                        @Param("step") int step,
                        @Param("format") String format,
                        @Param("profileId") UUID profileId,
                        @Param("offset") Object[] offset);

        @Aggregation(pipeline = "findProfilesByPromotion")
        List<ProfileDTO> findProfilesByPromotion(
                        @Param("promotionId") UUID promotionId,
                        @Param("limit") int limit,
                        @Param("step") int step,
                        @Param("profileId") UUID profileId,
                        @Param("offset") Object[] offset);

        @Aggregation(pipeline = "findMembersByPromotion")
        List<MemberDTO> findMembersByPromotion(
                        @Param("promotionId") UUID promotionId,
                        @Param("limit") int limit,
                        @Param("step") int step,
                        @Param("status") String[] status,
                        @Param("offset") Object[] offset);

        @Aggregation(pipeline = "findPromotionsByRec")
        List<PromotionDTO> findPromotionsByRec(
                        @Param("currentId") UUID currentId,
                        @Param("loc") Point loc,
                        @Param("limit") int limit,
                        @Param("step") int step,
                        @Param("type") String type,
                        @Param("groupId") UUID groupId,
                        @Param("offset") Object[] offset);

        @Aggregation(pipeline = "findFullEventsByPromoter")
        List<EventDTO> findFullEventsByPromoter(
                        @Param("profileId") UUID profileId,
                        @Param("limit") int limit,
                        @Param("step") int step,
                        @Param("format") String format,
                        @Param("offset") Object[] offset);
}

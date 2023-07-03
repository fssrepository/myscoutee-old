package com.raxim.myscoutee.profile.repository.mongo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.raxim.myscoutee.profile.data.document.mongo.EventItem;
import com.raxim.myscoutee.profile.data.dto.rest.MemberDTO;

@RepositoryRestResource(collectionResourceRel = "items", path = "items")
public interface EventItemRepository extends MongoRepository<EventItem, UUID> {

        @Query("{'type': ?0}")
        List<EventItem> findEventItemsByType(String type);

        @Aggregation(pipeline = "findMembersByItem")
        List<MemberDTO> findMembersByItem(
                        @Param("itemId") UUID itemId,
                        @Param("limit") int limit,
                        @Param("step") int step,
                        @Param("status") String[] status,
                        @Param("offset") Object[] offset);
}

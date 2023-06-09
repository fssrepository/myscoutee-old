package com.raxim.myscoutee.profile.repository.mongo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.raxim.myscoutee.profile.data.document.mongo.EventItem;
import com.raxim.myscoutee.profile.data.dto.rest.MemberDTO;

@RepositoryRestResource(collectionResourceRel = "items", path = "items")
public interface EventItemRepository extends MongoRepository<EventItem, UUID>, QuerydslPredicateExecutor<EventItem> {
        @Aggregation(pipeline = "findMembersByItem")
        List<MemberDTO> findMembersByItem(
                        UUID itemId,
                        int limit,
                        int step,
                        @Param("status") String[] status,
                        @Param("offset") Object[] offset);
}

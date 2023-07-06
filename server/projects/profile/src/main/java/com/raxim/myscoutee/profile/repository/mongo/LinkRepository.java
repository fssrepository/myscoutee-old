package com.raxim.myscoutee.profile.repository.mongo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.raxim.myscoutee.profile.data.document.mongo.Link;
import com.raxim.myscoutee.profile.data.dto.rest.RewardDTO;

public interface LinkRepository extends MongoRepository<Link, UUID> {

    @Query("{key: ?0}")
    Link findByKey(UUID key);

    @Aggregation(pipeline = "findRewards")
    List<RewardDTO> findRewards(UUID profile);

}

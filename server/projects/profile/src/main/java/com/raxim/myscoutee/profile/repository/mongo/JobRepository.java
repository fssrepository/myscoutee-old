package com.raxim.myscoutee.profile.repository.mongo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import com.raxim.myscoutee.profile.data.document.mongo.Job;
import com.raxim.myscoutee.profile.data.dto.rest.JobDTO;

public interface JobRepository extends MongoRepository<Job, UUID> {

    @Aggregation(pipeline = "findJobsByProfile")
    List<JobDTO> findJobsByProfile(
            @Param("profileId") UUID profileId,
            @Param("limit") int limit,
            @Param("step") int step,
            @Param("offset") Object[] offset);

}

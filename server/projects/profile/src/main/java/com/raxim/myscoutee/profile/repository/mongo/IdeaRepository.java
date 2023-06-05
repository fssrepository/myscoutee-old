package com.raxim.myscoutee.profile.repository.mongo;

import com.raxim.myscoutee.profile.data.document.mongo.Idea;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IdeaRepository extends MongoRepository<Idea, UUID> {

    @Aggregation(pipeline = {
        "{\n" +
        "        $match: {\n" +
        "            'createdBy': ?0\n" +
        "        }\n" +
        "}",

        "{\n" +
        "        $match: {\n" +
        "            $expr: \n" +
        "            {\n" +
        "                $and: [\n" +
        "                    {$gt: [ {$ifNull: [ '$createdDate', ISODate()]}, { $toDate: :#{#offset[0]} }] },\n" +
        "                ]\n" +
        "            },\n" +
        "        }\n" +
        "}",

        "{\n" +
        "        $sort: {\n" +
        "            'createdDate': 1\n" +
        "        }\n" +
        "}",

        "{ $limit : ?1 }",

        "{\n" +
        "        $replaceRoot: { \n" +
        "            'newRoot': { \n" +
        "                $mergeObjects: [{'idea': '$$ROOT'}, { groupKey: '$createdDate', offset: ['$createdDate'] } ]\n" +
        "            }\n" +
        "        }\n" +
        "}"
    })
    List<com.raxim.myscoutee.profile.data.dto.rest.Idea> findIdeasByProfile(
        UUID profileId,
        int limit,
        int step,
        @Param("offset") Object[] offset
    );
}

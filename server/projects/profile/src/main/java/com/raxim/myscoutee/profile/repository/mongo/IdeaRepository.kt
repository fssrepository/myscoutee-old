package com.raxim.myscoutee.profile.repository.mongo

import com.raxim.myscoutee.profile.data.document.mongo.Idea
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface IdeaRepository : MongoRepository<Idea, UUID> {

    @Aggregation(
        pipeline = [
            "{\n" +
                    "        \$match: {\n" +
                    "            'createdBy': ?0\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: \n" +
                    "            {\n" +
                    "                \$and: [\n" +
                    "                    {\$gt: [ {\$ifNull: [ '\$createdDate', ISODate()]}, { \$toDate: :#{#offset[0]} }] },\n" +
                    "                ]\n" +
                    "            },\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$sort: {\n" +
                    "            'createdDate': 1\n" +
                    "        }\n" +
                    "}",

            "{ \$limit : ?1 }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            'newRoot': { \n" +
                    "                \$mergeObjects: [{'idea': '\$\$ROOT'}, { groupKey: '\$createdDate', offset: ['\$createdDate'] } ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "}"
        ]
    )
    fun findIdeasByProfile(
        profileId: UUID,
        limit: Int,
        step: Int,
        @Param("offset") offset: Array<Any>
    ): List<com.raxim.myscoutee.profile.data.dto.rest.Idea>
}
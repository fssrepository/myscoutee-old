package com.raxim.myscoutee.profile.repository.mongo

import com.raxim.myscoutee.profile.data.document.mongo.EventItem
import com.raxim.myscoutee.profile.data.dto.rest.Member
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(
    collectionResourceRel = "items",
    path = "items"
)
interface EventItemRepository : MongoRepository<EventItem, UUID>,
    QuerydslPredicateExecutor<EventItem> {
    @Aggregation(
        pipeline = [
            "{\n" +
                    "        \$match: {\n" +
                    "            '_id': ?0\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$unwind: '\$members'\n" +
                    "}",

            "{\n" +
                    "        \$lookup:\n" +
                    "        {\n" +
                    "            from: 'profiles',\n" +
                    "            let: { 'p_id': '\$members.profile.\$id', 'p_members' : '\$members' },\n" +
                    "            pipeline: [\n" +
                    "                {\n" +
                    "                    \$match: {\n" +
                    "                        \$expr: {\n" +
                    "                            \$eq: ['\$\$p_id', '\$_id']\n" +
                    "                        }\n" +
                    "                    },\n" +
                    "                },\n" +
                    "                {\n" +
                    "                    \$project: {\n" +
                    "                        '_id' : '\$\$p_members._id',\n" +
                    "                        'profile': '\$\$ROOT',\n" +
                    "                        'createdDate': '\$\$p_members.createdDate',\n" +
                    "                        'status': '\$\$p_members.status'\n" +
                    "                    }\n" +
                    "                }\n" +
                    "            ],\n" +
                    "            as: 'profiles',\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$unwind: '\$profiles'\n" +
                    "}",

            "{\n" +
                    "        \$group: {\n" +
                    "            '_id': '\$profiles._id',\n" +
                    "            data: {\n" +
                    "                \$first: '\$profiles'\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: \n" +
                    "            {\n" +
                    "                \$and: [\n" +
                    "                    {\$gt: [ {\$ifNull: [ '\$data.createdDate', ISODate()]}, { \$toDate: :#{#offset[1]} }] },\n" +
                    "                    {\$in: [ '\$data.status', :#{#status} ] },\n" +
                    "                    {\$gte: [ '\$data.status', :#{#offset[0]} ] }\n" +
                    "                ]\n" +
                    "            },\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$sort: {\n" +
                    "            'data.status': 1, 'data.createdDate': -1\n" +
                    "        }\n" +
                    "}",

            "{ \$limit : ?1 }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            'newRoot': { \n" +
                    "                \$mergeObjects: [{'member': '\$data'}, { offset: ['\$data.status', '\$data.createdDate'] } ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "}"
        ]
    )
    fun findMembersByItem(
        itemId: UUID,
        limit: Int,
        step: Int,
        @Param("status") status: Array<String>,
        @Param("offset") offset: Array<Any>
    ): List<Member>
}
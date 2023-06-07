package com.raxim.myscoutee.profile.repository.mongo

import com.raxim.myscoutee.profile.data.document.mongo.Like
import com.raxim.myscoutee.profile.data.document.mongo.Neo4jLike
import com.raxim.myscoutee.profile.data.document.mongo.Badge
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(
    collectionResourceRel = "likes",
    path = "likes"
)
interface LikeRepository : MongoRepository<Like, UUID>,
    QuerydslPredicateExecutor<Like> {

    @Query("{\$and: [{'createdBy.\$id': ?0}, { 'from.\$id' : { \$in: [ ?1 ] }}, { 'to.\$id' : { \$in: [ ?2 ] }} ] }")
    fun findByIds(currUser: UUID, from: Set<UUID>, to: Set<UUID>): List<Like>

    @Aggregation(
        pipeline = [
            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: \n" +
                    "            {\n" +
                    "            \$and: [\n" +
                    "                {\$eq: ['\$to.\$id', ?0]},\n" +
                    "                {\$gt: ['\$createdDate', {\$toDate: ?1 }] },\n" +
                    "                {\n" +
                    "                    \$eq: [{\$ifNull: [ '\$ref', 0]}, 0]\n" +
                    "                },\n" +
                    "            ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }",
            "{ \$count: 'games' }",
            "{ \n" +
                    "            \$replaceRoot: {  \n" +
                    "                'newRoot': {  \n" +
                    "                    \$mergeObjects: [{'root': 'games', 'value': '\$games' } ]\n" +
                    "                } \n" +
                    "            } \n" +
                    "    }"
        ]
    )
    fun newLikesByProfile(profileId: UUID, date: String): List<Badge>

    @Aggregation(
        pipeline = [
            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: \n" +
                    "            {\n" +
                    "            \$and: [\n" +
                    "                {\$eq: ['\$createdBy.\$id', ?0]},\n" +
                    "                {\n" +
                    "                    \$or: [\n" +
                    "                        {\$eq: ['\$from.\$id', ?1]},\n" +
                    "                        {\$eq: ['\$to.\$id', ?1]}\n" +
                    "                    ]\n" +
                    "                },\n" +
                    "                {\n" +
                    "                    \$eq: ['\$double', true]\n" +
                    "                },\n" +
                    "                {\n" +
                    "                    \$eq: [{\$ifNull: [ '\$ref', 0]}, 0]\n" +
                    "                },\n" +
                    "            ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$lookup:\n" +
                    "        {\n" +
                    "            from: 'profiles',\n" +
                    "            let: { 'from_id': '\$from.\$id', 'to_id' : '\$to.\$id' },\n" +
                    "            pipeline: [\n" +
                    "                {\n" +
                    "                    \$match: {\n" +
                    "                        \$expr: {\n" +
                    "                            \$or: [{\n" +
                    "                                    \$and: [\n" +
                    "                                        { \$eq: ['\$\$from_id', '\$_id']}, \n" +
                    "                                        { \$ne: ['\$\$from_id', ?1]}\n" +
                    "                                    ]},\n" +
                    "                                {\n" +
                    "                                    \$and: [\n" +
                    "                                        { \$eq: ['\$\$to_id', '\$_id']}, \n" +
                    "                                        { \$ne: ['\$\$to_id', ?1]}\n" +
                    "                                    ]}\n" +
                    "                            ]\n" +
                    "                        }\n" +
                    "                    },\n" +
                    "                },\n" +
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
                    "            'rate': {\$first: '\$rate'},\n" +
                    "            data: {\n" +
                    "                \$first: '\$profiles'\n" +
                    "            }\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: \n" +
                    "            {\n" +
                    "                \$and: [\n" +
                    "                    {\$gt: [ {\$ifNull: [ '\$data.createdDate', ISODate()]}, { \$toDate: :#{#offset[0]} }] },\n" +
                    "                    {\$in: [ '\$data.status', :#{#status} ]},\n" +
                    "                ]\n" +
                    "            },\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$sort: {\n" +
                    "            'data.createdDate': -1\n" +
                    "        }\n" +
                    "}",

            "{ \$limit : ?2 }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            'newRoot': { \n" +
                    "                \$mergeObjects: [{'profile': '\$data'}, { rate: {\$ifNull: [ '\$rate', 0]}, offset: ['\$data.createdDate'] } ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "}"
        ]
    )
    fun findDoubleById(
        currUser: UUID,
        selectedUser: UUID,
        limit: Int,
        step: Int,
        @Param("status") status: Array<String>,
        @Param("offset") offset: Array<Any>
    ): List<com.raxim.myscoutee.profile.data.dto.rest.Profile>

    @Aggregation(
        pipeline = [
            "{\n" +
                    "        \$match: {\n" +
                    "          \$expr: \n" +
                    "            {\n" +
                    "              \$gte: ['\$createdDate', { \$toDate: ?0 }]\n" +
                    "            }\n" +
                    "        },\n" +
                    "    }",

            "{\n" +
                    "        \$lookup:\n" +
                    "        {\n" +
                    "            from: 'likes',\n" +
                    "            let: { p_from: '\$from.\$id' , p_to: '\$to.\$id' },\n" +
                    "            pipeline: [\n" +
                    "              {\n" +
                    "                \$match: {\n" +
                    "                    \$expr: \n" +
                    "                    {\n" +
                    "                          \$or: [\n" +
                    "                            {\n" +
                    "                              \$and: [\n" +
                    "                                { \$eq: ['\$from.\$id', '\$\$p_from'] },\n" +
                    "                                { \$eq: ['\$to.\$id', '\$\$p_to'] },\n" +
                    "                                { \$eq: [ {\$ifNull: [ '\$ref', 0]}, 0 ]}\n" +
                    "                              ]\n" +
                    "                            },\n" +
                    "                            {\n" +
                    "                              \$and: [\n" +
                    "                                { \$eq: ['\$to.\$id', '\$\$p_from'] },\n" +
                    "                                { \$eq: ['\$from.\$id', '\$\$p_to'] },\n" +
                    "                                { \$eq: [ {\$ifNull: [ '\$ref', 0]}, 0 ]}\n" +
                    "                              ]\n" +
                    "                            }\n" +
                    "                          ]\n" +
                    "                    }\n" +
                    "                }\n" +
                    "              },\n" +
                    "              { \n" +
                    "                  \$group: {\n" +
                    "                      '_id': '\$\$p_from', \n" +
                    "                      'distance': { \n" +
                    "                          \$avg: '\$distance' \n" +
                    "                      },\n" +
                    "                      'type': { \n" +
                    "                          \$avg: { \n" +
                    "                              \$switch: { \n" +
                    "                                  branches: [ \n" +
                    "                                      {  \n" +
                    "                                          case: {  \n" +
                    "                                              \$and: [ \n" +
                    "                                                  { \$eq: ['\$to.\$id', '\$\$p_to' ] }, \n" +
                    "                                                  { \$eq: ['\$double', false] },\n" +
                    "                                                  { \$eq: [ {\$ifNull: [ '\$ref', 0]}, 0 ]} \n" +
                    "                                              ] \n" +
                    "                                          }, \n" +
                    "                                          then: 2 \n" +
                    "                                      }, \n" +
                    "                                      {  \n" +
                    "                                          case: {  \n" +
                    "                                              \$and: [ \n" +
                    "                                                  { \$eq: ['\$from.\$id', '\$\$p_to' ] }, \n" +
                    "                                                  { \$eq: ['\$double', false] },\n" +
                    "                                                  { \$eq: [ {\$ifNull: [ '\$ref', 0]}, 0 ]}\n" +
                    "                                              ] \n" +
                    "                                          }, \n" +
                    "                                          then: 1 \n" +
                    "                                      }, \n" +
                    "                                  ], \n" +
                    "                                  default: 0 \n" +
                    "                              } \n" +
                    "                          } \n" +
                    "                      },\n" +
                    "                      'rate': {\n" +
                    "                              \$sum: { \n" +
                    "                                \$switch: { \n" +
                    "                                    branches: [ \n" +
                    "                                        {  \n" +
                    "                                            case: {  \n" +
                    "                                                \$and: [\n" +
                    "                                                    { \$eq: ['\$double', false] }, \n" +
                    "                                                ] \n" +
                    "                                            }, \n" +
                    "                                            then: {\$divide: [1, '\$rate']} \n" +
                    "                                        } \n" +
                    "                                    ],\n" +
                    "                                    default: 0 \n" +
                    "                                } \n" +
                    "                              }\n" +
                    "                      },\n" +
                    "                      'from': {\n" +
                    "                        \$first: '\$from'\n" +
                    "                      },\n" +
                    "                      'to': {\n" +
                    "                        \$first: '\$to'\n" +
                    "                      },\n" +
                    "                      'createdDate': { \n" +
                    "                          \$max: '\$createdDate' \n" +
                    "                      }, \n" +
                    "                  }, \n" +
                    "              },\n" +
                    "            ],\n" +
                    "          as: 'likes'\n" +
                    "      }\n" +
                    "    }",

            "{ \n" +
                    "        \$unwind: { path: '\$likes' } \n" +
                    "    }",

            "{\n" +
                    "      \$group: {\n" +
                    "          '_id': '\$likes.createdDate',\n" +
                    "          'data': { \$first : '\$likes'}\n" +
                    "      } \n" +
                    "    }",

            "{ \n" +
                    "        \$match: { \n" +
                    "            \$expr:  \n" +
                    "            { \n" +
                    "              \$and: [\n" +
                    "                {\$eq: [ {\$ifNull: ['\$data.type', 0.0]}, ?1 ]}\n" +
                    "              ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }",

            "{ \n" +
                    "            \$replaceRoot: {  \n" +
                    "                'newRoot': '\$data' \n" +
                    "            } \n" +
                    "    }"
        ]
    )
    fun findBothAll(lastTime: String, type: Double): List<Neo4jLike>
}
package com.raxim.myscoutee.profile.repository.mongo

import com.raxim.myscoutee.profile.data.document.mongo.Topic
import com.raxim.myscoutee.profile.data.document.mongo.User
import com.raxim.myscoutee.profile.data.dto.rest.Group
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(
    collectionResourceRel = "users",
    path = "users"
)
interface UserRepository : MongoRepository<User, UUID>,
    QuerydslPredicateExecutor<User> {

    @Query("{email: ?0}")
    fun findUserByEmail(email: String): User?

    @Aggregation(
        pipeline = [
            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: { \$eq: ['\$email', ?0] }\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$unwind: '\$profiles'\n" +
                    "    }",

            "{\n" +
                    "        \$lookup:\n" +
                    "        {\n" +
                    "            from: 'profiles',\n" +
                    "            let: { 'p_id': '\$profiles.\$id' },\n" +
                    "            pipeline: [\n" +
                    "                {\n" +
                    "                    \$match: {\n" +
                    "                    \$expr: \n" +
                    "                      {\n" +
                    "                        \$and: [\n" +
                    "                            { \$ne: ['\$status', 'D' ] },\n" +
                    "                            { \$eq: ['\$\$p_id', '\$_id'] }\n" +
                    "                        ]\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                },\n" +
                    "            ],\n" +
                    "            as: 'profile'\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$unwind: '\$profile'\n" +
                    "    }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            'newRoot': '\$profile'\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$lookup:\n" +
                    "        {\n" +
                    "            from: 'roles',\n" +
                    "            let: { 'p_id': '\$_id' },\n" +
                    "            pipeline: [\n" +
                    "                {\n" +
                    "                    \$match: {\n" +
                    "                        \$expr: { \$eq: ['\$\$p_id', '\$profileId'] }\n" +
                    "                    },\n" +
                    "                },\n" +
                    "            ],\n" +
                    "            as: 'role'\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$unwind: '\$role'\n" +
                    "    }",

            "{\n" +
                    "        \$lookup:\n" +
                    "        {\n" +
                    "            from: 'groups',\n" +
                    "            let: { 'g_id': '\$group'},\n" +
                    "            pipeline: [\n" +
                    "                {\n" +
                    "                    \$match: {\n" +
                    "                        \$expr: {\$eq: ['\$\$g_id', '\$_id']},\n" +
                    "                        \n" +
                    "                    },\n" +
                    "                },\n" +
                    "            ],\n" +
                    "            as: 'group'\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$unwind: '\$group'\n" +
                    "    }",

            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: { \$eq: ['\$role.role', ?1] }\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: {\n" +
                    "                \$or: [\n" +
                    "                    { \$eq: [?2, true ] },\n" +
                    "                    {\n" +
                    "                        \$and: [\n" +
                    "                            { \$eq: [?2, false ] },\n" +
                    "                            { \$eq: ['\$group.createdBy', ?3] }\n" +
                    "                        ]\n" +
                    "                    }\n" +
                    "                ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$group: {\n" +
                    "            '_id': '\$group._id',\n" +
                    "            'role': {\n" +
                    "                \$first: '\$role.role'\n" +
                    "            },\n" +
                    "            data: {\n" +
                    "                \$first: '\$group'\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: \n" +
                    "            {\n" +
                    "                \$gt: [ {\$ifNull: [ '\$data.createdDate', ISODate()]}, { \$toDate: :#{#offset[0]} }],\n" +
                    "            },\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$sort: {\n" +
                    "            'data.createdDate': -1\n" +
                    "        }\n" +
                    "}",

            "{ \$limit : ?4 }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            'newRoot': { \n" +
                    "                \$mergeObjects: [{'group': '\$data'}, { offset: ['\$data.createdDate'] } ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "}"
        ]
    )
    fun findGroupsByEmail(
        email: String,
        role: String,
        isAdmin: Boolean,
        profile: UUID,
        limit: Int,
        step: Int,
        @Param("offset") offset: Array<Any>
    ): List<Group>

    @Aggregation(
        pipeline = [
            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: { \$eq: ['\$email', ?0] }\n" +
                    "        }\n" +
                    "    }",

            "{ \n" +
                    "                            \$unwind: '\$profiles' \n" +
                    "                        }",

            "{\n" +
                    "        \$lookup:\n" +
                    "        {\n" +
                    "            from: 'profiles',\n" +
                    "            let: { 'p_id': '\$profiles.\$id' },\n" +
                    "            pipeline: [\n" +
                    "                {\n" +
                    "                    \$match: {\n" +
                    "                        \$expr: {\n" +
                    "                            \$and: [\n" +
                    "                                { \$eq: ['\$\$p_id', '\$_id'] },\n" +
                    "                                { \$ne: ['\$status', 'D'] }\n" +
                    "                                { \$ne: ['\$status', 'P'] }\n" +
                    "                            ]\n" +
                    "                        }\n" +
                    "                    },\n" +
                    "                },\n" +
                    "            ],\n" +
                    "            as: 'profile'\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$unwind: '\$profile'\n" +
                    "    }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            \"newRoot\": '\$profile'\n" +
                    "        }\n" +
                    "    }",

            "{ \n" +
                    "        \$lookup: \n" +
                    "        { \n" +
                    "            from: 'roles', \n" +
                    "            let: { 'p_id': '\$_id' }, \n" +
                    "            pipeline: [ \n" +
                    "                { \n" +
                    "                    \$match: { \n" +
                    "                        \$expr: { \$eq: ['\$\$p_id', '\$profileId'] } \n" +
                    "                    }, \n" +
                    "                }, \n" +
                    "            ], \n" +
                    "            as: 'role' \n" +
                    "        } \n" +
                    "    }",

            "{ \n" +
                    "        \$unwind: '\$role' \n" +
                    "    }",

            "{\n" +
                    "        \$lookup:\n" +
                    "        {\n" +
                    "            from: 'groups',\n" +
                    "            let: { 'g_id': '\$group'},\n" +
                    "            pipeline: [\n" +
                    "                {\n" +
                    "                    \$match: {\n" +
                    "                        \$expr: {\$eq: ['\$\$g_id', '\$_id']},\n" +
                    "                        \n" +
                    "                    },\n" +
                    "                },\n" +
                    "            ],\n" +
                    "            as: 'group'\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$unwind: '\$group'\n" +
                    "    }",

            "{\n" +
                    "        \$group: {\n" +
                    "            '_id': '\$_id',\n" +
                    "            'role': {\n" +
                    "                \$first: '\$role.role'\n" +
                    "            },\n" +
                    "            'data': {\n" +
                    "                \$first: '\$group'\n" +
                    "            },\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$sort: {\n" +
                    "            'group.createdDate': 1\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            \"newRoot\": { \n" +
                    "                \$mergeObjects: [{'group': '\$data'}, { 'role': '\$role' , offset: [] } ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }"
        ]
    )
    fun findAllGroupsByEmail(
        email: String
    ): List<Group>

    @Aggregation(
        pipeline = [
            "{ \n" +
                    "        \$unwind: { path: '\$profiles' } \n" +
                    "    }",
            "{\n" +
                    "        \$lookup:\n" +
                    "        {\n" +
                    "            from: 'profiles',\n" +
                    "            let: { p_id: '\$profiles.\$id' },\n" +
                    "            pipeline: [\n" +
                    "              {\n" +
                    "                  \$match: {\n" +
                    "                    \$expr: \n" +
                    "                      {\n" +
                    "                        \$and: [\n" +
                    "                          {\$eq: [ '\$\$p_id', '\$_id' ]},\n" +
                    "                          {\$gte: ['\$createdDate', { \$toDate: ?0 }]},\n" +
                    "                        ]\n" +
                    "                      }\n" +
                    "                  },            \n" +
                    "              },\n" +
                    "              {\n" +
                    "                \$lookup:\n" +
                    "                  {\n" +
                    "                      from: 'groups',\n" +
                    "                      let: { g_id: '\$group' },\n" +
                    "                      pipeline: [\n" +
                    "                        {\n" +
                    "                            \$match: {\n" +
                    "                              \$expr: \n" +
                    "                                {\n" +
                    "                                  \$and: [{\n" +
                    "                                    \$eq: [ '\$\$g_id', '\$_id' ],\n" +
                    "                                  }]\n" +
                    "                                }\n" +
                    "                            },\n" +
                    "                            \n" +
                    "                        },\n" +
                    "                      ],\n" +
                    "                    as: 'group'\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "          as: 'profiles'\n" +
                    "      }\n" +
                    "    }",
            "{\n" +
                    "        \$lookup:\n" +
                    "        {\n" +
                    "            from: 'tokens',\n" +
                    "            let: { p_uuid: '\$_id' },\n" +
                    "            pipeline: [\n" +
                    "              {\n" +
                    "                  \$match: {\n" +
                    "                    \$expr: \n" +
                    "                      {\n" +
                    "                        \$eq: [ '\$\$p_uuid', '\$uuid' ],\n" +
                    "                      }\n" +
                    "                  },\n" +
                    "              },\n" +
                    "            ],\n" +
                    "          as: 'tokens'\n" +
                    "      }\n" +
                    "    }",

            "{ \n" +
                    "        \$unwind: { path: '\$profiles' } \n" +
                    "    }",

            // inside lookup $in not working
            "{\n" +
                    "      \$match: {\n" +
                    "          \$expr: \n" +
                    "            {\n" +
                    "              \$in: [ '\$profiles.status', :#{#status} ],\n" +
                    "            }\n" +
                    "        },\n" +
                    "    }",

            "{ \n" +
                    "        \$unwind: { path: '\$profiles.group' } \n" +
                    "    }",

            "{ \n" +
                    "        \$unwind: { path: '\$tokens' } \n" +
                    "    }",

            "{ \n" +
                    "        \$group: {\n" +
                    "            '_id': '\$profiles.group._id', \n" +
                    "            'name': {\$first: '\$profiles.group.name'},\n" +
                    "            'tokens': {\$addToSet: '\$tokens.deviceKey'},\n" +
                    "        }, \n" +
                    "    }"
        ]
    )
    fun findDeviceWithProfileStatusAll(lastTime: String, @Param("status") status: Array<String>): List<Topic>?
}
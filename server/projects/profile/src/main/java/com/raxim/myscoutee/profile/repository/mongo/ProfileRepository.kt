package com.raxim.myscoutee.profile.repository.mongo

import com.mongodb.client.model.geojson.Point
import com.raxim.myscoutee.profile.data.dto.rest.Car
import com.raxim.myscoutee.profile.data.dto.rest.Event
import com.raxim.myscoutee.profile.data.dto.rest.Profile
import com.raxim.myscoutee.profile.data.dto.rest.School
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(
    collectionResourceRel = "profiles",
    path = "profiles"
)
interface ProfileRepository : MongoRepository<com.raxim.myscoutee.profile.data.document.mongo.Profile, UUID>,
    QuerydslPredicateExecutor<com.raxim.myscoutee.profile.data.document.mongo.Profile> {

    @Aggregation(
        pipeline = [
            "{\n" +
                    "        \$geoNear :\n" +
                    "        {\n" +
                    "            near :\n" +
                    "            ?0,\n" +
                    "            spherical     : true,\n" +
                    "            minDistance   : :#{#offset[0]},\n" +
                    "            distanceField : 'distance',\n" +
                    "            distanceMultiplier: 0.001,\n" +
                    "            query         : {'_id': { \$nin: [?4] }, 'gender': ?5, 'group': ?6 }\n" +
                    "        }\n" +
                    "}",

            "{ \n" +
                    "                            \$lookup: \n" +
                    "                            { \n" +
                    "                                from: 'likes', \n" +
                    "                                let: { p_id: '\$_id' }, \n" +
                    "                                pipeline: [ \n" +
                    "                                    { \n" +
                    "                                        \$match: { \n" +
                    "                                            \$expr:  \n" +
                    "                                            { \n" +
                    "                                                \$or: [ \n" +
                    "                                                    { \n" +
                    "                                                        \$and: [ \n" +
                    "                                                            { \$eq: ['\$from.\$id', '\$\$p_id'] }, \n" +
                    "                                                            { \$eq: ['\$to.\$id', ?4 ] },\n" +
                    "                                                            {\n" +
                    "                                                               \$eq: [{\$ifNull: [ '\$ref', 0]}, 0]\n" +
                    "                                                            },\n" +
                    "                                                        ] \n" +
                    "                                                    }, \n" +
                    "                                                    { \n" +
                    "                                                        \$and: [ \n" +
                    "                                                            { \$eq: ['\$to.\$id', '\$\$p_id'] }, \n" +
                    "                                                            { \$eq: ['\$from.\$id', ?4 ] },\n" +
                    "                                                            {\n" +
                    "                                                               \$eq: [{\$ifNull: [ '\$ref', 0]}, 0]\n" +
                    "                                                            },\n" +
                    "                                                        ] \n" +
                    "                                                    },\n" +
                    "                                                ] \n" +
                    "                                            }, \n" +
                    "                                        } \n" +
                    "                                    }, \n" +
                    "                                    { \n" +
                    "                                        \$group: {\n" +
                    "                                            '_id': '\$\$p_id', \n" +
                    "                                            'distance': { \n" +
                    "                                                \$avg: '\$distance' \n" +
                    "                                            },\n" +
                    "                                            'ref' : {\n" +
                    "                                                \$first: {\$ifNull: [ '\$ref', 0]}\n" +
                    "                                            },\n" +
                    "                                            'type': { \n" +
                    "                                                \$avg: { \n" +
                    "                                                    \$switch: { \n" +
                    "                                                        branches: [ \n" +
                    "                                                            {  \n" +
                    "                                                                case: {  \n" +
                    "                                                                    \$and: [ \n" +
                    "                                                                        { \$eq: ['\$to.\$id', ?4 ] }, \n" +
                    "                                                                        { \$eq: ['\$double', false] }, \n" +
                    "                                                                    ] \n" +
                    "                                                                }, \n" +
                    "                                                                then: 2 \n" +
                    "                                                            }, \n" +
                    "                                                            {  \n" +
                    "                                                                case: {  \n" +
                    "                                                                    \$and: [ \n" +
                    "                                                                        { \$eq: ['\$from.\$id', ?4 ] }, \n" +
                    "                                                                        { \$eq: ['\$double', false] }, \n" +
                    "                                                                    ] \n" +
                    "                                                                }, \n" +
                    "                                                                then: 1 \n" +
                    "                                                            }, \n" +
                    "                                                        ], \n" +
                    "                                                        default: 0 \n" +
                    "                                                    } \n" +
                    "                                                } \n" +
                    "                                            },\n" +
                    "                                            'rate': { \n" +
                    "                                                \$sum: { \n" +
                    "                                                    \$switch: { \n" +
                    "                                                        branches: [ \n" +
                    "                                                            {  \n" +
                    "                                                                case: {  \n" +
                    "                                                                    \$and: [ \n" +
                    "                                                                        { \$eq: ['\$createdBy.\$id', ?4 ] }, \n" +
                    "                                                                        { \$eq: ['\$double', false] }, \n" +
                    "                                                                    ] \n" +
                    "                                                                }, \n" +
                    "                                                                then: '\$rate' \n" +
                    "                                                            } \n" +
                    "                                                        ],\n" +
                    "                                                        default: 0 \n" +
                    "                                                    } \n" +
                    "                                                } \n" +
                    "                                            },\n" +
                    "                                            'createdDate': { \n" +
                    "                                                \$max: '\$createdDate' \n" +
                    "                                            }, \n" +
                    "                                        }, \n" +
                    "                                    },\n" +
                    "                                ], \n" +
                    "                                as: 'likes', \n" +
                    "                            } \n" +
                    "                        }",

            "{\n" +
                    "        \$unwind: { path: '\$likes', preserveNullAndEmptyArrays: true }\n" +
                    "    }",
            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: \n" +
                    "            {\n" +
                    "                        \$and: [\n" +
                    "                            {\$eq: [ '\$status', 'A' ] },\n" +
                    "                            {\$eq: [ {\$ifNull: ['\$likes.type', 0]}, :#{#type} ]},\n" +
                    "                            {\$gt: [{\$ifNull: [ '\$likes.createdDate', ISODate()]}, {\$toDate: :#{#offset[3]}}] },\n" +
                    "                            {\$gte: [{\$ifNull: [ '\$likes.distance', { \$abs: {\$subtract: [\$score,?8] } } ]}, :#{#offset[1]}] },\n" +
                    "                            {\$gte: [{\$ifNull: ['\$distance', 0]}, :#{#offset[2]}] },\n" +
                    "                        ]\n" +
                    "            },\n" +
                    "        }\n" +
                    "    }",


            "{\n" +
                    "                \$addFields: {\n" +
                    "                    'groupKey': {\$multiply: [{\$floor : [{\$divide : ['\$distance', 5]}]}, 5]},\n" +
                    "                }\n" +
                    "            }",

            "{\n" +
                    "                \$addFields: {\n" +
                    "                    'likes.rate': {\$ifNull: [ '\$likes.rate', 0]},\n" +
                    "                }\n" +
                    "            }",

            "{ \n" +
                    "                            \$sort: { \n" +
                    "                                'groupKey': 1, 'likes.rate': -1, 'likes.distance': 1\n" +
                    "                            } \n" +
                    "                        }",

            "{ \$limit : ?2 }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            'newRoot': { \n" +
                    "                \$mergeObjects: [{'profile': '\$\$ROOT'}, { rate: '\$likes.rate', groupKey: '\$groupKey', offset: ['\$groupKey', '\$likes.rate', '\$likes.distance', Date()] } ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "}"
        ]
    )
    fun findProfile(
        loc: Point,
        @Param("offset") offset: Array<Any>, //minDistance
        limit: Int,
        step: Int,
        currentId: UUID,
        gender: String,
        groupId: UUID,
        @Param("type") type: Double,
        score: Int? = 0
    ): List<Profile>

    @Aggregation(
        pipeline = [
            "{\n" +
                    "        \$geoNear :\n" +
                    "        {\n" +
                    "            near :\n" +
                    "            ?0,\n" +
                    "            spherical     : true,\n" +
                    "            minDistance   : :#{#offset[0]},\n" +
                    "            distanceField : 'distance',\n" +
                    "            distanceMultiplier: 0.001,\n" +
                    "            query         : {'_id': { \$nin: [?7] }, 'gender': ?5, 'group': ?6 }\n" +
                    "        }\n" +
                    "}",

            "{ \n" +
                    "                            \$lookup: \n" +
                    "                            { \n" +
                    "                                from: 'likes', \n" +
                    "                                let: { p_id: '\$_id' }, \n" +
                    "                                pipeline: [ \n" +
                    "                                    { \n" +
                    "                                        \$match: { \n" +
                    "                                            \$expr:  \n" +
                    "                                            { \n" +
                    "                                                \$or: [ \n" +
                    "                                                    { \n" +
                    "                                                        \$and: [ \n" +
                    "                                                            { \$eq: ['\$from.\$id', '\$\$p_id'] }, \n" +
                    "                                                            { \$eq: ['\$to.\$id', ?4 ] },\n" +
                    "                                                            {\n" +
                    "                                                               \$eq: [{\$ifNull: [ '\$ref', 0]}, 0]\n" +
                    "                                                            },\n" +
                    "                                                        ] \n" +
                    "                                                    }, \n" +
                    "                                                    { \n" +
                    "                                                        \$and: [ \n" +
                    "                                                            { \$eq: ['\$to.\$id', '\$\$p_id'] }, \n" +
                    "                                                            { \$eq: ['\$from.\$id', ?4 ] },\n" +
                    "                                                            {\n" +
                    "                                                               \$eq: [{\$ifNull: [ '\$ref', 0]}, 0]\n" +
                    "                                                            },\n" +
                    "                                                        ] \n" +
                    "                                                    },\n" +
                    "                                                ] \n" +
                    "                                            }, \n" +
                    "                                        } \n" +
                    "                                    }, \n" +
                    "                                    { \n" +
                    "                                        \$group: {\n" +
                    "                                            '_id': '\$\$p_id', \n" +
                    "                                            'distance': { \n" +
                    "                                                \$avg: '\$distance' \n" +
                    "                                            }, \n" +
                    "                                            'type': { \n" +
                    "                                                \$avg: { \n" +
                    "                                                    \$switch: { \n" +
                    "                                                        branches: [ \n" +
                    "                                                            {  \n" +
                    "                                                                case: {  \n" +
                    "                                                                    \$and: [ \n" +
                    "                                                                        { \$eq: ['\$to.\$id', ?4 ] }, \n" +
                    "                                                                        { \$eq: ['\$double', false] }, \n" +
                    "                                                                    ] \n" +
                    "                                                                }, \n" +
                    "                                                                then: 2 \n" +
                    "                                                            }, \n" +
                    "                                                            {  \n" +
                    "                                                                case: {  \n" +
                    "                                                                    \$and: [ \n" +
                    "                                                                        { \$eq: ['\$from.\$id', ?4 ] }, \n" +
                    "                                                                        { \$eq: ['\$double', false] }, \n" +
                    "                                                                    ] \n" +
                    "                                                                }, \n" +
                    "                                                                then: 1 \n" +
                    "                                                            }, \n" +
                    "                                                        ], \n" +
                    "                                                        default: 0 \n" +
                    "                                                    } \n" +
                    "                                                } \n" +
                    "                                            },\n" +
                    "                                            'rate': { \n" +
                    "                                                \$sum: { \n" +
                    "                                                    \$switch: { \n" +
                    "                                                        branches: [ \n" +
                    "                                                            {  \n" +
                    "                                                                case: {  \n" +
                    "                                                                    \$and: [ \n" +
                    "                                                                        { \$eq: ['\$createdBy.\$id', ?7 ] }, \n" +
                    "                                                                        { \$eq: ['\$double', true] }, \n" +
                    "                                                                    ] \n" +
                    "                                                                }, \n" +
                    "                                                                then: '\$rate' \n" +
                    "                                                            } \n" +
                    "                                                        ],\n" +
                    "                                                        default: 0 \n" +
                    "                                                    } \n" +
                    "                                                } \n" +
                    "                                            },\n" +
                    "                                            'createdDate': { \n" +
                    "                                                \$max: '\$createdDate' \n" +
                    "                                            }, \n" +
                    "                                        }, \n" +
                    "                                    },\n" +
                    "                                ], \n" +
                    "                                as: 'likes', \n" +
                    "                            } \n" +
                    "                        }",

            "{\n" +
                    "        \$unwind: { path: '\$likes', preserveNullAndEmptyArrays: true }\n" +
                    "    }",
            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: \n" +
                    "            {\n" +
                    "                        \$and: [\n" +
                    "                            {\$eq: [ '\$likes.rate', 0 ]},\n" +
                    "                            {\$eq: [ '\$status', 'A' ] },\n" +
                    "                            {\$gt: [{\$ifNull: [ '\$likes.createdDate', ISODate()]}, {\$toDate: :#{#offset[3]}}] },\n" +
                    "                            {\$gte: [{\$ifNull: [ '\$likes.distance', 0]}, :#{#offset[1]}] },\n" +
                    "                            {\$gte: [{\$ifNull: ['\$distance', 0]}, :#{#offset[2]}] },\n" +
                    "                        ]\n" +
                    "            },\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "                \$addFields: {\n" +
                    "                    'groupKey': {\$multiply: [{\$floor : [{\$divide : ['\$distance', ?3]}]}, ?3]},\n" +
                    "                }\n" +
                    "            }",

            "{\n" +
                    "                \$addFields: {\n" +
                    "                    'likes.rate': {\$ifNull: [ '\$likes.rate', 0]},\n" +
                    "                }\n" +
                    "            }",

            "{\n" +
                    "        \$sort: {\n" +
                    "            'groupKey': 1, 'likes.rate': -1, 'likes.distance': 1\n" +
                    "        }\n" +
                    "    }",

            "{ \$limit : ?2 }",

            "{\n" +
                    "                \$replaceRoot: {  \n" +
                    "                    'newRoot': {  \n" +
                    "                        \$mergeObjects: [{'profile': '\$\$ROOT'}, { rate: {\$ifNull: ['\$likes.rate', 0]}, groupKey: '\$groupKey', offset: ['\$groupKey', '\$likes.rate', '\$likes.distance', Date()] } ] \n" +
                    "                    } \n" +
                    "                } \n" +
                    "            }"
        ]
    )
    fun findProfileNoType(
        loc: Point,
        @Param("offset") offset: Array<Any>, //minDistance
        limit: Int,
        step: Int,
        sProfileId: UUID,
        gender: String,
        groupId: UUID,
        cProfileId: UUID, // curr
        @Param("type") type: Double
    ): List<Profile>

    @Aggregation(
        pipeline = [
            "{\n" +
                    "        \$geoNear :\n" +
                    "        {\n" +
                    "            near :\n" +
                    "            ?1,\n" +
                    "            spherical     : true,\n" +
                    "            minDistance   : :#{#offset[0]},\n" +
                    "            distanceField : 'distance',\n" +
                    "            distanceMultiplier: 0.001,\n" +
                    "            query         : {'_id': { \$nin: [?0] }, 'group': ?4 }\n" +
                    "        }\n" +
                    "}",

            "{ \n" +
                    "                            \$lookup: \n" +
                    "                            { \n" +
                    "                                from: 'likes', \n" +
                    "                                let: { p_id: '\$_id' }, \n" +
                    "                                pipeline: [ \n" +
                    "                                    { \n" +
                    "                                        \$match: { \n" +
                    "                                            \$expr:  \n" +
                    "                                            { \n" +
                    "                                                \$or: [ \n" +
                    "                                                    { \n" +
                    "                                                        \$and: [ \n" +
                    "                                                            { \$eq: ['\$from.\$id', '\$\$p_id'] }, \n" +
                    "                                                            { \$eq: ['\$to.\$id', ?0 ] },\n" +
                    "                                                            {\n" +
                    "                                                               \$eq: [{\$ifNull: [ '\$ref', 0]}, 0]\n" +
                    "                                                            },\n" +
                    "                                                        ] \n" +
                    "                                                    }, \n" +
                    "                                                    { \n" +
                    "                                                        \$and: [ \n" +
                    "                                                            { \$eq: ['\$to.\$id', '\$\$p_id'] }, \n" +
                    "                                                            { \$eq: ['\$from.\$id', ?0 ] },\n" +
                    "                                                            {\n" +
                    "                                                               \$eq: [{\$ifNull: [ '\$ref', 0]}, 0]\n" +
                    "                                                            },\n" +
                    "                                                        ] \n" +
                    "                                                    },\n" +
                    "                                                ] \n" +
                    "                                            }, \n" +
                    "                                        } \n" +
                    "                                    }, \n" +
                    "                                    { \n" +
                    "                                        \$group: {\n" +
                    "                                            '_id': '\$\$p_id', \n" +
                    "                                            'distance': { \n" +
                    "                                                \$avg: '\$distance' \n" +
                    "                                            }, \n" +
                    "                                            'type': { \n" +
                    "                                                \$avg: { \n" +
                    "                                                    \$switch: { \n" +
                    "                                                        branches: [ \n" +
                    "                                                            {  \n" +
                    "                                                                case: {  \n" +
                    "                                                                    \$and: [ \n" +
                    "                                                                        { \$eq: ['\$to.\$id', ?0 ] }, \n" +
                    "                                                                        { \$eq: ['\$double', false] }, \n" +
                    "                                                                    ] \n" +
                    "                                                                }, \n" +
                    "                                                                then: 2 \n" +
                    "                                                            }, \n" +
                    "                                                            {  \n" +
                    "                                                                case: {  \n" +
                    "                                                                    \$and: [ \n" +
                    "                                                                        { \$eq: ['\$from.\$id', ?0 ] }, \n" +
                    "                                                                        { \$eq: ['\$double', false] }, \n" +
                    "                                                                    ] \n" +
                    "                                                                }, \n" +
                    "                                                                then: 1 \n" +
                    "                                                            }, \n" +
                    "                                                        ], \n" +
                    "                                                        default: 0 \n" +
                    "                                                    } \n" +
                    "                                                } \n" +
                    "                                            },\n" +
                    "                                            'rate': { \n" +
                    "                                                \$sum: { \n" +
                    "                                                    \$switch: { \n" +
                    "                                                        branches: [ \n" +
                    "                                                            {  \n" +
                    "                                                                case: {  \n" +
                    "                                                                    \$and: [ \n" +
                    "                                                                        { \$eq: ['\$createdBy.\$id', ?0 ] }, \n" +
                    "                                                                        { \$eq: ['\$double', true] }, \n" +
                    "                                                                    ] \n" +
                    "                                                                }, \n" +
                    "                                                                then: '\$rate' \n" +
                    "                                                            } \n" +
                    "                                                        ],\n" +
                    "                                                        default: 0 \n" +
                    "                                                    } \n" +
                    "                                                } \n" +
                    "                                            },\n" +
                    "                                            'createdDate': { \n" +
                    "                                                \$max: '\$createdDate' \n" +
                    "                                            }, \n" +
                    "                                        }, \n" +
                    "                                    },\n" +
                    "                                ], \n" +
                    "                                as: 'likes', \n" +
                    "                            } \n" +
                    "                        }",

            "{\n" +
                    "        \$unwind: { path: '\$likes', preserveNullAndEmptyArrays: true }\n" +
                    "    }",

            "{ \n" +
                    "                            \$match: { \n" +
                    "                                \$expr:  \n" +
                    "                                { \n" +
                    "                                            \$and: [\n" +
                    "                                                {\n" +
                    "                                                    \$or: [\n" +
                    "                                                        {\$eq: [ '\$status', 'A' ] },\n" +
                    "                                                        {\$eq: [ '\$status', 'F' ] }, \n" +
                    "                                                    ]\n" +
                    "                                                },\n" +
                    "                                                \n" +
                    "                                                {\$gte: [{\$ifNull: [ '\$likes.distance', 0]}, :#{#offset[1]} ] }, \n" +
                    "                                            ] \n" +
                    "                                }, \n" +
                    "                            } \n" +
                    "                        }",

            "{\n" +
                    "                \$addFields: {\n" +
                    "                    'groupKey': {\$multiply: [{\$floor : [{\$divide : ['\$distance', ?3]}]}, ?3]},\n" +
                    "                }\n" +
                    "            }",

            "{\n" +
                    "                \$addFields: {\n" +
                    "                    'likes.rate': {\$ifNull: [ '\$likes.rate', 0]},\n" +
                    "                }\n" +
                    "            }",

            // lookup members table to check whether profile was invited

            "{\n" +
                    "        \$lookup:\n" +
                    "        {\n" +
                    "            from: 'events',\n" +
                    "            let: { 'p_id': '\$_id' },\n" +
                    "            pipeline: [\n" +
                    "                {\n" +
                    "                    \$match: {\n" +
                    "                        \$expr: {\n" +
                    "                            \$and: [{\$in: [\n" +
                    "                                '\$\$p_id',\n" +
                    "                                '\$info.members.profile.\$id'\n" +
                    "                            ]},\n" +
                    "                            {\$gt: [{\$ifNull: [ '\$info.range.start', ISODate()]}, { \$toDate: :#{#offset[3]} }]}\n" +
                    "                            ]\n" +
                    "                        }\n" +
                    "                    }\n" +
                    "                },\n" +
                    "                {\n" +
                    "                    \$match: {\n" +
                    "                        'info.members' : { \n" +
                    "                            \$elemMatch: {\n" +
                    "                                'profile.\$id': ?0,\n" +
                    "                                'status': 'I'\n" +
                    "                            }\n" +
                    "                        }\n" +
                    "                    }\n" +
                    "                },\n" +
                    "            ],\n" +
                    "            as: 'events',\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$unwind: { path: '\$events' }\n" +
                    "    }",

            "{\n" +
                    "        \$sort: {\n" +
                    "            'groupKey': 1, 'rate': -1, 'distance': 1\n" +
                    "        }\n" +
                    "}",

            "{ \$limit : ?2 }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            'newRoot': { \n" +
                    "                \$mergeObjects: [{'event': '\$events'}, { groupKey: '\$groupKey', isPromotion: { \$cond: [ {\$ifNull: ['\$ref', false] }, true, false ] }, offset: ['\$groupKey', '\$rate', '\$distance', '\$createdDate'] } ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }"
        ]
    )
    fun findInvitationByProfile(
        currentId: UUID,
        loc: Point,
        limit: Int,
        step: Int,
        group: UUID,
        @Param("offset") offset: Array<Any>,
        @Param("type") type: Double
    ): List<Event>

    @Aggregation(
        pipeline = [
            "{\n" +
                    "        \$geoNear :\n" +
                    "        {\n" +
                    "            near :\n" +
                    "            ?1,\n" +
                    "            spherical     : true,\n" +
                    "            minDistance   : :#{#offset[0]},\n" +
                    "            distanceField : 'distance',\n" +
                    "            distanceMultiplier: 0.001,\n" +
                    "            query         : {'_id': { \$nin: [?0] }, 'gender': ?4, 'group': ?5 }\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$lookup:\n" +
                    "        {\n" +
                    "            from: 'likes',\n" +
                    "            let: { p_id: '\$_id' },\n" +
                    "            pipeline: [\n" +
                    "                {\n" +
                    "                    \$match: {\n" +
                    "                        \$expr: \n" +
                    "                        {\n" +
                    "                            \$or: [\n" +
                    "                                {\n" +
                    "                                    \$and: [\n" +
                    "                                        { \$eq: ['\$from.\$id', '\$\$p_id'] },\n" +
                    "                                        { \$eq: ['\$to.\$id', ?0 ] },\n" +
                    "                                    ]\n" +
                    "                                },\n" +
                    "                                {\n" +
                    "                                    \$and: [\n" +
                    "                                        { \$eq: ['\$to.\$id', '\$\$p_id'] },\n" +
                    "                                        { \$eq: ['\$from.\$id', ?0 ] },\n" +
                    "                                    ]\n" +
                    "                                }\n" +
                    "                            ]\n" +
                    "                        },\n" +
                    "                    }\n" +
                    "                },\n" +
                    "                {\n" +
                    "                    \$project: {\n" +
                    "                        'distance': {\n" +
                    "                            \$avg: '\$distance'\n" +
                    "                        },\n" +
                    "                        'type': {\n" +
                    "                            \$avg: {\n" +
                    "                                \$switch: {\n" +
                    "                                    branches: [\n" +
                    "                                        { \n" +
                    "                                            case: { \n" +
                    "                                                \$and: [\n" +
                    "                                                    { \$eq: ['\$to.\$id', ?0 ] },\n" +
                    "                                                    { \$eq: ['\$double', false] },\n" +
                    "                                                ]\n" +
                    "                                            },\n" +
                    "                                            then: 2\n" +
                    "                                        },\n" +
                    "                                        { \n" +
                    "                                            case: { \n" +
                    "                                                \$and: [\n" +
                    "                                                    { \$eq: ['\$from.\$id', ?0 ] },\n" +
                    "                                                    { \$eq: ['\$double', false] },\n" +
                    "                                                ]\n" +
                    "                                            },\n" +
                    "                                            then: 1\n" +
                    "                                        }\n" +
                    "                                    ],\n" +
                    "                                    default: 0\n" +
                    "                                }\n" +
                    "                            }\n" +
                    "                        },\n" +
                    "                        'createdDate': {\n" +
                    "                            \$max: '\$createdDate'\n" +
                    "                        },\n" +
                    "                        data: '\$\$ROOT'\n" +
                    "                    }\n" +
                    "                },\n" +
                    "            ],\n" +
                    "            as: 'likes',\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$unwind: { path: '\$likes', preserveNullAndEmptyArrays: true }\n" +
                    "    }",
            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: \n" +
                    "            {\n" +
                    "                        \$and: [\n" +
                    "                            {\$eq: [ '\$status', 'A' ] },\n" +
                    "                            {\$eq: [ {\$ifNull: ['\$likes.type', 0]}, :#{#type} ]},\n" +
                    "                            {\$gte: [{\$ifNull: [ '\$likes.distance', 0]}, :#{#offset[1]}] },\n" +
                    "                        ]\n" +
                    "            },\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$group: {\n" +
                    "            '_id': '\$_id',\n" +
                    "            'likes': {\n" +
                    "                \$avg: '\$likes.distance'\n" +
                    "            },\n" +
                    "            'type': {\n" +
                    "                \$avg: '\$likes.type'\n" +
                    "            },\n" +
                    "            'rate': {\n" +
                    "                \$avg: {\n" +
                    "                    \$switch: {\n" +
                    "                        branches: [\n" +
                    "                            { \n" +
                    "                                case: { \n" +
                    "                                    \$eq: ['\$likes.type', 1 ] \n" +
                    "                                },\n" +
                    "                                then: '\$likes.data.rate'\n" +
                    "                            }\n" +
                    "                        ],\n" +
                    "                        default: 0\n" +
                    "                    }\n" +
                    "                }\n" +
                    "            },\n" +
                    "            'groupKey': {\n" +
                    "                \$first : {\$multiply: [{\$floor : [{\$divide : ['\$distance', ?3]}]}, ?3]}\n" +
                    "            },\n" +
                    "            'distance': {\n" +
                    "                \$first : '\$distance'\n" +
                    "            },\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$lookup:\n" +
                    "        {\n" +
                    "            from: 'events',\n" +
                    "            let: { 'p_id': '\$_id' },\n" +
                    "            pipeline: [\n" +
                    "                {\n" +
                    "                    \$match: {\n" +
                    "                        \$expr: {\n" +
                    "                            \$and: [\n" +
                    "                                {\$in: ['\$\$p_id','\$info.members.profile.\$id']},\n" +
                    "                                {\$not: {\$in: [?0, '\$info.members.profile.\$id']} },\n" +
                    "                                {\$gt: [{\$ifNull: [ '\$createdDate', ISODate()]}, { \$toDate: :#{#offset[4]} }]}\n" +
                    "                            ]\n" +
                    "                        }\n" +
                    "                    }\n" +
                    "                }\n" +
                    "            ],\n" +
                    "            as: 'events',\n" +
                    "        }\n" +
                    "}",

            // unwind members from here, not events
            "{\n" +
                    "        \$unwind: { path: '\$events' }\n" +
                    "    }",

            "{\n" +
                    "        \$unwind: { path: '\$events.info.members' }\n" +
                    "    }",

            "{\n" +
                    "        \$group: {\n" +
                    "            '_id': '\$events.info.members.profile.\$id',\n" +
                    "            'rate': {\n" +
                    "                \$avg: '\$events.rate'\n" +
                    "            },\n" +
                    "            'createdDate': {\n" +
                    "                \$min: '\$createdDate'\n" +
                    "            },\n" +
                    "            'groupKey': {\n" +
                    "                \$min: '\$groupKey'\n" +
                    "            },\n" +
                    "            'likes': {\n" +
                    "                \$avg: '\$likes'\n" +
                    "            },\n" +
                    "            'distance': {\n" +
                    "                \$min: '\$distance'\n" +
                    "            },\n" +
                    "            data: { '\$first': '\$events.info.members.profile' },\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: {\n" +
                    "                \$and: [\n" +
                    "                    {\$gte: [{\$ifNull: [ '\$rate', 0]}, :#{#offset[2]} ]},\n" +
                    "                    {\$gte: [{\$ifNull: ['\$distance', 0]}, :#{#offset[3]} ]},\n" +
                    "                ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$sort: {\n" +
                    "            'groupKey': 1, 'likes': -1, 'rate': -1, 'distance': 1\n" +
                    "        }\n" +
                    "}",

            "{ \$limit : ?2 }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            'newRoot': { \n" +
                    "                \$mergeObjects: [{'profile': '\$data'}, { groupKey: '\$groupKey', offset: ['\$groupKey', {\$ifNull: [ '\$likes', 0]}, '\$rate', '\$distance', '\$createdDate'] } ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }"
        ]
    )
    // rate avg needs to be fixed based on previous methods
    // TODO: merge into games/non_rated query
    @Deprecated("profiles recommendation removed")
    fun findPeopleByProfile(
        currentId: UUID,
        loc: Point,
        limit: Int,
        step: Int,
        gender: String,
        groupId: UUID,
        @Param("offset") offset: Array<Any>,
        @Param("type") type: Double
    ): List<Profile>

    @Aggregation(
        pipeline = [
            "{\n" +
                    "        \$match: {\n" +
                    "            '_id': ?0\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$lookup: {\n" +
                    "            from: 'cars',\n" +
                    "            localField: 'cars.\$id',\n" +
                    "            foreignField: '_id',\n" +
                    "            as: 'cars'\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$unwind: '\$cars'\n" +
                    "    }",

            "{\n" +
                    "        \$group: {\n" +
                    "            '_id': '\$cars._id',\n" +
                    "            data: {\n" +
                    "                \$first: '\$cars'\n" +
                    "            }\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: \n" +
                    "            {\n" +
                    "                \$and: [\n" +
                    "                    {\$eq: [ '\$data.status', 'A' ] },\n" +
                    "                    {\$gt: [ {\$ifNull: [ '\$data.createdDate', ISODate()]}, { \$toDate: :#{#offset[0]} }] },\n" +
                    "                ]\n" +
                    "            },\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$sort: {\n" +
                    "            'data.createdDate': -1\n" +
                    "        }\n" +
                    "}",

            "{ \$limit : ?1 }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            'newRoot': { \n" +
                    "                \$mergeObjects: [{'car': '\$data'}, { offset: ['\$data.createdDate'] } ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "}"
        ]
    )
    fun findCarsByProfile(
        profileId: UUID,
        limit: Int,
        step: Int,
        @Param("offset") offset: Array<Any>
    ): List<Car>

    @Aggregation(
        pipeline = [
            "{\n" +
                    "        \$match: {\n" +
                    "            '_id': ?0\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$unwind: '\$cars'\n" +
                    "    }",

            "{\n" +
                    "        \$lookup:\n" +
                    "        {\n" +
                    "            from: 'cars',\n" +
                    "            let: { 'c_id': '\$cars.\$id' },\n" +
                    "            pipeline: [\n" +
                    "                {\n" +
                    "                    \$match: {\n" +
                    "                        \$expr: {\n" +
                    "                            \$and: [ \n" +
                    "                                { \$eq: ['\$status', 'A'] },\n" +
                    "                                { \$eq: ['\$\$c_id', ?1] }\n" +
                    "                            ]\n" +
                    "                        }\n" +
                    "                    },\n" +
                    "                },\n" +
                    "            ],\n" +
                    "            as: 'cars'\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$unwind: '\$cars'\n" +
                    "    }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            'newRoot': '\$cars'\n" +
                    "        }\n" +
                    "    }"
                ]
    )
    fun findCarByProfile(profileId: UUID, carId: UUID): Optional<com.raxim.myscoutee.profile.data.document.mongo.Car>

    @Aggregation(
        pipeline = [
            "{\n" +
                    "        \$match: {\n" +
                    "            '_id': ?0\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$unwind: '\$schools'\n" +
                    "    }",

            "{\n" +
                    "        \$lookup:\n" +
                    "        {\n" +
                    "            from: 'schools',\n" +
                    "            let: { 's_id': '\$schools.\$id' },\n" +
                    "            pipeline: [\n" +
                    "                {\n" +
                    "                    \$match: {\n" +
                    "                        \$expr: {\n" +
                    "                            \$and: [ \n" +
                    "                                { \$eq: ['\$status', 'A'] },\n" +
                    "                                { \$eq: ['\$\$s_id', ?1] }\n" +
                    "                            ]\n" +
                    "                        }\n" +
                    "                    },\n" +
                    "                },\n" +
                    "            ],\n" +
                    "            as: 'schools'\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$unwind: '\$schools'\n" +
                    "    }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            'newRoot': '\$schools'\n" +
                    "        }\n" +
                    "    }"
        ]
    )
    fun findSchoolByProfile(profileId: UUID, schoolId: UUID): Optional<com.raxim.myscoutee.profile.data.document.mongo.School>

    @Aggregation(
        pipeline = [
            "{\n" +
                    "        \$match: {\n" +
                    "            '_id': ?0\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$lookup: {\n" +
                    "            from: 'schools',\n" +
                    "            localField: 'schools.\$id',\n" +
                    "            foreignField: '_id',\n" +
                    "            as: 'schools'\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$unwind: '\$schools'\n" +
                    "}",

            "{\n" +
                    "        \$group: {\n" +
                    "            '_id': '\$schools._id',\n" +
                    "            data: { '\$first': '\$schools' },\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: \n" +
                    "            {\n" +
                    "                \$and: [\n" +
                    "                    {\$eq: [ '\$data.status', 'A' ] },\n" +
                    "                    {\$gte: [ '\$data.type', :#{#offset[0]} ] },\n" +
                    "                    {\$gt: [ '\$data.range.start', { \$toDate: :#{#offset[1]} } ] },\n" +
                    "                    {\$gt: [ {\$ifNull: [ '\$data.createdDate', ISODate()]}, { \$toDate: :#{#offset[2]} }] },\n" +
                    "                ]\n" +
                    "            },\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$sort: {\n" +
                    "            'data.type': 1, 'data.range.start': 1, 'data.createdDate': 1\n" +
                    "        }\n" +
                    "}",

            "{ \$limit : ?1 }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            'newRoot': { \n" +
                    "                \$mergeObjects: [{'school': '\$data'}, { groupKey: '\$data.type', offset: ['\$data.type', '\$data.range.start', '\$data.createdDate'] } ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "}"
        ]
    )
    fun findSchoolsByProfile(
        profileId: UUID,
        limit: Int,
        step: Int,
        @Param("offset") offset: Array<Any>
    ): List<School>

    @Aggregation(
        pipeline = [
            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: \n" +
                    "            {\n" +
                    "                \$lt: [{\$ifNull: [ \$createdDate, ISODate()]}, { \$toDate: :#{#offset[0]} }]\n" +
                    "            }\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$sort: {\n" +
                    "            createdDate: -1\n" +
                    "        }\n" +
                    "}",

            "{ \$limit : ?0 }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            newRoot: { \n" +
                    "                \$mergeObjects: [{profile: \$\$ROOT}, { offset: [\$createdDate] } ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "}"
        ]
    )
    fun findAllProfiles(
        limit: Int,
        step: Int,
        @Param("offset") offset: Array<Any>
    ): List<Profile>

    @Aggregation(
        pipeline = [
            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: {\n" +
                    "                \$and: [\n" +
                    "                    { \$eq: ['\$group', ?1] },\n" +
                    "                    { \$ne: ['\$_id', ?0 ] },\n" +
                    "                    {\n" +
                    "                        \$or: [\n" +
                    "                            { \$eq: ['\$status', 'A' ] },\n" +
                    "                            { \$eq: ['\$status', 'F' ] },\n" +
                    "                            { \$eq: ['\$status', 'P' ] },\n" +
                    "                        ]\n" +
                    "                    },\n" +
                    "                ]\n" +
                    "                    \n" +
                    "            }\n" +
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
                    "        \$match: {\n" +
                    "            \$expr: \n" +
                    "            {\n" +
                    "                \$gt: [ {\$ifNull: [ '\$createdDate', ISODate()]}, { \$toDate: :#{#offset[0]} }],\n" +
                    "            }\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$sort: {\n" +
                    "            createdDate: -1\n" +
                    "        }\n" +
                    "}",

            "{ \$limit : ?2 }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            newRoot: { \n" +
                    "                \$mergeObjects: [{profile: \$\$ROOT}, { role: '\$role.role', offset: [\$createdDate] } ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "}"
        ]
    )
    fun findProfilesByGroup(
        profileId: UUID,
        groupId: UUID,
        limit: Int,
        step: Int,
        @Param("offset") offset: Array<Any>
    ): List<Profile>
}

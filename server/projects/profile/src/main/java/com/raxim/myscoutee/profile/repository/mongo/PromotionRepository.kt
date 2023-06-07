package com.raxim.myscoutee.profile.repository.mongo

import com.mongodb.client.model.geojson.Point
import com.raxim.myscoutee.profile.data.dto.rest.*
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PromotionRepository : MongoRepository<com.raxim.myscoutee.profile.data.document.mongo.Promotion, UUID> {

    @Query("{'events.\$id' : ?}")
    fun findPromotionByEvent(eventId: UUID): Optional<com.raxim.myscoutee.profile.data.document.mongo.Promotion>

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
                    "                    {\$gt: [ '\$range.start', { \$toDate: :#{#offset[0]} } ] },\n" +
                    "                    {\$gt: [ {\$ifNull: [ '\$createdDate', ISODate()]}, { \$toDate: :#{#offset[1]} }] },\n" +
                    "                ]\n" +
                    "            },\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$sort: {\n" +
                    "            'range.start': 1, 'type': 1, 'createdDate': 1\n" +
                    "        }\n" +
                    "}",

            "{ \$limit : ?1 }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            'newRoot': { \n" +
                    "                \$mergeObjects: [{'promotion': '\$\$ROOT'}, { groupKey: '\$createdDate', offset: ['\$range.start', '\$type', '\$createdDate'] } ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "}"
        ]
    )
    fun findPromotionsByProfile(
        profileId: UUID,
        limit: Int,
        step: Int,
        @Param("offset") offset: Array<Any>
    ): List<Promotion>

    @Aggregation(
        pipeline = [
            "{\n" +
                    "        \$match: {\n" +
                    "            '_id': ?0\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$lookup: {\n" +
                    "            from: 'events',\n" +
                    "            localField: 'events.\$id',\n" +
                    "            foreignField: '_id',\n" +
                    "            as: 'events'\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$unwind: '\$events'\n" +
                    "}",

            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: \n" +
                    "            {\n" +
                    "                \$and: [\n" +
                    "                    {\$eq: [ '\$events.status', 'P' ] },\n" +
                    "                    {\$gt: [ '\$events.cnt', 0 ] },\n" +
                    "                ]\n" +
                    "            },\n" +
                    "        }\n" +
                    "}",

            "{ \n" +
                    "                            \$lookup: \n" +
                    "                            { \n" +
                    "                                from: 'likes', \n" +
                    "                                let: { 'p_id': '\$events._id' }, \n" +
                    "                                pipeline: [ \n" +
                    "                                    { \n" +
                    "                                        \$match: { \n" +
                    "                                            \$expr: { \n" +
                    "                                                \$and: [\n" +
                    "                                                    {\$eq: [ '\$\$p_id', '\$ref' ]},\n" +
                    "                                                    {\$eq: [ '\$from.\$id', ?4 ]},\n" +
                    "                                                ] \n" +
                    "                                            } \n" +
                    "                                        } \n" +
                    "                                    },\n" +
                    "                                ], \n" +
                    "                                as: 'likes', \n" +
                    "                            } \n" +
                    "                    }",

            "{ \n" +
                    "                            \$unwind: { path: '\$likes', preserveNullAndEmptyArrays: true } \n" +
                    "                        }",


            "{ \n" +
                    "                            \$group: { \n" +
                    "                                '_id': '\$events._id', \n" +
                    "                                'data': { '\$first': '\$events' },\n" +
                    "                                'groupType': { '\$first': '\$groupType' },\n" +
                    "                                'promoType': { '\$first': '\$type' },\n" +
                    "                                'rate': { '\$first': {\$ifNull: [ '\$likes.rate', 0]} },\n" +
                    "                            } \n" +
                    "                    }",

            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: \n" +
                    "            {\n" +
                    "                \$and: [\n" +
                    "                    {\$gt: [ '\$data.info.range.start', { \$toDate: :#{#offset[0]} } ] },\n" +
                    "                    {\$lte: [ '\$rate', :#{#offset[1]} ] },\n" +
                    "                    {\$gt: [ {\$ifNull: [ '\$data.createdDate', ISODate()]}, { \$toDate: :#{#offset[2]} }] },\n" +
                    "                ]\n" +
                    "            },\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$sort: {\n" +
                    "            'data.info.range.start': 1, 'rate': -1, 'data.createdDate': 1\n" +
                    "        }\n" +
                    "}",

            "{ \$limit : ?1 }",

            "{\n" +
                    "        \$addFields: {\n" +
                    "            'groupKey': {\n" +
                    "                \$dateToString: {\n" +
                    "                    format: ?3,\n" +
                    "                    date: '\$data.info.range.start'\n" +
                    "                }\n" +
                    "            },\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            'newRoot': { \n" +
                    "                \$mergeObjects: [{'event': '\$data'}, { groupKey: '\$groupKey', promoType: '\$promoType', rate: '\$rate', groupType: '\$groupType', offset: ['\$data.info.range.start', '\$rate', '\$data.createdDate'] } ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "}"
        ]
    )
    fun findEventsByPromotion(
        promotionId: UUID,
        limit: Int,
        step: Int,
        format: String,
        profileId: UUID,
        @Param("offset") offset: Array<Any>
    ): List<Event>

    @Aggregation(
        pipeline = [
            "{\n" +
                    "        \$match: {\n" +
                    "            '_id': ?0\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$lookup: {\n" +
                    "            from: 'events',\n" +
                    "            localField: 'events.\$id',\n" +
                    "            foreignField: '_id',\n" +
                    "            as: 'events'\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$unwind: '\$events'\n" +
                    "}",

            "{ \n" +
                    "                            \$lookup: \n" +
                    "                            { \n" +
                    "                                from: 'likes', \n" +
                    "                                let: { 'p_id': '\$events._id' }, \n" +
                    "                                pipeline: [ \n" +
                    "                                    { \n" +
                    "                                        \$match: { \n" +
                    "                                            \$expr: { \n" +
                    "                                                \$and: [\n" +
                    "                                                    {\$eq: [ '\$\$p_id', '\$ref' ]},\n" +
                    "                                                ] \n" +
                    "                                            } \n" +
                    "                                        } \n" +
                    "                                    },\n" +
                    "                                ], \n" +
                    "                                as: 'likes', \n" +
                    "                            } \n" +
                    "                    }",

            "{ \n" +
                    "                            \$unwind: { path: '\$likes', preserveNullAndEmptyArrays: true } \n" +
                    "                        }",

            "{ \n" +
                    "                                        \$match: { \n" +
                    "                                            \$expr: { \n" +
                    "                                                \$and: [\n" +
                    "                                                    {\$ne: [ '\$likes.from.\$id', ?3 ]},\n" +
                    "                                                ] \n" +
                    "                                            } \n" +
                    "                                        } \n" +
                    "                                    }",

            "{ \n" +
                    "                            \$lookup: \n" +
                    "                            { \n" +
                    "                                from: 'profiles', \n" +
                    "                                let: { 'p_id': '\$likes.from.\$id', 'g_id': '\$group.\$id' }, \n" +
                    "                                pipeline: [ \n" +
                    "                                    { \n" +
                    "                                        \$match: { \n" +
                    "                                            \$expr: { \n" +
                    "                                                \$and: [\n" +
                    "                                                    {\$eq: [ '\$\$g_id', '\$group' ]},\n" +
                    "                                                    {\$eq: [ '\$\$p_id', '\$_id' ]},\n" +
                    "                                                ] \n" +
                    "                                            } \n" +
                    "                                        } \n" +
                    "                                    },\n" +
                    "                                ], \n" +
                    "                                as: 'profiles', \n" +
                    "                            } \n" +
                    "                    }",

            "{ \n" +
                    "                            \$unwind: { path: '\$profiles' } \n" +
                    "                        }",

            "{ \n" +
                    "                            \$lookup: \n" +
                    "                            { \n" +
                    "                                from: 'likes', \n" +
                    "                                let: { 'p_id': '\$profiles._id', 'e_id': '\$events._id' }, \n" +
                    "                                pipeline: [ \n" +
                    "                                    { \n" +
                    "                                        \$match: { \n" +
                    "                                            \$expr: { \n" +
                    "                                                \$and: [\n" +
                    "                                                    {\$eq: [ '\$\$p_id', '\$to.\$id' ]},\n" +
                    "                                                    {\$eq: [ '\$\$e_id', '\$ref' ]},\n" +
                    "                                                ] \n" +
                    "                                            } \n" +
                    "                                        } \n" +
                    "                                    },\n" +
                    "                                ], \n" +
                    "                                as: 'likes', \n" +
                    "                            } \n" +
                    "                    }",

            "{ \n" +
                    "                            \$unwind: { path: '\$likes', preserveNullAndEmptyArrays: true } \n" +
                    "                        }",

            "{ \n" +
                    "                            \$group: { \n" +
                    "                                '_id': '\$profiles._id', \n" +
                    "                                'data': { '\$first': '\$profiles' },\n" +
                    "                                'ref': { '\$first': '\$events._id' },\n" +
                    "                                'rate': { '\$first': {\$ifNull: [ '\$likes.rate', 0]} },\n" +
                    "                            } \n" +
                    "                    }",

            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: \n" +
                    "            {\n" +
                    "                \$and: [\n" +
                    "                    {\$lte: [ '\$rate', :#{#offset[0]} ] },\n" +
                    "                    {\$gt: [ {\$ifNull: [ '\$data.createdDate', ISODate()]}, { \$toDate: :#{#offset[1]} }] },\n" +
                    "                ]\n" +
                    "            },\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$sort: {\n" +
                    "            'rate': -1, 'data.createdDate': 1\n" +
                    "        }\n" +
                    "}",

            "{ \$limit : ?1 }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            'newRoot': { \n" +
                    "                \$mergeObjects: [{'profile': '\$data'}, { ref: '\$ref', rate: '\$rate', offset: ['\$rate', '\$data.createdDate'] } ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "}"
        ]
    )
    // TODO: order events by info.range.start at 'unwind' events
    fun findProfilesByPromotion(
        promotionId: UUID,
        limit: Int,
        step: Int,
        profileId: UUID,
        @Param("offset") offset: Array<Any>
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
                    "            from: 'events',\n" +
                    "            localField: 'events.\$id',\n" +
                    "            foreignField: '_id',\n" +
                    "            as: 'events'\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$unwind: '\$events'\n" +
                    "}",

            "{\n" +
                    "        \$group: {\n" +
                    "            '_id': '\$events._id',\n" +
                    "            data: { '\$first': '\$events' },\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: \n" +
                    "            {\n" +
                    "                \$and: [\n" +
                    "                    {\$gte: [ '\$data.type', :#{#offset[0]} ] },\n" +
                    "                    {\$gt: [ '\$data.info.range.start', { \$toDate: :#{#offset[1]} } ] },\n" +
                    "                    {\$gt: [ {\$ifNull: [ '\$data.createdDate', ISODate()]}, { \$toDate: :#{#offset[2]} }] },\n" +
                    "                ]\n" +
                    "            },\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$sort: {\n" +
                    "            'data.info.range.start': 1, 'data.type': 1, 'data.createdDate': 1\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$lookup:\n" +
                    "        {\n" +
                    "            from: 'profiles',\n" +
                    "            let: { 'p_id': '\$data.info.members.profile.\$id', 'p_members' : '\$data.info.members' },\n" +
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
    fun findMembersByPromotion(
        promotionId: UUID,
        limit: Int,
        step: Int,
        @Param("status") status: Array<String>,
        @Param("offset") offset: Array<Any>
    ): List<Member>

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

                    // 'type': ?4, 'group': ?5
                    "            query         : { 'group.\$id': ?5 }\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: \n" +
                    "            {\n" +
                    "                \$and: [\n" +
                    "                    {\$gt: [ '\$cnt', 0 ] }\n" +
                    "                ]\n" +
                    "            },\n" +
                    "        }\n" +
                    "}",

            "{\n" +
                    "                \$addFields: {\n" +
                    "                    'groupKey': {\$multiply: [{\$floor : [{\$divide : ['\$distance', ?3]}]}, ?3]},\n" +
                    "                }\n" +
                    "            }",

            // 'rate': -1,
            "{\n" +
                    "        \$sort: {\n" +
                    "            'groupKey': 1, 'distance': 1, createdDate: -1\n" +
                    "        }\n" +
                    "}",

            "{ \$limit : ?2 }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            'newRoot': { \n" +
                    "                \$mergeObjects: [{'promotion': '\$\$ROOT'}, { groupKey: '\$groupKey', offset: ['\$groupKey', '\$distance', '\$createdDate'] } ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }"
        ]
    )
    fun findPromotionsByRec(
        currentId: UUID,
        loc: Point,
        limit: Int,
        step: Int,
        type: String,
        groupId: UUID,
        @Param("offset") offset: Array<Any>
    ): List<Promotion>

    @Aggregation(
        pipeline = [
            "{ \n" +
                    "            \$match: { \n" +
                    "                createdBy: ?0 \n" +
                    "            } \n" +
                    "    }",
            "{ \n" +
                    "            \$unwind: '\$events'\n" +
                    "    }",
            "{ \n" +
                    "            \$lookup: \n" +
                    "            { \n" +
                    "                from: 'events', \n" +
                    "                let: { 'e_id': '\$events.\$id' },\n" +
                    "                pipeline: [ \n" +
                    "                    { \n" +
                    "                        \$match: { \n" +
                    "                            \$expr: { \n" +
                    "                                \$and: [\n" +
                    "                                    {\$eq: [ '\$status', 'P' ]},\n" +
                    "                                    {\$eq: [ '\$\$e_id', '\$ref.\$id' ]},\n" +
                    "                                ] \n" +
                    "                            } \n" +
                    "                        } \n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \$project: {\n" +
                    "                            '_id': '\$_id',\n" +
                    "                            'cap': {\n" +
                    "                                \$size: {\n" +
                    "                                    \$filter: {\n" +
                    "                                        input: '\$info.members',\n" +
                    "                                        as: \"member\",\n" +
                    "                                        cond: { \$eq: [ '\$\$member.status', 'A' ] }\n" +
                    "                                    },    \n" +
                    "                                }\n" +
                    "                            },\n" +
                    "                            'data': '\$\$ROOT'\n" +
                    "                        }\n" +
                    "                    },\n" +
                    "                    { \n" +
                    "                        \$match: { \n" +
                    "                            \$expr: { \n" +
                    "                                \$and: [\n" +
                    "                                    {\$gte: [ '\$cap', '\$data.capacity.min' ]},\n" +
                    "                                ] \n" +
                    "                            } \n" +
                    "                        } \n" +
                    "                    },\n" +
                    "                    { \n" +
                    "                        \$replaceRoot: {  \n" +
                    "                            'newRoot': '\$data' \n" +
                    "                        } \n" +
                    "                    },\n" +
                    "                ], \n" +
                    "                as: 'events', \n" +
                    "            } \n" +
                    "    }",

            "{ \n" +
                    "            \$unwind: '\$events'\n" +
                    "    }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            'newRoot': '\$events'\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: \n" +
                    "            {\n" +
                    "                \$and: [\n" +
                    "                    {\$gte: [ '\$info.range.start', { \$toDate: :#{#offset[0]} } ] },\n" +
                    "                    {\$gt: [ '\$createdDate', { \$toDate: :#{#offset[1]} }] },\n" +
                    "                ]\n" +
                    "            },\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$sort: {\n" +
                    "            'info.range.start': 1, 'createdDate': 1\n" +
                    "        }\n" +
                    "    }",

            "{ \$limit : ?1 }",

            "{\n" +
                    "        \$addFields: {\n" +
                    "            'groupKey': {\n" +
                    "                \$dateToString: {\n" +
                    "                    format: ?3,\n" +
                    "                    date: '\$info.range.start'\n" +
                    "                }\n" +
                    "            },\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            'newRoot': { \n" +
                    "                \$mergeObjects: [{'event': '\$\$ROOT'}, { groupKey: '\$groupKey', sortKey: { \$toLong: '\$info.range.start' }, offset: ['\$info.range.start', '\$createdDate'] } ]\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }"
        ]
    )
    fun findFullEventsByPromoter(
        profileId: UUID, limit: Int,
        step: Int,
        format: String,
        @Param("offset") offset: Array<Any>
    ): List<Event>

}
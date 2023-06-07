package com.raxim.myscoutee.profile.repository.mongo

import com.mongodb.client.model.geojson.Point
import com.raxim.myscoutee.profile.data.document.mongo.Group
import com.raxim.myscoutee.profile.data.document.mongo.Role
import com.raxim.myscoutee.profile.data.dto.rest.Car
import com.raxim.myscoutee.profile.data.dto.rest.Profile
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface GroupRepository : MongoRepository<Group, UUID>,
    QuerydslPredicateExecutor<Group> {

    @Query("{system: true}")
    fun findSystemGroups(): List<Group>

    @Query("{type: ?0, system: true}")
    fun findSystemGroupByType(type: String): Group

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
                    "            query         : {'_id': { \$nin: ?4 }, 'type': ?0, 'system': false, 'visibility': 'pu' }\n" +
                    "        }\n" +
                    "}",

            "{ \n" +
                    "                            \$match: { \n" +
                    "                                \$expr:  \n" +
                    "                                { \n" +
                    "                                            \$and: [\n" +
                    "                                                {\$gt: [{\$ifNull: [ '\$createdDate', ISODate()]}, {\$toDate: :#{#offset[1]} }] }, \n" +
                    "                                                {\$gte: ['\$distance', :#{#offset[2]} ] }, \n" +
                    "                                            ] \n" +
                    "                                }, \n" +
                    "                            } \n" +
                    "                        }",

            "{ \n" +
                    "                                    \$addFields: { \n" +
                    "                                        'groupKey': {\$multiply: [{\$floor : [{\$divide : ['\$distance', ?3]}]}, ?3]}, \n" +
                    "                                    } \n" +
                    "                                }",

            "{ \n" +
                    "                            \$sort: { \n" +
                    "                                'groupKey': 1, 'distance': 1, 'createdDate': -1 \n" +
                    "                            } \n" +
                    "                    }",

            "{ \$limit : ?2 }",

            "{ \n" +
                    "                \$replaceRoot: {  \n" +
                    "                    'newRoot': {  \n" +
                    "                        \$mergeObjects: [{'group': '\$\$ROOT'}, { groupKey: '\$groupKey', offset: ['\$groupKey', '\$distance', '\$createdDate'] } ] \n" +
                    "                    } \n" +
                    "                } \n" +
                    "            }"
        ]
    )
    fun findGroupByProfile(
        type: String,
        loc: Point,
        limit: Int,
        step: Int,
        uuids: List<UUID>,
        @Param("offset") offset: Array<Any>
    ): List<com.raxim.myscoutee.profile.data.dto.rest.Group>

}
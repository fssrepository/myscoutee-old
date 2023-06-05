package com.raxim.myscoutee.profile.repository.mongo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mongodb.client.model.geojson.Point;
import com.raxim.myscoutee.profile.data.document.mongo.Group;

@Repository
public interface GroupRepository extends MongoRepository<Group, UUID>,
                QuerydslPredicateExecutor<Group> {

        @Query("{system: true}")
        List<Group> findSystemGroups();

        @Query("{type: ?0, system: true}")
        Group findSystemGroupByType(String type);

        @Aggregation(pipeline = {
                        "{\n" +
                                        "        $geoNear :\n" +
                                        "        {\n" +
                                        "            near :\n" +
                                        "            ?1,\n" +
                                        "            spherical     : true,\n" +
                                        "            minDistance   : :#{#offset[0]},\n" +
                                        "            distanceField : 'distance',\n" +
                                        "            distanceMultiplier: 0.001,\n" +
                                        "            query         : {'_id': { $nin: ?4 }, 'type': ?0, 'system': false, 'visibility': 'pu' }\n"
                                        +
                                        "        }\n" +
                                        "}",

                        "{ \n" +
                                        "                            $match: { \n" +
                                        "                                $expr:  \n" +
                                        "                                { \n" +
                                        "                                            $and: [\n" +
                                        "                                                {$gt: [{$ifNull: [ '$createdDate', ISODate()]}, {$toDate: :#{#offset[1]} }] }, \n"
                                        +
                                        "                                                {$gte: ['$distance', :#{#offset[2]} ] }, \n"
                                        +
                                        "                                            ] \n" +
                                        "                                }, \n" +
                                        "                            } \n" +
                                        "                        }",

                        "{ \n" +
                                        "                                    $addFields: { \n" +
                                        "                                        'groupKey': {$multiply: [{$floor : [{$divide : ['$distance', ?3]}]}, ?3]}, \n"
                                        +
                                        "                                    } \n" +
                                        "                                }",

                        "{ \n" +
                                        "                            $sort: { \n" +
                                        "                                'groupKey': 1, 'distance': 1, 'createdDate': -1 \n"
                                        +
                                        "                            } \n" +
                                        "                    }",

                        "{ $limit : ?2 }",

                        "{ \n" +
                                        "                $replaceRoot: {  \n" +
                                        "                    'newRoot': {  \n" +
                                        "                        $mergeObjects: [{'group': '$$ROOT'}, { groupKey: '$groupKey', offset: ['$groupKey', '$distance', '$createdDate'] } ] \n"
                                        +
                                        "                    } \n" +
                                        "                } \n" +
                                        "            }"
        })
        List<com.raxim.myscoutee.profile.data.dto.rest.Group> findGroupByProfile(
                        String type,
                        Point loc,
                        int limit,
                        int step,
                        List<UUID> uuids,
                        @Param("offset") Object[] offset);
}

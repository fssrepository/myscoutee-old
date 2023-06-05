package com.raxim.myscoutee.profile.repository.mongo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.raxim.myscoutee.profile.data.document.mongo.Link;
import com.raxim.myscoutee.profile.data.dto.rest.Reward;

@Repository
public
interface LinkRepository extends MongoRepository<Link, UUID> {

        @Query("{key: ?0}")
        Optional<Link> findByKey(UUID key);

        @Aggregation(pipeline = {
                        "{\n" +
                                        "        $match: {\n" +
                                        "            $expr: { $eq: ['$createdBy', ?0] }\n" +
                                        "        }\n" +
                                        "    }",

                        "{\n" +
                                        "        $lookup: {\n" +
                                        "            from: 'groups',\n" +
                                        "            localField: 'refId',\n" +
                                        "            foreignField: '_id',\n" +
                                        "            as: 'ref'\n" +
                                        "        }\n" +
                                        "    }",

                        "{\n" +
                                        "        $unwind: '$ref'\n" +
                                        "    }",

                        "{\n" +
                                        "        $group: {\n" +
                                        "            '_id': '$refId',\n" +
                                        "            'type': {$first: '$type'},\n" +
                                        "            'name': {$first: '$ref.name'},\n" +
                                        "            'data': {\n" +
                                        "                $sum: {\n" +
                                        "                    $size: '$usedBys'\n" +
                                        "                }\n" +
                                        "            },\n" +
                                        "        }\n" +
                                        "    }",

                        "{\n" +
                                        "        $replaceRoot: { \n" +
                                        "            'newRoot': { \n" +
                                        "                $mergeObjects: [{'ref': '$_id', 'type': '$type', 'cnt': '$data', 'name': '$name'}]\n"
                                        +
                                        "            }\n" +
                                        "        }\n" +
                                        "    }"
        })
        List<Reward> findRewards(
                        UUID profile);
}

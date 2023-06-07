package com.raxim.myscoutee.profile.repository.mongo

import com.raxim.myscoutee.profile.data.document.mongo.Link
import com.raxim.myscoutee.profile.data.document.mongo.Role
import com.raxim.myscoutee.profile.data.document.mongo.User
import com.raxim.myscoutee.profile.data.dto.rest.Group
import com.raxim.myscoutee.profile.data.dto.rest.Reward
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface LinkRepository : MongoRepository<Link, UUID> {
    @Query("{key: ?0}")
    fun findByKey(key: UUID): Optional<Link>

    @Aggregation(
        pipeline = [
            "{\n" +
                    "        \$match: {\n" +
                    "            \$expr: { \$eq: ['\$createdBy', ?0] }\n" +
                    "        }\n" +
                    "    }",
            
            "{\n" +
                    "        \$lookup: {\n" +
                    "            from: 'groups',\n" +
                    "            localField: 'refId',\n" +
                    "            foreignField: '_id',\n" +
                    "            as: 'ref'\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$unwind: '\$ref'\n" +
                    "    }",

            

            "{\n" +
                    "        \$group: {\n" +
                    "            '_id': '\$refId',\n" +
                    "            'type': {\$first: '\$type'},\n" +
                    "            'name': {\$first: '\$ref.name'},\n" +
                    "            'data': {\n" +
                    "                \$sum: {\n" +
                    "                    \$size: '\$usedBys'\n" +
                    "                }\n" +
                    "            },\n" +
                    "        }\n" +
                    "    }",

            "{\n" +
                    "        \$replaceRoot: { \n" +
                    "            'newRoot': { \n" +
                    "                \$mergeObjects: [{'ref': '\$_id', 'type': '\$type', 'cnt': '\$data', 'name': '\$name'}]\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }"
        ]
    )
    fun findRewards(
        profile: UUID
    ): List<Reward>
}
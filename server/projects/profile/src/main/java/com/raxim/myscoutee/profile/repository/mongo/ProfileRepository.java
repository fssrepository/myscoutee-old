package com.raxim.myscoutee.profile.repository.mongo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.mongodb.client.model.geojson.Point;
import com.raxim.myscoutee.algo.dto.Range;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.RangeLocal;
import com.raxim.myscoutee.profile.data.dto.rest.CarDTO;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;
import com.raxim.myscoutee.profile.data.dto.rest.ISODateRange;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.data.dto.rest.SchoolDTO;

@RepositoryRestResource(collectionResourceRel = "profiles", path = "profiles")
public interface ProfileRepository extends MongoRepository<Profile, UUID> {

        @Aggregation(pipeline = "findProfile")
        List<ProfileDTO> findProfile(
                        @Param("param") PageParam param,
                        @Param("loc") Point loc,
                        @Param("gender") String gender,
                        @Param("groupId") UUID groupId,
                        @Param("direction") double direction,
                        @Param("score") int score,
                        @Param("met") boolean met,
                        @Param("selectedId") UUID selectedId,
                        @Param("bRange") RangeLocal bRange);

        @Aggregation(pipeline = "findCarsByProfile")
        List<CarDTO> findCarsByProfile(
                        @Param("param") PageParam pageParam);

        @Aggregation(pipeline = "findSchoolsByProfile")
        List<SchoolDTO> findSchoolsByProfile(@Param("profileId") UUID profileId,
                        @Param("param") PageParam pageParam);

        @Aggregation(pipeline = "findGroupsByProfile")
        List<GroupDTO> findGroupsByProfile(
                        @Param("profileId") UUID profileId,
                        @Param("param") PageParam pageParam);
}

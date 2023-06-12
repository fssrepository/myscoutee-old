package com.raxim.myscoutee.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;

import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.raxim.myscoutee.common.config.RepositoryConfig;
import com.raxim.myscoutee.common.repository.MongoDataLoaderTestExecutionListener;
import com.raxim.myscoutee.common.repository.TestData;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;
import com.raxim.myscoutee.profile.repository.mongo.GroupRepository;

@DataMongoTest
@Import({ RepositoryConfig.class })
@TestPropertySource(properties = { "de.flapdoodle.mongodb.embedded.version=6.0.6",
        "logging.level.org.springframework.data.mongodb=DEBUG"})
@TestData("mongo/groups.json")
@TestExecutionListeners(value = MongoDataLoaderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class GroupRepositoryTest {

    @Autowired
    private GroupRepository groupRepository;

    @Test
    void testShouldGetSystemGroup() {
        Object[] tOffset = new Object[] { 0.0, "1900-01-01", 0.0 };
        List<GroupDTO> groups = groupRepository.findGroupByProfile("d",
                new Point(new Position(Arrays.asList(new Double[] {
                        47.497912, 19.040235
                }))), 20, 5000, new ArrayList<>(), tOffset);
        System.out.println(groups);
    }
}

package com.raxim.myscoutee.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;

import com.raxim.myscoutee.common.AppTestConstants;
import com.raxim.myscoutee.common.config.RepositoryConfig;
import com.raxim.myscoutee.common.repository.MongoDataLoaderTestExecutionListener;
import com.raxim.myscoutee.common.repository.TestData;
import com.raxim.myscoutee.profile.data.document.mongo.Group;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.repository.mongo.GroupRepository;

@DataMongoTest
@DirtiesContext
@Import({ RepositoryConfig.class })
@TestPropertySource(properties = { "de.flapdoodle.mongodb.embedded.version=6.0.6",
                "logging.level.org.springframework.data.mongodb=DEBUG" })
@TestData({ "mongo/rec/groups.json", "mongo/user/profiles.json", "mongo/user/users.json" })
@TestExecutionListeners(value = MongoDataLoaderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class GroupRepositoryTest {

        @Autowired
        private GroupRepository groupRepository;

        @Test
        void testShouldGetRecGroup() {
                List<Group> groups = groupRepository.findAll();
                assertEquals(5, groups.size());

                PageParam pageParam = new PageParam();

                double minDistance = 0d;
                LocalDate updatedDateFrom = LocalDate.now();
                String updatedDateFromF = updatedDateFrom.atStartOfDay(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                Object[] tOffset = new Object[] { minDistance, updatedDateFromF };

                pageParam.setId(AppTestConstants.UUID_PROFILE_OLIVER);
                pageParam.setOffset(tOffset);

                List<GroupDTO> recGroups = groupRepository.findRecGroups(pageParam,
                                AppTestConstants.LOCATION_PROFILE_OLIVER, AppTestConstants.UUID_GROUP_DATING);

                assertEquals(1, recGroups.size());
                assertEquals(AppTestConstants.UUID_GROUP_3, recGroups.get(0).getGroup().getId());
        }
}

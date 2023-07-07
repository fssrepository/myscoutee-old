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
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.dto.rest.MemberDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.repository.mongo.EventItemRepository;

@DataMongoTest
@DirtiesContext
@Import({ RepositoryConfig.class })
@TestPropertySource(properties = { "de.flapdoodle.mongodb.embedded.version=6.0.6",
                "logging.level.org.springframework.data.mongodb=DEBUG" })
@TestData({ "mongo/profiles.json", "mongo/list/items.json", "mongo/list/events.json" })
@TestExecutionListeners(value = MongoDataLoaderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class EventItemRepositoryMemberTest {

        @Autowired
        private EventItemRepository eventItemRepository;

        @Test
        public void shouldGetMembersForItem() {
                String[] memberStatuses = new String[] { "A", "I", "J", "W" };
                String status = "A";
                LocalDate createdDate = LocalDate.of(1901, 1, 1);
                String createdDateF = createdDate.atStartOfDay(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                String[] tOffset = new String[] { status, createdDateF };

                PageParam pageParam = new PageParam();
                pageParam.setId(AppTestConstants.UUID_PROFILE_OLIVER);
                pageParam.setOffset(tOffset);

                List<MemberDTO> memberDTOs = this.eventItemRepository.findMembersByItem(pageParam,
                                AppTestConstants.UUID_EVENT_ITEM_32,
                                memberStatuses);
                assertEquals(4, memberDTOs.size());

                assertEquals(AppTestConstants.UUID_PROFILE_AVA, memberDTOs.get(3).getMember().getProfile().getId());

                int limit = 3;
                pageParam.setLimit(limit);

                memberDTOs = this.eventItemRepository.findMembersByItem(pageParam, AppTestConstants.UUID_EVENT_ITEM_32,
                                memberStatuses);
                assertEquals(limit, memberDTOs.size());

                assertEquals(AppTestConstants.UUID_PROFILE_OLIVER, memberDTOs.get(0).getMember().getProfile().getId());
                assertEquals(AppTestConstants.UUID_PROFILE_EMMA, memberDTOs.get(1).getMember().getProfile().getId());

                Object[] lOffset = CommonUtil.offset(memberDTOs, tOffset).toArray();
                pageParam.setOffset(lOffset);

                memberDTOs = this.eventItemRepository.findMembersByItem(pageParam, AppTestConstants.UUID_EVENT_ITEM_32,
                                memberStatuses);
                assertEquals(1, memberDTOs.size());

                assertEquals(AppTestConstants.UUID_PROFILE_AVA, memberDTOs.get(0).getMember().getProfile().getId());

        }

}

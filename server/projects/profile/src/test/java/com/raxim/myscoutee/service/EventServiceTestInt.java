package com.raxim.myscoutee.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;

import com.raxim.myscoutee.algo.AbstractAlgoTest;
import com.raxim.myscoutee.common.AppTestConstants;
import com.raxim.myscoutee.common.config.JsonConfig;
import com.raxim.myscoutee.common.config.RepositoryConfig;
import com.raxim.myscoutee.common.repository.MongoDataLoaderTestExecutionListener;
import com.raxim.myscoutee.common.repository.TestData;
import com.raxim.myscoutee.profile.converter.Converters;
import com.raxim.myscoutee.profile.data.dto.rest.MemberDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.exception.MessageException;
import com.raxim.myscoutee.profile.repository.mongo.EventItemRepository;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.PromotionRepository;
import com.raxim.myscoutee.profile.service.EventService;
import com.raxim.myscoutee.profile.service.StatusService;

@DataMongoTest
@DirtiesContext
@Import({ RepositoryConfig.class, JsonConfig.class })
@TestPropertySource(properties = { "de.flapdoodle.mongodb.embedded.version=6.0.6",
                "logging.level.org.springframework.data.mongodb=DEBUG" })
@TestData({ "mongo/profiles.json", "mongo/list/items.json", "mongo/list/events.json" })
@TestExecutionListeners(value = MongoDataLoaderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class EventServiceTestInt extends AbstractAlgoTest {

        @Autowired
        private ProfileRepository profileRepository;

        @Autowired
        private EventRepository eventRepository;

        @Autowired
        private EventItemRepository eventItemRepository;

        @Autowired
        //@Spy is not woring for repositories
        private PromotionRepository promotionRepository;

        private Converters converters = new Converters();

        private EventService eventService;
        private StatusService statusService;

        @BeforeEach
        public void init() {
                eventService = new EventService(eventRepository, eventItemRepository, promotionRepository,
                                profileRepository, converters);
                statusService = new StatusService(profileRepository, eventRepository, eventItemRepository,
                                promotionRepository);
        }

        @Test
        public void shouldEventAdminInviteAndAcceptFirst() throws MessageException {
                String[] memberStatuses = new String[] { "A", "I", "J", "W" };
                String status = "A";
                LocalDate createdDate = LocalDate.of(1901, 1, 1);
                String createdDateF = createdDate.atStartOfDay(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                String[] tOffset = new String[] { status, createdDateF };

                PageParam pageParam = new PageParam();
                pageParam.setId(AppTestConstants.UUID_PROFILE_OLIVER);
                pageParam.setOffset(tOffset);

                List<MemberDTO> memberDTOs = this.eventRepository.findMembersByEvent(pageParam,
                                AppTestConstants.UUID_EVENT_32,
                                memberStatuses);
                assertEquals(4, memberDTOs.size());

                List<UUID> invitedUuids = List.of(AppTestConstants.UUID_PROFILE_EMMA,
                                AppTestConstants.UUID_PROFILE_ETHAN, AppTestConstants.UUID_PROFILE_LIAM);
                List<String> invitedIds = invitedUuids.stream().map(
                                uuid -> uuid.toString())
                                .toList();

                eventService.inviteMembersForEvent(AppTestConstants.UUID_EVENT_32.toString(), invitedIds,
                                AppTestConstants.UUID_PROFILE_OLIVER);

                memberDTOs = this.eventRepository.findMembersByEvent(pageParam,
                                AppTestConstants.UUID_EVENT_32,
                                memberStatuses);
                assertEquals(6, memberDTOs.size());

                assertEquals(AppTestConstants.UUID_PROFILE_EMMA, memberDTOs.get(1).getMember().getProfile().getId());
                assertEquals("A", memberDTOs.get(1).getMember().getStatus());

                assertEquals(AppTestConstants.UUID_PROFILE_ETHAN, memberDTOs.get(4).getMember().getProfile().getId());
                assertEquals("I", memberDTOs.get(4).getMember().getStatus());

                assertEquals(AppTestConstants.UUID_PROFILE_LIAM, memberDTOs.get(5).getMember().getProfile().getId());
                assertEquals("I", memberDTOs.get(5).getMember().getStatus());

                statusService.changeStatusForEvent(AppTestConstants.UUID_EVENT_32.toString(),
                                AppTestConstants.UUID_PROFILE_LIAM, "A");
                // late accept
                statusService.changeStatusForEvent(AppTestConstants.UUID_EVENT_32.toString(),
                                AppTestConstants.UUID_PROFILE_ETHAN, "A");

                memberDTOs = this.eventRepository.findMembersByEvent(pageParam, AppTestConstants.UUID_EVENT_32,
                                memberStatuses);
                assertEquals(6, memberDTOs.size());

                assertEquals(AppTestConstants.UUID_PROFILE_LIAM, memberDTOs.get(4).getMember().getProfile().getId());
                assertEquals("A", memberDTOs.get(4).getMember().getStatus());

                assertEquals(AppTestConstants.UUID_PROFILE_ETHAN, memberDTOs.get(5).getMember().getProfile().getId());
                assertEquals("W", memberDTOs.get(5).getMember().getStatus());

        }
}

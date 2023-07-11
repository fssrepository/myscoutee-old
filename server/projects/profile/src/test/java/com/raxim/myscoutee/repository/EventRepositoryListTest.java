package com.raxim.myscoutee.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.handler.EventItemParamHandler;
import com.raxim.myscoutee.profile.handler.EventParamHandler;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;

@DataMongoTest
@DirtiesContext
@Import({ RepositoryConfig.class })
@TestPropertySource(properties = { "de.flapdoodle.mongodb.embedded.version=6.0.6",
                "logging.level.org.springframework.data.mongodb=DEBUG" })
// @TestData({"profiles" = mongo/profiles.json})
@TestData({ "mongo/profiles.json", "mongo/multislot/events.json" })
@TestExecutionListeners(value = MongoDataLoaderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class EventRepositoryListTest {

        @Autowired
        private EventRepository eventRepository;

        @Test
        public void shouldFindEventByMonth() {
                LocalDate from = LocalDate.of(2020, 1, 1);
                LocalDate createdDate = LocalDate.of(1901, 1, 1);
                LocalDate until = from.plusMonths(1);

                String fromF = from.atStartOfDay(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                String untilF = until.atStartOfDay(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                String createdDateF = createdDate.atStartOfDay(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                String[] tOffset = new String[] { fromF, createdDateF, untilF };

                PageParam pageParam = new PageParam(tOffset, EventParamHandler.MONTH);
                pageParam.setId(AppTestConstants.UUID_PROFILE_OLIVER);

                List<EventDTO> eventDTOs = this.eventRepository.findEventByMonth(pageParam,
                                new String[] { "A", "P", "C" });
                assertEquals(1, eventDTOs.size());

                LocalDateTime untilM = until.atStartOfDay().minus(1, ChronoUnit.MILLIS);
                assertEquals(untilM, eventDTOs.get(0).getItem().getRange().getEnd());
        }

        @Test
        public void shouldFindEventsByDayWithPagination() {
                LocalDate from = LocalDate.of(2020, 1, 1);
                LocalDate createdDate = LocalDate.of(1901, 1, 1);

                String fromF = from.atStartOfDay(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                String createdDateF = createdDate.atStartOfDay(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                String[] tOffset = new String[] { fromF, createdDateF };

                PageParam pageParam = new PageParam(tOffset, EventParamHandler.MONTH);
                pageParam.setId(AppTestConstants.UUID_PROFILE_OLIVER);
                pageParam.setGroupKey(EventParamHandler.DAY_FORMAT);

                List<EventDTO> eventDTOs = this.eventRepository.findEventDown(pageParam,
                                new String[] { "A", "P", "C" });
                assertEquals(2, eventDTOs.size());

                // pagination with one item back and forward
                pageParam.setLimit(1);

                eventDTOs = this.eventRepository.findEventDown(pageParam,
                                new String[] { "A", "P", "C" });
                assertEquals(1, eventDTOs.size());
                assertEquals("Event32", eventDTOs.get(0).getItem().getName());

                String[] tOffsetM1 = eventDTOs.get(0).getOffset().stream()
                                .map(Object::toString)
                                .toArray(String[]::new);

                pageParam.setOffset(tOffsetM1);

                eventDTOs = this.eventRepository.findEventDown(pageParam,
                                new String[] { "A", "P", "C" });
                assertEquals(1, eventDTOs.size());
                assertEquals("Event2", eventDTOs.get(0).getItem().getName());

                eventDTOs = this.eventRepository.findEventUp(pageParam,
                                new String[] { "A", "P", "C" });
                assertEquals(1, eventDTOs.size());
                assertEquals(1, eventDTOs.size());
                assertEquals("Event31", eventDTOs.get(0).getItem().getName());
        }

        @Test
        public void shouldFindEventsByWeek() {
                LocalDate from = LocalDate.of(2020, 1, 1);
                LocalDate createdDate = LocalDate.of(1901, 1, 1);

                String fromF = from.atStartOfDay(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                String createdDateF = createdDate.atStartOfDay(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                String[] tOffset = new String[] { fromF, createdDateF };

                PageParam pageParam = new PageParam(tOffset, EventParamHandler.MONTH);
                pageParam.setId(AppTestConstants.UUID_PROFILE_OLIVER);
                pageParam.setGroupKey(EventParamHandler.WEEK_FORMAT);

                List<EventDTO> eventDTOs = this.eventRepository.findEventDown(pageParam,
                                new String[] { "A", "P", "C" });
                assertEquals(2, eventDTOs.size());
                assertEquals("2020 04", eventDTOs.get(0).getGroupKey());
                assertEquals("2021 04", eventDTOs.get(1).getGroupKey());

        }

        @Test
        public void shouldFindEventItem() {
                LocalDate from = LocalDate.of(2020, 1, 1);
                LocalDate createdDate = LocalDate.of(1901, 1, 1);

                String fromF = from.atStartOfDay(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                String createdDateF = createdDate.atStartOfDay(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                Integer stage = 0;

                Object[] tOffset = new Object[] { fromF, createdDateF, stage };

                PageParam pageParam = new PageParam(tOffset, EventParamHandler.MONTH);
                pageParam.setId(AppTestConstants.UUID_PROFILE_AVA);
                pageParam.setGroupKey(EventItemParamHandler.DAY_FORMAT);
                pageParam.setLimit(2);

                List<EventDTO> eventItemDTOs = this.eventRepository.findItemsByEvent(AppTestConstants.UUID_EVENT_32,
                                pageParam);
                assertEquals(2, eventItemDTOs.size());
                // the createdDate for the second EventItem32 is +1 millisec (while adding more
                // than one clone can happen)

                String nameWithSlot = eventItemDTOs.get(0).getItem().getSlotCnt() + ". " + "EventItem32";
                assertEquals(nameWithSlot, eventItemDTOs.get(0).getItem().getName());
                assertEquals("U", eventItemDTOs.get(0).getRole());
                assertEquals("1. 2020-01-31 - 2020-10-17", eventItemDTOs.get(0).getGroupKey());

                nameWithSlot = eventItemDTOs.get(1).getItem().getSlotCnt() + ". " + "EventItem32";
                assertEquals(nameWithSlot, eventItemDTOs.get(1).getItem().getName());
                assertEquals("P", eventItemDTOs.get(1).getRole());
                assertEquals("1. 2020-01-31 - 2020-10-17", eventItemDTOs.get(1).getGroupKey());

                Object[] lOffset = CommonUtil.offset(eventItemDTOs, tOffset).toArray();
                pageParam.setOffset(lOffset);

                eventItemDTOs = this.eventRepository.findItemsByEvent(AppTestConstants.UUID_EVENT_32,
                                pageParam);
                assertEquals(1, eventItemDTOs.size());
                assertEquals("EventItem33", eventItemDTOs.get(0).getItem().getName());
                assertEquals("A", eventItemDTOs.get(0).getRole());
                assertEquals("2. 2020-01-31 - 2020-10-17", eventItemDTOs.get(0).getGroupKey());

        }

}

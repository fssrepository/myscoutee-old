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
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.util.AppConstants;

@DataMongoTest
@DirtiesContext
@Import({ RepositoryConfig.class })
@TestPropertySource(properties = { "de.flapdoodle.mongodb.embedded.version=6.0.6",
                "logging.level.org.springframework.data.mongodb=DEBUG" })
@TestData({ "mongo/profiles.json", "mongo/rec/events.json" })
@TestExecutionListeners(value = MongoDataLoaderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class EventRepositoryRecTest {

        @Autowired
        private EventRepository eventRepository;

        @Test
        public void shouldFindRecommendations() {
                double minDistance = 0d;
                LocalDate from = AppConstants.DATE_MIN;
                LocalDate updatedDateFrom = LocalDate.now();

                String updatedDateFromF = updatedDateFrom.atStartOfDay(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                String fromF = from.atStartOfDay(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                Object[] tOffset = new Object[] { minDistance, fromF, updatedDateFromF };

                PageParam pageParam = new PageParam();
                pageParam.setId(AppTestConstants.UUID_PROFILE_OLIVER);
                pageParam.setOffset(tOffset);

                List<EventDTO> events = this.eventRepository.findRecommendation(pageParam,
                                AppTestConstants.LOCATION_PROFILE_OLIVER,
                                AppTestConstants.UUID_GROUP_DATING, "A");
                assertEquals(2, events.size());
                assertEquals("Event1", events.get(0).getItem().getName());
                assertEquals("Event2", events.get(1).getItem().getName());

                events = this.eventRepository.findRecommendation(pageParam,
                                AppTestConstants.LOCATION_PROFILE_OLIVER,
                                AppTestConstants.UUID_GROUP_DATING, "F");
                assertEquals(1, events.size());
                assertEquals("Event1", events.get(0).getItem().getName());

        }
}

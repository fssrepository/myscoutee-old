package com.raxim.myscoutee.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;

@DataMongoTest
@DirtiesContext
@Import({ RepositoryConfig.class })
@TestPropertySource(properties = { "de.flapdoodle.mongodb.embedded.version=6.0.6",
                "logging.level.org.springframework.data.mongodb=DEBUG" })
@TestData({ "mongo/profiles.json", "mongo/clone/events.json" })
@TestExecutionListeners(value = MongoDataLoaderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class EventRepositoryCloneTest {

        @Autowired
        private EventRepository eventRepository;

        @Test
        public void shouldFindParents() {

                List<Event> events = this.eventRepository.findParents(AppTestConstants.UUID_EVENT_111, 0);
                assertEquals(2, events.size());
                assertEquals("Event111", events.get(0).getName());
                assertEquals("Event11", events.get(1).getName());

                events = this.eventRepository.findParents(AppTestConstants.UUID_EVENT_111, 1);
                assertEquals(3, events.size());
                assertEquals("Event111", events.get(0).getName());
                assertEquals("Event11", events.get(1).getName());
                assertEquals("Event1", events.get(2).getName());
        }
}

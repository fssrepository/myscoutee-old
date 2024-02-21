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

import com.raxim.myscoutee.common.config.RepositoryConfig;
import com.raxim.myscoutee.common.repository.MongoDataLoaderTestExecutionListener;
import com.raxim.myscoutee.common.repository.TestData;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.EventWithCandidates;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;

@DataMongoTest
@DirtiesContext
@Import({ RepositoryConfig.class })
@TestPropertySource(properties = { "de.flapdoodle.mongodb.embedded.version=6.0.6",
        "logging.level.org.springframework.data.mongodb=DEBUG" })
// @TestData({"profiles" = mongo/profiles.json})
@TestData({ "mongo/profiles.json", "mongo/priority/events.json" })
@TestExecutionListeners(value = MongoDataLoaderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class EventRepositoryAlgoTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    public void shouldFindCandidates() {
        List<Event> events = eventRepository.findAll();
        assertEquals(5, events.size());

        List<EventWithCandidates> eventsWithCandidates = eventRepository.findEventsWithCandidates();
        assertEquals(1, eventsWithCandidates.size());

        EventWithCandidates eventWithCandidates = eventsWithCandidates.get(0);
        assertEquals(2, eventWithCandidates.getEvent().getItems().size());
        assertEquals("P", eventWithCandidates.getEvent().getStatus());
        assertEquals(2, eventWithCandidates.getCandidates().size());

        // mutual
        Event event = eventWithCandidates.getEvent();
        event.getRule().setMutual(true);
        eventRepository.save(event);

        eventsWithCandidates = eventRepository.findEventsWithCandidates();

        eventWithCandidates = eventsWithCandidates.get(0);
        assertEquals("P", eventWithCandidates.getEvent().getStatus());
        assertEquals(5, eventWithCandidates.getCandidates().size());
    }

}

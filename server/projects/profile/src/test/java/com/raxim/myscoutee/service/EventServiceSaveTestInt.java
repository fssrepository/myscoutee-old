package com.raxim.myscoutee.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

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
import com.raxim.myscoutee.profile.converter.Convertable;
import com.raxim.myscoutee.profile.converter.Converters;
import com.raxim.myscoutee.profile.converter.EventConverter;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.exception.MessageException;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.ScoreMatrixRepository;
import com.raxim.myscoutee.profile.service.EventService;

@DataMongoTest
@DirtiesContext
@Import({ RepositoryConfig.class, JsonConfig.class })
@TestPropertySource(properties = { "de.flapdoodle.mongodb.embedded.version=6.0.6",
                "logging.level.org.springframework.data.mongodb=DEBUG" })
@TestData({ "mongo/profiles.json", "mongo/clone/events.json" })
@TestExecutionListeners(value = MongoDataLoaderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class EventServiceSaveTestInt extends AbstractAlgoTest {

        @Autowired
        private ProfileRepository profileRepository;

        @Autowired
        private EventRepository eventRepository;

        @Autowired
        private ScoreMatrixRepository scoreMatrixRepository;

        private Converters<? extends Convertable<?>, ? extends Convertable<?>> converters;

        private EventService eventService;

        @BeforeEach
        public void init() {
                converters = new Converters<>(List.of(new EventConverter()));
                eventService = new EventService(eventRepository,
                                profileRepository, converters, scoreMatrixRepository);
        }

        @Test
        public void shouldSaveNewEvent() throws MessageException {
                Profile profileSophia = this.profileRepository.findById(AppTestConstants.UUID_PROFILE_SOPHIA).get();

                Event event = this.eventRepository.findById(AppTestConstants.UUID_EVENT_111).get();
                event.setId(null);
                event.setName(AppTestConstants.UUID_EVENT_111_FAKE_NAME);

                EventDTO eventDTO = this.eventService
                                .save(profileSophia, event, AppTestConstants.UUID_EVENT_11.toString()).get();

                List<Event> eventItems = eventDTO.getItem().getItems();

                assertEquals(2, eventItems.size());

                Event childEvent1 = eventItems.get(1);
                assertEquals(AppTestConstants.UUID_EVENT_11.toString(),
                                childEvent1.getParentId().toString());
                assertEquals(AppTestConstants.UUID_EVENT_111_FAKE_NAME, childEvent1.getName());

                Event childEvent2 = eventItems.get(0);
                assertEquals(AppTestConstants.UUID_EVENT_111.toString(),
                                childEvent2.getId().toString());
        }

        @Test
        public void shouldSaveExistingEvent() throws MessageException {
                Profile profileSophia = this.profileRepository.findById(AppTestConstants.UUID_PROFILE_SOPHIA).get();

                Event event = this.eventRepository.findById(AppTestConstants.UUID_EVENT_111).get();

                EventDTO eventDTO = this.eventService
                                .save(profileSophia, event, AppTestConstants.UUID_EVENT_1.toString()).get();

                List<Event> eventItems = eventDTO.getItem().getItems();

                assertEquals(2, eventItems.size());

                Event childEvent1 = eventItems.get(1);
                assertEquals(AppTestConstants.UUID_EVENT_1.toString(),
                                childEvent1.getParentId().toString());
                assertEquals(AppTestConstants.UUID_EVENT_111.toString(),
                                childEvent1.getId().toString());

                Event childEvent2 = eventItems.get(0);
                assertEquals(AppTestConstants.UUID_EVENT_11.toString(),
                                childEvent2.getId().toString());
        }
}

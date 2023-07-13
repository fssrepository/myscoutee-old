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
import com.raxim.myscoutee.profile.data.dto.rest.CloneDTO;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.service.EventService;

@DataMongoTest
@DirtiesContext
@Import({ RepositoryConfig.class, JsonConfig.class })
@TestPropertySource(properties = { "de.flapdoodle.mongodb.embedded.version=6.0.6",
                "logging.level.org.springframework.data.mongodb=DEBUG" })
@TestData({ "mongo/profiles.json", "mongo/clone/events.json" })
@TestExecutionListeners(value = MongoDataLoaderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class EventServiceCloneTestInt extends AbstractAlgoTest {

        @Autowired
        private ProfileRepository profileRepository;

        @Autowired
        private EventRepository eventRepository;

        private Converters<? extends Convertable<?>, ? extends Convertable<?>> converters;

        private EventService eventService;

        @BeforeEach
        public void init() {
                converters = new Converters<>(List.of(new EventConverter()));
                eventService = new EventService(eventRepository,
                                profileRepository, converters);
        }

        @Test
        public void shouldCloneEvent() throws CloneNotSupportedException {
                CloneDTO cloneDTO = new CloneDTO();
                cloneDTO.setNumberOfCopies(3);

                Profile profile = profileRepository.findById(AppTestConstants.UUID_PROFILE_SOPHIA).get();
                Event event = eventRepository.findById(AppTestConstants.UUID_EVENT_1).get();

                this.eventService.cloneBy(event.getId().toString(), profile, cloneDTO);

                List<Event> events = this.eventRepository.findAll();
                assertEquals(12, events.size());

        }
}

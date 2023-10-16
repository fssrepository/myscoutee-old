package com.raxim.myscoutee.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;

import com.raxim.myscoutee.algo.AbstractAlgoTest;
import com.raxim.myscoutee.common.config.JsonConfig;
import com.raxim.myscoutee.common.config.RepositoryConfig;
import com.raxim.myscoutee.common.repository.MongoDataLoaderTestExecutionListener;
import com.raxim.myscoutee.common.repository.TestData;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.EventWithCandidates;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.SequenceRepository;
import com.raxim.myscoutee.profile.service.EventGeneratorByPriorityService;
import com.raxim.myscoutee.profile.service.LikeService;

@DataMongoTest
@DirtiesContext
@Import({ RepositoryConfig.class, JsonConfig.class })
@TestPropertySource(properties = { "de.flapdoodle.mongodb.embedded.version=6.0.6",
                "logging.level.org.springframework.data.mongodb=DEBUG" })
@TestData({ "mongo/profiles.json", "mongo/priority/events.json", "mongo/likes.json" })
@TestExecutionListeners(value = MongoDataLoaderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class EventGeneratorPriorityServiceTestInt extends AbstractAlgoTest {

        private static final UUID UUID_PROFILE_OLIVER = UUID.fromString("534ccc6b-2547-4bf0-ad91-dca739943ea4");
        private static final UUID UUID_PROFILE_SOPHIA = UUID.fromString("39402632-a452-57be-2518-53cc117b1abc");

        @Autowired
        private ProfileRepository profileRepository;

        @Autowired
        private LikeRepository likeRepository;

        @Autowired
        private SequenceRepository sequenceRepository;

        @Autowired
        private EventRepository eventRepository;

        @Test
        public void shouldGeneratePriorityEvent() {
                LikeService likeService = new LikeService(profileRepository, likeRepository, eventRepository,
                                sequenceRepository);
                EventGeneratorByPriorityService eventGeneratorPriorityService = new EventGeneratorByPriorityService(
                                likeService,
                                eventRepository);

                List<EventWithCandidates> eventWithCandidates = this.eventRepository.findEventsWithCandidates();
                assertTrue(eventWithCandidates.size() > 0);
                assertEquals("T", eventWithCandidates.get(0).getEvent().getStatus());
                assertEquals(1, eventWithCandidates.get(2).getEvent().getMembers().size());

                Event event = this.eventRepository.findById(eventWithCandidates.get(0).getEvent().getId()).get();
                assertEquals("P", event.getStatus());

                eventGeneratorPriorityService.generate();

                event = this.eventRepository.findById(eventWithCandidates.get(0).getEvent().getId()).get();
                assertEquals("T", event.getStatus());

                event = this.eventRepository.findById(eventWithCandidates.get(2).getEvent().getId()).get();
                assertEquals("P", event.getStatus());

                assertEquals(2, event.getMembers().size());

                Set<Member> members = Set.of(new Member(new Profile(UUID_PROFILE_OLIVER), "I", "U"),
                                new Member(new Profile(UUID_PROFILE_SOPHIA), "A", "A"));
                boolean allProfilesMatched = matchAll(event.getMembers(), members);
                assertTrue(allProfilesMatched);
        }
}

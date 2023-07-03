package com.raxim.myscoutee.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.algo.AbstractAlgoTest;
import com.raxim.myscoutee.common.config.JsonConfig;
import com.raxim.myscoutee.data.mongo.TestEvent;
import com.raxim.myscoutee.data.mongo.TestProfile;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.repository.mongo.EventItemRepository;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.service.EventServiceForGenerator;

@DirtiesContext
@ExtendWith({ SpringExtension.class })
@ContextConfiguration(classes = JsonConfig.class)
public class EventServiceForGeneratorTest extends AbstractAlgoTest {

        private static final UUID UUID_PROFILE_SOPHIA = UUID.fromString("39402632-a452-57be-2518-53cc117b1abc");
        private static final UUID UUID_PROFILE_OLIVER = UUID.fromString("534ccc6b-2547-4bf0-ad91-dca739943ea4");

        private static final UUID UUID_PROFILE_ETHAN = UUID.fromString("2f7e01ce-1336-37d4-e69e-efc88d2ee81a");
        private static final UUID UUID_PROFILE_EMMA = UUID.fromString("fc4c72f8-a905-e1d1-05f8-c0d9914e158c");

        @InjectMocks
        private EventServiceForGenerator eventServiceForGenerator;

        @Mock
        private EventRepository eventRepository;

        @Mock
        private EventItemRepository eventItemRepository;

        @Autowired
        @Spy
        private ObjectMapper objectMapper;

        @Captor
        private ArgumentCaptor<List<Event>> captorEvents;

        @Test
        public void shouldSaveEvents() throws IOException {
                // json property override
                objectMapper.addMixIn(Event.class, TestEvent.class);

                Profile[] profileArray = loadJson(this, "rest/profiles.json",
                                TestProfile[].class,
                                objectMapper);

                List<Profile> profiles = Arrays.asList(profileArray);

                // build parameters
                List<Set<Profile>> profileByGroups = new ArrayList<>();
                profileByGroups.add(Set.of(profiles.get(0), profiles.get(1))); // Sophia - Oliver
                profileByGroups.add(Set.of(profiles.get(2), profiles.get(4))); // Ethan - Emma

                eventServiceForGenerator.saveEvents(profileByGroups);

                Mockito.verify(eventRepository).saveAll(captorEvents.capture());

                List<Event> capturedEvents = (List<Event>) captorEvents.getValue();
                assertEquals(2, capturedEvents.size());

                // event1
                Event event1 = capturedEvents.get(0);
                assertEquals(2, event1.getMembers().size());

                List<UUID> memberUuids1 = List.of(UUID_PROFILE_SOPHIA, UUID_PROFILE_OLIVER);
                assertTrue(memberUuids1.stream().allMatch(
                                id -> event1.getMembers().stream().anyMatch(
                                                member -> member.getProfile().getId().equals(id))));

                // event2
                Event event2 = capturedEvents.get(1);
                assertEquals(2, event2.getMembers().size());

                List<UUID> memberUuids2 = List.of(UUID_PROFILE_ETHAN, UUID_PROFILE_EMMA);
                assertTrue(memberUuids2.stream().allMatch(
                                id -> event2.getMembers().stream().anyMatch(
                                                member -> member.getProfile().getId().equals(id))));

        }
}

package com.raxim.myscoutee.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.common.config.JsonConfig;
import com.raxim.myscoutee.data.mongo.TestEventItem;
import com.raxim.myscoutee.data.mongo.TestProfile;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.EventItem;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.repository.mongo.EventItemRepository;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.service.EventServiceForGenerator;
import com.raxim.myscoutee.util.TestJsonUtil;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(classes = JsonConfig.class)
public class EventServiceForGeneratorTest {

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
        private ArgumentCaptor<List<EventItem>> captorEventItems;

        @Captor
        private ArgumentCaptor<List<Event>> captorEvents;

        @Test
        public void shouldSaveEvents() throws IOException {
                // json property override
                objectMapper.addMixIn(EventItem.class, TestEventItem.class);

                Profile[] profileArray = TestJsonUtil.loadJson(this, "rest/profiles.json",
                                TestProfile[].class,
                                objectMapper);
                EventItem[] eventItemArray = TestJsonUtil.loadJson(this, "rest/eventItems.json",
                                EventItem[].class,
                                objectMapper);

                List<Profile> profiles = Arrays.asList(profileArray);
                List<EventItem> eventItems = Arrays.asList(eventItemArray);

                when(eventItemRepository.saveAll(anyList())).thenReturn(eventItems);

                // build parameters
                List<Set<Profile>> profileByGroups = new ArrayList<>();
                profileByGroups.add(Set.of(profiles.get(0), profiles.get(1))); // Sophia - Oliver
                profileByGroups.add(Set.of(profiles.get(2), profiles.get(4))); // Ethan - Emma

                eventServiceForGenerator.saveEvents(profileByGroups);

                Mockito.verify(eventItemRepository).saveAll(captorEventItems.capture());

                List<EventItem> capturedEventItems = captorEventItems.getValue();
                assertEquals(2, capturedEventItems.size());

                Mockito.verify(eventRepository).saveAll(captorEvents.capture());

                List<Event> capturedEvents = (List<Event>) captorEvents.getValue();
                assertEquals(2, capturedEvents.size());

                // event1
                Event event1 = capturedEvents.get(0);
                assertEquals(1, event1.getItems().size());

                EventItem eventInfo1 = event1.getInfo();
                assertEquals(2, eventInfo1.getMembers().size());

                List<UUID> memberUuids1 = List.of(UUID_PROFILE_SOPHIA, UUID_PROFILE_OLIVER);
                assertTrue(memberUuids1.stream().allMatch(
                                id -> eventInfo1.getMembers().stream().anyMatch(
                                                member -> member.getProfile().getId().equals(id))));

                // event2
                Event event2 = capturedEvents.get(1);
                assertEquals(1, event2.getItems().size());

                EventItem eventInfo2 = event2.getInfo();
                assertEquals(2, eventInfo2.getMembers().size());

                List<UUID> memberUuids2 = List.of(UUID_PROFILE_ETHAN, UUID_PROFILE_EMMA);
                assertTrue(memberUuids2.stream().allMatch(
                                id -> eventInfo2.getMembers().stream().anyMatch(
                                                member -> member.getProfile().getId().equals(id))));

        }
}

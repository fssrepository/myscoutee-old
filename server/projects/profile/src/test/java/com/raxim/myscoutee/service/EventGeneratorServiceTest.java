package com.raxim.myscoutee.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.algo.AbstractAlgoTest;
import com.raxim.myscoutee.algo.dto.Range;
import com.raxim.myscoutee.common.config.JsonConfig;
import com.raxim.myscoutee.data.mongo.TestEvent;
import com.raxim.myscoutee.data.mongo.TestLike;
import com.raxim.myscoutee.data.mongo.TestProfile;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Like;
import com.raxim.myscoutee.profile.data.document.mongo.LikeGroup;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Schedule;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.repository.mongo.ScheduleRepository;
import com.raxim.myscoutee.profile.service.EventGeneratorService;

@DirtiesContext
@ExtendWith({ SpringExtension.class })
@ContextConfiguration(classes = JsonConfig.class)
public class EventGeneratorServiceTest extends AbstractAlgoTest {

        private static final Range FLAGS_DEFAULT = new Range(2, 3);

        private static final UUID UUID_PROFILE_LIAM = UUID.fromString("8f4bd3a8-1195-01b0-172a-a04f052f5982");
        private static final UUID UUID_PROFILE_EMMA = UUID.fromString("fc4c72f8-a905-e1d1-05f8-c0d9914e158c");

        private static final UUID UUID_PROFILE_OLIVER = UUID.fromString("534ccc6b-2547-4bf0-ad91-dca739943ea4");
        private static final UUID UUID_PROFILE_AVA = UUID.fromString("eb494fba-6c56-752f-5123-c414c8c86599");

        private static final UUID UUID_PROFILE_ETHAN = UUID.fromString("2f7e01ce-1336-37d4-e69e-efc88d2ee81a");
        private static final UUID UUID_PROFILE_SOPHIA = UUID.fromString("39402632-a452-57be-2518-53cc117b1abc");

        private static final UUID UUID_PROFILE_LUCAS = UUID.fromString("694ef9b8-e486-bdf2-6510-7d05f19db486");

        @InjectMocks
        private EventGeneratorService eventGeneratorService;

        @Mock
        private ScheduleRepository scheduleRepository;

        @Mock
        private LikeRepository likeRepository;

        @Mock
        private EventRepository eventRepository;

        @Autowired
        @Spy
        private ObjectMapper objectMapper;

        @Test
        public void testShouldGetBalancedGroup() throws IOException {
                // json property override
                objectMapper.addMixIn(Profile.class, TestProfile.class);
                objectMapper.addMixIn(Like.class, TestLike.class);

                Like[] likeArray = loadJson(this, "algo/likes.json",
                                Like[].class, objectMapper);

                List<LikeGroup> likesBoth = Arrays.asList(likeArray)
                                .stream().collect(Collectors.groupingBy(Like::getCnt))
                                .entrySet().stream()
                                .map(entry -> new LikeGroup(entry.getKey(), entry.getValue()))
                                .collect(Collectors.toList());

                String flags = jsonToString(FLAGS_DEFAULT,
                                objectMapper);
                Optional<Schedule> scheduleResp = Optional.of(
                                new Schedule(0L, 1000L,
                                                flags));
                when(scheduleRepository.findByKey(EventGeneratorService.SCHEDULE_RANDOM_GROUP))
                                .thenReturn(scheduleResp);

                when(likeRepository.findLikeGroups())
                                .thenReturn(likesBoth);
                /*
                 * when(likeRepository.findLikeGroupsByBatch(0L, 1000L))
                 * .thenReturn(likesBoth);
                 */

                List<Set<Profile>> profilesByGroup = eventGeneratorService.generate();
                assertEquals(3, profilesByGroup.size());

                // group1
                Set<Profile> group1 = profilesByGroup.get(0);
                assertEquals(2, group1.size());

                List<UUID> expectedUuidsForGroup1 = List.of(UUID_PROFILE_LUCAS, UUID_PROFILE_SOPHIA);
                boolean allProfilesMatched = matchAll(group1, expectedUuidsForGroup1);
                assertTrue(allProfilesMatched);

                Set<Profile> group2 = profilesByGroup.get(1);
                assertEquals(2, group2.size());

                List<UUID> expectedUuidsForGroup2 = List.of(UUID_PROFILE_AVA, UUID_PROFILE_OLIVER);
                allProfilesMatched = matchAll(group2, expectedUuidsForGroup2);
                assertTrue(allProfilesMatched);

                // group3
                Set<Profile> group3 = profilesByGroup.get(2);
                assertEquals(2, group3.size());

                List<UUID> expectedUuidsForGroup3 = List.of(UUID_PROFILE_LIAM, UUID_PROFILE_EMMA);
                allProfilesMatched = matchAll(group3, expectedUuidsForGroup3);
                assertTrue(allProfilesMatched);

                // ignored
                objectMapper.addMixIn(Event.class, TestEvent.class);

                Event[] eventArray = loadJson(this, "rest/events.json",
                                Event[].class,
                                objectMapper);
                List<Event> events = Arrays.asList(eventArray);

                when(eventRepository.findAll()).thenReturn(events);

                profilesByGroup = eventGeneratorService.generate();
                assertEquals(2, profilesByGroup.size());

                // group1
                Set<Profile> group1F = profilesByGroup.get(0);
                assertEquals(2, group1F.size());

                List<UUID> expectedUuidsForGroup1F = List.of(UUID_PROFILE_LUCAS, UUID_PROFILE_SOPHIA);
                allProfilesMatched = matchAll(group1F, expectedUuidsForGroup1F);
                assertTrue(allProfilesMatched);

                Set<Profile> group2F = profilesByGroup.get(1);
                assertEquals(2, group2F.size());

                List<UUID> expectedUuidsForGroup2F = List.of(UUID_PROFILE_LIAM, UUID_PROFILE_EMMA);
                allProfilesMatched = matchAll(group2F, expectedUuidsForGroup2F);
                assertTrue(allProfilesMatched);

        }
}

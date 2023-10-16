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
import com.raxim.myscoutee.common.AppTestConstants;
import com.raxim.myscoutee.common.config.JsonConfig;
import com.raxim.myscoutee.common.data.TestEvent;
import com.raxim.myscoutee.common.data.TestLike;
import com.raxim.myscoutee.common.data.TestProfile;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Like;
import com.raxim.myscoutee.profile.data.document.mongo.LikeGroup;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Schedule;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.repository.mongo.ScheduleRepository;
import com.raxim.myscoutee.profile.service.EventGeneratorRandomService;

@DirtiesContext
@ExtendWith({ SpringExtension.class })
@ContextConfiguration(classes = JsonConfig.class)
public class EventGeneratorServiceTest extends AbstractAlgoTest {

        private static final Range FLAGS_DEFAULT = new Range(2, 3);

        @InjectMocks
        private EventGeneratorRandomService eventGeneratorService;

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
                when(scheduleRepository.findByKey(EventGeneratorRandomService.SCHEDULE_RANDOM_GROUP))
                                .thenReturn(scheduleResp);

                when(likeRepository.findLikeGroups())
                                .thenReturn(likesBoth);
                /*
                 * when(likeRepository.findLikeGroupsByBatch(0L, 1000L))
                 * .thenReturn(likesBoth);
                 */

                List<Event> generatedEvents = eventGeneratorService.generate();
                assertEquals(3, generatedEvents.size());

                // group1
                Set<Member> group1 = generatedEvents.get(0).getMembers();
                assertEquals(2, group1.size());

                List<UUID> expectedUuidsForGroup1 = List.of(AppTestConstants.UUID_PROFILE_LUCAS,
                                AppTestConstants.UUID_PROFILE_SOPHIA);
                boolean allProfilesMatched = matchAll(group1, expectedUuidsForGroup1);
                assertTrue(allProfilesMatched);

                Set<Member> group2 = generatedEvents.get(1).getMembers();
                assertEquals(2, group2.size());

                List<UUID> expectedUuidsForGroup2 = List.of(AppTestConstants.UUID_PROFILE_AVA,
                                AppTestConstants.UUID_PROFILE_OLIVER);
                allProfilesMatched = matchAll(group2, expectedUuidsForGroup2);
                assertTrue(allProfilesMatched);

                // group3
                Set<Member> group3 = generatedEvents.get(2).getMembers();
                assertEquals(2, group3.size());

                List<UUID> expectedUuidsForGroup3 = List.of(AppTestConstants.UUID_PROFILE_LIAM,
                                AppTestConstants.UUID_PROFILE_EMMA);
                allProfilesMatched = matchAll(group3, expectedUuidsForGroup3);
                assertTrue(allProfilesMatched);

                // ignored
                objectMapper.addMixIn(Event.class, TestEvent.class);

                Event[] eventArray = loadJson(this, "rest/events.json",
                                Event[].class,
                                objectMapper);
                List<Event> events = Arrays.asList(eventArray);

                when(eventRepository.findAll()).thenReturn(events);

                generatedEvents = eventGeneratorService.generate();
                assertEquals(2, generatedEvents.size());

                // group1
                Set<Member> group1F = generatedEvents.get(0).getMembers();
                assertEquals(2, group1F.size());

                List<UUID> expectedUuidsForGroup1F = List.of(AppTestConstants.UUID_PROFILE_LUCAS,
                                AppTestConstants.UUID_PROFILE_SOPHIA);
                allProfilesMatched = matchAll(group1F, expectedUuidsForGroup1F);
                assertTrue(allProfilesMatched);

                Set<Member> group2F = generatedEvents.get(1).getMembers();
                assertEquals(2, group2F.size());

                List<UUID> expectedUuidsForGroup2F = List.of(AppTestConstants.UUID_PROFILE_LIAM,
                                AppTestConstants.UUID_PROFILE_EMMA);
                allProfilesMatched = matchAll(group2F, expectedUuidsForGroup2F);
                assertTrue(allProfilesMatched);

        }
}

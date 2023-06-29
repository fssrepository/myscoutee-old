package com.raxim.myscoutee.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.common.config.JsonConfig;
import com.raxim.myscoutee.common.config.RepositoryConfig;
import com.raxim.myscoutee.common.repository.MongoDataLoaderTestExecutionListener;
import com.raxim.myscoutee.common.repository.TestData;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.data.mongo.TestEventItem;
import com.raxim.myscoutee.profile.data.document.mongo.EventItem;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.repository.mongo.EventItemRepository;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.service.EventServiceForGenerator;

@DataMongoTest
@DirtiesContext
@Import({ RepositoryConfig.class, JsonConfig.class })
@TestPropertySource(properties = { "de.flapdoodle.mongodb.embedded.version=6.0.6",
        "logging.level.org.springframework.data.mongodb=DEBUG" })
@TestData({ "mongo/profiles.json" })
@TestExecutionListeners(value = MongoDataLoaderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class EventServiceForGeneratorIntTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventItemRepository eventItemRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldSaveGeneratedEvents() {
        objectMapper.addMixIn(EventItem.class, TestEventItem.class);

        EventServiceForGenerator eventServiceForGenerator = new EventServiceForGenerator(eventRepository,
                eventItemRepository);

        List<Profile> profiles = profileRepository.findProfileByStatus("A");

        Map<String, List<Profile>> profilesByGender = profiles.stream()
                .collect(Collectors.groupingBy(Profile::getGender));

        Random random = new Random();

        List<Set<Profile>> groups = IntStream.rangeClosed(1, 1).boxed().map(num -> {
            int groupSize = random.nextInt(3) + 1;
            Set<Profile> profileMergedSet = profilesByGender.keySet().stream().flatMap(gender -> {
                Set<Profile> profileSet = CommonUtil.randomRange(0, profilesByGender.get(gender).size() - 1, groupSize)
                        .stream().map(idx -> profilesByGender.get(gender).get(idx)).collect(Collectors.toSet());
                return profileSet.stream();
            }).collect(Collectors.toSet());
            return profileMergedSet;
        }).toList();

        eventServiceForGenerator.saveEvents(groups);

        List<EventItem> eventItems = this.eventItemRepository.findEventItemsByType("g");

        assertEquals(groups.size(), eventItems.size());

        String eventItemsJson = JsonUtil.toMongoJsonArray(eventItems, objectMapper);
        System.out.println(eventItemsJson);
    }
}

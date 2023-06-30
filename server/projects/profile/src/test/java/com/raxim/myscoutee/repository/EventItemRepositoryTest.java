package com.raxim.myscoutee.repository;

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
import com.raxim.myscoutee.profile.data.document.mongo.EventItem;
import com.raxim.myscoutee.profile.data.document.mongo.EventItemWithCandidates;
import com.raxim.myscoutee.profile.repository.mongo.EventItemRepository;

@DataMongoTest
@DirtiesContext
@Import({ RepositoryConfig.class })
@TestPropertySource(properties = { "de.flapdoodle.mongodb.embedded.version=6.0.6",
        "logging.level.org.springframework.data.mongodb=DEBUG" })
@TestData({ "mongo/items.json" })
@TestExecutionListeners(value = MongoDataLoaderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class EventItemRepositoryTest {

    @Autowired
    private EventItemRepository eventItemRepository;

    @Test
    public void shouldFindEventItemsByType() {
        List<EventItem> eventItems = eventItemRepository.findEventItemsByType("g");
        System.out.println("----" + eventItems.size());
    }

    @Test
    public void shouldFindCandidates() {
        List<EventItemWithCandidates> eventItems = eventItemRepository.findCandidates();
        System.out.println(eventItems.size());
    }
}

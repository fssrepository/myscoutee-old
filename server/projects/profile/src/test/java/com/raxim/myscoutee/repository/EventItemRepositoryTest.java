package com.raxim.myscoutee.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.raxim.myscoutee.common.config.RepositoryConfig;
import com.raxim.myscoutee.profile.data.document.mongo.EventItem;
import com.raxim.myscoutee.profile.repository.mongo.EventItemRepository;

@DataMongoTest
@Import(RepositoryConfig.class)
@TestPropertySource(properties = "de.flapdoodle.mongodb.embedded.version=6.0.6")
public class EventItemRepositoryTest {

    @Autowired
    private EventItemRepository eventItemRepository;

    @Test
    void testSave() {
        var eventItem = new EventItem();

        // int cnt = eventItemRepository.findMembersByItem(null, 0, 0, null, null);
    }
}
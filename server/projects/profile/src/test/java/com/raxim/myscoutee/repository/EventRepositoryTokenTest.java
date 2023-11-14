package com.raxim.myscoutee.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;

import com.raxim.myscoutee.common.AppTestConstants;
import com.raxim.myscoutee.common.config.RepositoryConfig;
import com.raxim.myscoutee.common.repository.MongoDataLoaderTestExecutionListener;
import com.raxim.myscoutee.common.repository.TestData;
import com.raxim.myscoutee.profile.data.document.mongo.EventWithToken;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.util.AppConstants;

@DataMongoTest
@DirtiesContext
@Import({ RepositoryConfig.class })
@TestPropertySource(properties = { "de.flapdoodle.mongodb.embedded.version=6.0.6",
                "logging.level.org.springframework.data.mongodb=DEBUG" })
@TestData({ "mongo/token/profiles.json", "mongo/token/users.json", "mongo/token/events.json",
                "mongo/token/tokens.json" })
@TestExecutionListeners(value = MongoDataLoaderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class EventRepositoryTokenTest {

        @Autowired
        private EventRepository eventRepository;

        @Test
        public void shouldGetTokensForEvent() {

                Optional<EventWithToken> optEventToken = this.eventRepository
                                .findTokensByEvent(AppTestConstants.UUID_EVENT_32);
                assertTrue(optEventToken.isPresent());

                EventWithToken eventWithToken = optEventToken.get();

                assertEquals(3, eventWithToken.getTokens().size());

                assertEquals(AppTestConstants.DEVICE_OLIVER, eventWithToken.getTokens().get(0).getDeviceKey());
                assertEquals(AppConstants.MQTT, eventWithToken.getTokens().get(0).getType());

                assertEquals(AppTestConstants.DEVICE_MASON, eventWithToken.getTokens().get(1).getDeviceKey());
                assertEquals(AppConstants.FIREBASE, eventWithToken.getTokens().get(1).getType());

                assertEquals(AppTestConstants.DEVICE_LUCAS, eventWithToken.getTokens().get(2).getDeviceKey());
                assertEquals(AppConstants.FIREBASE, eventWithToken.getTokens().get(2).getType());
        }

}

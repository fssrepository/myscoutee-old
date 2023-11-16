package com.raxim.myscoutee.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
import com.raxim.myscoutee.profile.data.dto.rest.ImageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.MessageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.repository.mongo.MessageRepository;

@DataMongoTest
@DirtiesContext
@Import({ RepositoryConfig.class })
@TestPropertySource(properties = { "de.flapdoodle.mongodb.embedded.version=6.0.6",
                "logging.level.org.springframework.data.mongodb=DEBUG" })
@TestData({ "mongo/msg/profiles.json", "mongo/msg/messages.json" })
@TestExecutionListeners(value = MongoDataLoaderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class MessageRepositoryTest {

        @Autowired
        private MessageRepository messageRepository;

        @Test
        public void shouldGetLastMessageByChannels() {
                LocalDate createdDate = LocalDate.now();
                String createdDateF = createdDate.atStartOfDay(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                String[] tOffset = new String[] { createdDateF };

                PageParam pageParam = new PageParam(tOffset);
                pageParam.setId(AppTestConstants.UUID_PROFILE_AVA);

                List<MessageDTO> messageDTOs = this.messageRepository.findLastMessageByChannels(pageParam);

                assertEquals(2, messageDTOs.size());
                assertEquals(AppTestConstants.MSG_2, messageDTOs.get(0).getMessage().getValue());
                assertEquals(AppTestConstants.MSG_3, messageDTOs.get(1).getMessage().getValue());

                assertTrue(!messageDTOs.get(0).getFrom().getName().isBlank());
                assertTrue(!messageDTOs.get(1).getFrom().getName().isBlank());
        }

        @Test
        public void shouldMessagesByChannel() {
                LocalDate createdDate = LocalDate.of(1901, 1, 1);
                String createdDateF = createdDate.atStartOfDay(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                String[] tOffset = new String[] { createdDateF };

                PageParam pageParam = new PageParam(tOffset);
                pageParam.setId(AppTestConstants.UUID_PROFILE_AVA);

                List<MessageDTO> messageDTOs = this.messageRepository
                                .findMessagesByChannel(AppTestConstants.UUID_EVENT_1, pageParam);

                assertEquals(2, messageDTOs.size());
                assertEquals(AppTestConstants.MSG_1, messageDTOs.get(0).getMessage().getValue());
                assertEquals(AppTestConstants.MSG_2, messageDTOs.get(1).getMessage().getValue());

                List<ImageDTO> images = messageDTOs.get(0).getReads();
                assertEquals(0, images.size());

                images = messageDTOs.get(1).getReads();
                assertEquals(1, images.size());

                assertTrue(images.get(0).getName().contains(AppTestConstants.NAME_PROFILE_OLIVER));
        }
}

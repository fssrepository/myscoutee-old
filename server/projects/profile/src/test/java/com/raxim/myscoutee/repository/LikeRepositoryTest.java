package com.raxim.myscoutee.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;

import com.raxim.myscoutee.common.config.RepositoryConfig;
import com.raxim.myscoutee.common.repository.MongoDataLoaderTestExecutionListener;
import com.raxim.myscoutee.common.repository.TestData;
import com.raxim.myscoutee.profile.data.dto.rest.LikeGroupDTO;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;

@DataMongoTest
@Import({ RepositoryConfig.class })
@TestPropertySource(properties = { "de.flapdoodle.mongodb.embedded.version=6.0.6",
        "logging.level.org.springframework.data.mongodb=DEBUG" })
@TestData({ "mongo/profiles.json", "mongo/likes.json" })
@TestExecutionListeners(value = MongoDataLoaderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class LikeRepositoryTest {

    @Autowired
    private LikeRepository likeRepository;

    @Test
    public void testShouldFindBothAll() {

        List<LikeGroupDTO> likes = this.likeRepository.findBothAll(0L, 1000L);

        assertEquals(2, likes.size());

        LikeGroupDTO likeGroup1 = likes.get(0);
        assertEquals(2, likeGroup1.getLikes().size());

        assertTrue(likeGroup1.getLikes().stream().anyMatch(
                like -> "Evelyn".equals(like.getFrom().getFirstName())
                        && "Liam".equals(like.getTo().getFirstName())));

        LikeGroupDTO likeGroup2 = likes.get(1);
        assertEquals(2, likeGroup1.getLikes().size());
        assertTrue(likeGroup2.getLikes().stream().anyMatch(
                like -> "Oliver".equals(like.getFrom().getFirstName())
                        && "Mia".equals(like.getTo().getFirstName())));
    }
}

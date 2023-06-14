package com.raxim.myscoutee.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;

import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.raxim.myscoutee.common.config.RepositoryConfig;
import com.raxim.myscoutee.common.repository.MongoDataLoaderTestExecutionListener;
import com.raxim.myscoutee.common.repository.TestData;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;

@DataMongoTest
@Import({ RepositoryConfig.class })
@TestPropertySource(properties = { "de.flapdoodle.mongodb.embedded.version=6.0.6",
        "logging.level.org.springframework.data.mongodb=DEBUG" })
@TestData({ "mongo/groups.json", "mongo/profiles.json", "mongo/likes.json" })
@TestExecutionListeners(value = MongoDataLoaderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class ProfileRepositoryTest {

    private final static UUID UUID_GROUP_DATING = UUID.fromString("b33ec186-aea8-4636-b635-4a2f620a0c54");
    private final static UUID UUID_PROFILE_OLIVER = UUID.fromString("534ccc6b-2547-4bf0-ad91-dca739943ea4");
    private final static Point LOC_OLIVER = new Point(new Position(List.of(47.497912, 19.040235)));
    private final static Object[] OFFSET_START = new Object[] { 0, 0, 0, "1900-01-01" };

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Test
    public void testShouldFindProfilesNotRatedInTwoPages() {
        double typeNonRated = 0.0;
        int step = 1000;
        int limit = 2;

        List<ProfileDTO> profiles = this.profileRepository.findProfile(
                LOC_OLIVER, OFFSET_START, limit,
                step, UUID_PROFILE_OLIVER, "w",
                UUID_GROUP_DATING,
                typeNonRated, 0);

        assertEquals(2, profiles.size());

        ProfileDTO profile1 = profiles.get(0);
        assertEquals(1000.0, profile1.getGroupKey());
        assertEquals("Lily", profile1.getProfile().getFirstName());

        ProfileDTO profile2 = profiles.get(1);
        assertEquals(1000.0, profile2.getGroupKey());
        assertEquals("Evelyn", profile2.getProfile().getFirstName());

        // next page with last offset
        Object[] tOffset = new ArrayList<>(profile2.getOffset()).toArray();

        profiles = this.profileRepository.findProfile(
                LOC_OLIVER, tOffset, limit,
                step, UUID_PROFILE_OLIVER, "w",
                UUID_GROUP_DATING,
                typeNonRated, 0);

        assertEquals(1, profiles.size());

        profile1 = profiles.get(0);
        assertEquals(2000.0, profile1.getGroupKey());
        assertEquals("Amelia", profile1.getProfile().getFirstName());
    }

    @Test
    public void testShouldFindProfilesRateGive() {
        double typeNonRated = 1.0;
        int step = 1000;
        int limit = 2;

        List<ProfileDTO> profiles = this.profileRepository.findProfile(
                LOC_OLIVER, OFFSET_START, limit,
                step, UUID_PROFILE_OLIVER, "w",
                UUID_GROUP_DATING,
                typeNonRated, 0);

        assertEquals(2, profiles.size());
    }
}

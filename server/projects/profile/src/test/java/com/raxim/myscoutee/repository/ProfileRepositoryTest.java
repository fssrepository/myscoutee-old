package com.raxim.myscoutee.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;

import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.raxim.myscoutee.common.config.RepositoryConfig;
import com.raxim.myscoutee.common.repository.MongoDataLoaderTestExecutionListener;
import com.raxim.myscoutee.common.repository.TestData;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;

@DataMongoTest
@DirtiesContext
@Import({ RepositoryConfig.class })
@TestPropertySource(properties = { "de.flapdoodle.mongodb.embedded.version=6.0.6",
                "logging.level.org.springframework.data.mongodb=DEBUG" })
@TestData({ "mongo/groups.json", "mongo/profiles.json", "mongo/likes.json" })
@TestExecutionListeners(value = MongoDataLoaderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class ProfileRepositoryTest {

        private final static UUID UUID_GROUP_DATING = UUID.fromString("b33ec186-aea8-4636-b635-4a2f620a0c54");
        private final static Point LOCATION_DEFAULT = new Point(new Position(List.of(47.497912, 19.040235)));
        private final static Object[] OFFSET_START_DEFAULT = new Object[] { 0, 0, 0, "1900-01-01" };

        private final static UUID UUID_PROFILE_OLIVER = UUID.fromString("534ccc6b-2547-4bf0-ad91-dca739943ea4");
        private final static UUID UUID_PROFILE_LIAM = UUID.fromString("8f4bd3a8-1195-01b0-172a-a04f052f5982");

        @Autowired
        private ProfileRepository profileRepository;

        @Test
        public void testShouldFindProfilesNotRatedForOliver() {
                double direction = 0.0;
                int step = 1000;
                int limit = 2;

                List<ProfileDTO> profiles = this.profileRepository.findProfile(
                                LOCATION_DEFAULT, OFFSET_START_DEFAULT, limit,
                                step, UUID_PROFILE_OLIVER, "w",
                                UUID_GROUP_DATING,
                                direction, 0);

                assertEquals(1, profiles.size());

                ProfileDTO profile1 = profiles.get(0);
                assertEquals(2000.0, profile1.getGroupKey());
                assertEquals("Amelia", profile1.getProfile().getFirstName());
                assertEquals(0d, profile1.getRate());
        }

        @Test
        public void testShouldFindProfilesNotRatedForLiamInTwoPages() {
                double direction = 0.0;
                int step = 1000;
                int limit = 2;

                List<ProfileDTO> profiles = this.profileRepository.findProfile(
                                LOCATION_DEFAULT, OFFSET_START_DEFAULT, limit,
                                step, UUID_PROFILE_LIAM, "w",
                                UUID_GROUP_DATING,
                                direction, 0);

                assertEquals(2, profiles.size());

                ProfileDTO profile1 = profiles.get(0);
                assertEquals(1000.0, profile1.getGroupKey());
                assertEquals("Mia", profile1.getProfile().getFirstName());
                assertEquals(0d, profile1.getRate());

                ProfileDTO profile2 = profiles.get(1);
                assertEquals(2000.0, profile2.getGroupKey());
                assertEquals("Amelia", profile2.getProfile().getFirstName());
                assertEquals(0d, profile2.getRate());

                Object[] nextOffset = profile2.getOffset().toArray();

                profiles = this.profileRepository.findProfile(
                                LOCATION_DEFAULT, nextOffset, limit,
                                step, UUID_PROFILE_LIAM, "w",
                                UUID_GROUP_DATING,
                                direction, 0);

                assertEquals(1, profiles.size());

                profile1 = profiles.get(0);
                assertEquals(2000.0, profile1.getGroupKey());
                assertEquals("Charlotte", profile1.getProfile().getFirstName());
                assertEquals(0d, profile1.getRate());
        }

        @Test
        public void testShouldFindProfilesRateGiveForOliver() {
                double direction = 1.0;
                int step = 1000;
                int limit = 2;

                List<ProfileDTO> profiles = this.profileRepository.findProfile(
                                LOCATION_DEFAULT, OFFSET_START_DEFAULT, limit,
                                step, UUID_PROFILE_OLIVER, "w",
                                UUID_GROUP_DATING,
                                direction, 0);

                assertEquals(1, profiles.size());
                ProfileDTO profile1 = profiles.get(0);
                assertEquals(2000.0, profile1.getGroupKey());
                assertEquals("Charlotte", profile1.getProfile().getFirstName());
                assertEquals(9d, profile1.getRate());
        }

        @Test
        @DisplayName("Find Profiles Rate Receive For Oliver with Larger Page Size Than the Result")
        public void testShouldFindProfilesRateReceiveForOliver() {
                double direction = 2.0;
                int step = 1000;
                int limit = 3;

                List<ProfileDTO> profiles = this.profileRepository.findProfile(
                                LOCATION_DEFAULT, OFFSET_START_DEFAULT, limit,
                                step, UUID_PROFILE_OLIVER, "w",
                                UUID_GROUP_DATING,
                                direction, 0);

                assertEquals(2, profiles.size());

                ProfileDTO profile1 = profiles.get(0);
                assertEquals(1000.0, profile1.getGroupKey());
                assertEquals("Lily", profile1.getProfile().getFirstName());
                assertEquals(6d, profile1.getRate());

                ProfileDTO profile2 = profiles.get(1);
                assertEquals(1000.0, profile2.getGroupKey());
                assertEquals("Evelyn", profile2.getProfile().getFirstName());
                assertEquals(2d, profile2.getRate());
        }

        @Test
        @DisplayName("Find Profiles Rate Both For Oliver")
        public void testShouldFindProfilesRateBothForOliver() {
                double direction = 1.5;
                int step = 1000;
                int limit = 3;

                List<ProfileDTO> profiles = this.profileRepository.findProfile(
                                LOCATION_DEFAULT, OFFSET_START_DEFAULT, limit,
                                step, UUID_PROFILE_OLIVER, "w",
                                UUID_GROUP_DATING,
                                direction, 0);

                assertEquals(1, profiles.size());

                ProfileDTO profile1 = profiles.get(0);
                assertEquals(1000.0, profile1.getGroupKey());
                assertEquals("Mia", profile1.getProfile().getFirstName());
                assertEquals(6.461538461538462d, profile1.getRate());
        }
}

package com.raxim.myscoutee.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import com.raxim.myscoutee.profile.data.document.mongo.EventItem;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.repository.mongo.EventItemRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.util.AppConstants;

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
        private final static UUID UUID_PROFILE_AMELIA = UUID.fromString("a84e4e95-d9c6-bd2c-2ed8-66e52cf2f5bb");
        private final static UUID UUID_PROFILE_MIA = UUID.fromString("4f837de6-5d83-2ddb-f545-c12539f490bc");
        private final static UUID UUID_PROFILE_CHARLOTTE = UUID.fromString("2b418324-7733-97ed-6730-bd1e3d589aa8");
        private final static UUID UUID_PROFILE_LILY = UUID.fromString("ed49d5e2-2227-681a-4017-2c21cb1c68bc");
        private final static UUID UUID_PROFILE_EVELYIN = UUID.fromString("428c6c68-23b9-07c7-71e7-720b001abdb7");

        @Autowired
        private ProfileRepository profileRepository;

        @Autowired
        private EventItemRepository eventItemRepository;

        @Test
        public void testShouldFindProfilesNotRatedForOliver() {
                double direction = 0.0;
                int step = 1000;
                int limit = 2;

                List<ProfileDTO> profiles = this.profileRepository.findProfile(
                                LOCATION_DEFAULT, OFFSET_START_DEFAULT, limit,
                                step, UUID_PROFILE_OLIVER, AppConstants.WOMAN,
                                UUID_GROUP_DATING,
                                direction, 0);

                assertEquals(1, profiles.size());

                ProfileDTO profile1 = profiles.get(0);
                assertEquals(2000.0, profile1.getGroupKey());
                assertEquals(UUID_PROFILE_AMELIA, profile1.getProfile().getId());
                assertEquals(0d, profile1.getRate());
                assertFalse(profile1.getMet());
        }

        @Test
        public void testShouldNotFindProfilesNotRatedForOliverMet() {

                Profile profileOliver = this.profileRepository.findById(UUID_PROFILE_OLIVER).get();
                Profile profileAmelia = this.profileRepository.findById(UUID_PROFILE_AMELIA).get();

                EventItem eventItem = new EventItem();
                eventItem.setId(UUID.randomUUID());
                Set<Member> members = new HashSet<>();
                Member memberOliver = new Member(profileOliver);
                members.add(memberOliver);
                Member memberAmelia = new Member(profileAmelia);
                members.add(memberAmelia);
                eventItem.setMembers(members);

                this.eventItemRepository.save(eventItem);

                double direction = 0.0;
                int step = 1000;
                int limit = 2;

                List<ProfileDTO> profiles = this.profileRepository.findProfile(
                                LOCATION_DEFAULT, OFFSET_START_DEFAULT, limit,
                                step, UUID_PROFILE_OLIVER, AppConstants.WOMAN,
                                UUID_GROUP_DATING,
                                direction, 0);

                assertEquals(0, profiles.size());
        }

        @Test
        public void testShouldFindProfilesNotRatedForLiamInTwoPages() {
                double direction = 0.0;
                int step = 1000;
                int limit = 2;

                List<ProfileDTO> profiles = this.profileRepository.findProfile(
                                LOCATION_DEFAULT, OFFSET_START_DEFAULT, limit,
                                step, UUID_PROFILE_LIAM, AppConstants.WOMAN,
                                UUID_GROUP_DATING,
                                direction, 0);

                assertEquals(2, profiles.size());

                ProfileDTO profile1 = profiles.get(0);
                assertEquals(1000.0, profile1.getGroupKey());
                assertEquals(UUID_PROFILE_MIA, profile1.getProfile().getId());
                assertEquals(0d, profile1.getRate());
                assertFalse(profile1.getMet());

                ProfileDTO profile2 = profiles.get(1);
                assertEquals(2000.0, profile2.getGroupKey());
                assertEquals(UUID_PROFILE_AMELIA, profile2.getProfile().getId());
                assertEquals(0d, profile2.getRate());
                assertFalse(profile2.getMet());

                Object[] nextOffset = profile2.getOffset().toArray();

                profiles = this.profileRepository.findProfile(
                                LOCATION_DEFAULT, nextOffset, limit,
                                step, UUID_PROFILE_LIAM, AppConstants.WOMAN,
                                UUID_GROUP_DATING,
                                direction, 0);

                assertEquals(1, profiles.size());

                profile1 = profiles.get(0);
                assertEquals(2000.0, profile1.getGroupKey());
                assertEquals(UUID_PROFILE_CHARLOTTE, profile1.getProfile().getId());
                assertEquals(0d, profile1.getRate());
                assertFalse(profile1.getMet());
        }

        @Test
        public void testShouldFindProfilesRateGiveForOliver() {
                double direction = 1.0;
                int step = 1000;
                int limit = 2;

                List<ProfileDTO> profiles = this.profileRepository.findProfile(
                                LOCATION_DEFAULT, OFFSET_START_DEFAULT, limit,
                                step, UUID_PROFILE_OLIVER, AppConstants.WOMAN,
                                UUID_GROUP_DATING,
                                direction, 0);

                assertEquals(1, profiles.size());
                ProfileDTO profile1 = profiles.get(0);
                assertEquals(2000.0, profile1.getGroupKey());
                assertEquals(UUID_PROFILE_CHARLOTTE, profile1.getProfile().getId());
                assertEquals(9d, profile1.getRate());
                assertFalse(profile1.getMet());
        }

        @Test
        @DisplayName("Find Profiles Rate Receive For Oliver with Larger Page Size Than the Result")
        public void testShouldFindProfilesRateReceiveForOliver() {
                double direction = 2.0;
                int step = 1000;
                int limit = 3;

                List<ProfileDTO> profiles = this.profileRepository.findProfile(
                                LOCATION_DEFAULT, OFFSET_START_DEFAULT, limit,
                                step, UUID_PROFILE_OLIVER, AppConstants.WOMAN,
                                UUID_GROUP_DATING,
                                direction, 0);

                assertEquals(2, profiles.size());

                ProfileDTO profile1 = profiles.get(0);
                assertEquals(1000.0, profile1.getGroupKey());
                assertEquals(UUID_PROFILE_LILY, profile1.getProfile().getId());
                assertEquals(6d, profile1.getRate());
                assertFalse(profile1.getMet());

                ProfileDTO profile2 = profiles.get(1);
                assertEquals(1000.0, profile2.getGroupKey());
                assertEquals(UUID_PROFILE_EVELYIN, profile2.getProfile().getId());
                assertEquals(2d, profile2.getRate());
                assertFalse(profile2.getMet());
        }

        @Test
        @DisplayName("Find Profiles Rate Both For Oliver")
        public void testShouldFindProfilesRateBothForOliver() {
                double direction = 1.5;
                int step = 1000;
                int limit = 3;

                List<ProfileDTO> profiles = this.profileRepository.findProfile(
                                LOCATION_DEFAULT, OFFSET_START_DEFAULT, limit,
                                step, UUID_PROFILE_OLIVER, AppConstants.WOMAN,
                                UUID_GROUP_DATING,
                                direction, 0);

                assertEquals(1, profiles.size());

                ProfileDTO profile1 = profiles.get(0);
                assertEquals(1000.0, profile1.getGroupKey());
                assertEquals(UUID_PROFILE_MIA, profile1.getProfile().getId());
                assertEquals(6.461538461538462d, profile1.getRate());
                assertFalse(profile1.getMet());
        }

        @Test
        public void testShouldFindProfilesRateBothForOliverMet() {

                Profile profileOliver = this.profileRepository.findById(UUID_PROFILE_OLIVER).get();
                Profile profileMia = this.profileRepository.findById(UUID_PROFILE_MIA).get();

                EventItem eventItem = new EventItem();
                eventItem.setId(UUID.randomUUID());
                Set<Member> members = new HashSet<>();
                Member memberOliver = new Member(profileOliver);
                members.add(memberOliver);
                Member memberAmelia = new Member(profileMia);
                members.add(memberAmelia);
                eventItem.setMembers(members);

                this.eventItemRepository.save(eventItem);

                double direction = 1.5;
                int step = 1000;
                int limit = 3;

                List<ProfileDTO> profiles = this.profileRepository.findProfile(
                                LOCATION_DEFAULT, OFFSET_START_DEFAULT, limit,
                                step, UUID_PROFILE_OLIVER, AppConstants.WOMAN,
                                UUID_GROUP_DATING,
                                direction, 0);

                assertEquals(1, profiles.size());

                ProfileDTO profile1 = profiles.get(0);
                assertEquals(1000.0, profile1.getGroupKey());
                assertEquals(UUID_PROFILE_MIA, profile1.getProfile().getId());
                assertEquals(6.461538461538462d, profile1.getRate());
                assertTrue(profile1.getMet());
        }
}

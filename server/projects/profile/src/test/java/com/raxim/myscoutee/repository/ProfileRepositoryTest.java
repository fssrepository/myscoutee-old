package com.raxim.myscoutee.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
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
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.RangeLocal;
import com.raxim.myscoutee.profile.data.dto.rest.ISODateRange;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
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

        private final static Object[] OFFSET_START_DEFAULT = new Object[] { 0, 0, 0, "1900-01-01" };

        @Autowired
        private ProfileRepository profileRepository;

        @Autowired
        private EventRepository eventRepository;

        private static RangeLocal ageRange;

        @BeforeAll
        public static void init() {
                LocalDateTime bStart = LocalDateTime.now().minus(100, ChronoUnit.YEARS);
                LocalDateTime bEnd = LocalDateTime.now().minus(18, ChronoUnit.YEARS);

                ageRange = new RangeLocal(bStart, bEnd);
        }

        @Test
        public void testShouldFindProfilesNotRatedForOliver() {
                double direction = 0.0;
                int step = 1000;
                int limit = 2;

                PageParam pageParam = new PageParam();
                pageParam.setOffset(OFFSET_START_DEFAULT);
                pageParam.setLimit(limit);
                pageParam.setStep(step);
                pageParam.setId(AppTestConstants.UUID_PROFILE_OLIVER);

                List<ProfileDTO> profiles = this.profileRepository.findProfile(
                                pageParam,
                                AppTestConstants.LOCATION_PROFILE_OLIVER, AppConstants.WOMAN,
                                AppTestConstants.UUID_GROUP_DATING,
                                direction, 0, false, null, ageRange);

                assertEquals(1, profiles.size());

                ProfileDTO profile1 = profiles.get(0);
                assertEquals(2000.0, profile1.getGroupKey());
                assertEquals(AppTestConstants.UUID_PROFILE_AMELIA, profile1.getProfile().getId());
                assertEquals(0d, profile1.getRate());
                assertFalse(profile1.getMet());
        }

        @Test
        public void testShouldNotFindProfilesNotRatedForOliverMet() {

                Profile profileOliver = this.profileRepository.findById(AppTestConstants.UUID_PROFILE_OLIVER).get();
                Profile profileAmelia = this.profileRepository.findById(AppTestConstants.UUID_PROFILE_AMELIA).get();

                Event event = new Event();
                event.setId(UUID.randomUUID());
                event.setStatus("A");
                Set<Member> members = new HashSet<>();
                Member memberOliver = new Member(profileOliver);
                members.add(memberOliver);
                Member memberAmelia = new Member(profileAmelia);
                members.add(memberAmelia);
                event.setMembers(members);

                this.eventRepository.save(event);

                double direction = 0.0;
                int step = 1000;
                int limit = 2;

                PageParam pageParam = new PageParam();
                pageParam.setOffset(OFFSET_START_DEFAULT);
                pageParam.setLimit(limit);
                pageParam.setStep(step);
                pageParam.setId(AppTestConstants.UUID_PROFILE_OLIVER);

                List<ProfileDTO> profiles = this.profileRepository.findProfile(
                                pageParam,
                                AppTestConstants.LOCATION_PROFILE_OLIVER, AppConstants.WOMAN,
                                AppTestConstants.UUID_GROUP_DATING,
                                direction, 0, false, null, ageRange);

                assertEquals(0, profiles.size());
        }

        @Test
        public void testShouldFindProfilesNotRatedForLiamInTwoPages() {
                double direction = 0.0;
                int step = 1000;
                int limit = 2;

                PageParam pageParam = new PageParam();
                pageParam.setOffset(OFFSET_START_DEFAULT);
                pageParam.setLimit(limit);
                pageParam.setStep(step);
                pageParam.setId(AppTestConstants.UUID_PROFILE_LIAM);

                List<ProfileDTO> profiles = this.profileRepository.findProfile(
                                pageParam,
                                AppTestConstants.LOCATION_PROFILE_LIAM, AppConstants.WOMAN,
                                AppTestConstants.UUID_GROUP_DATING,
                                direction, 0, false, null, ageRange);

                assertEquals(2, profiles.size());

                ProfileDTO profile1 = profiles.get(0);
                assertEquals(1000.0, profile1.getGroupKey());
                assertEquals(AppTestConstants.UUID_PROFILE_MIA, profile1.getProfile().getId());
                assertEquals(0d, profile1.getRate());
                assertFalse(profile1.getMet());

                ProfileDTO profile2 = profiles.get(1);
                assertEquals(2000.0, profile2.getGroupKey());
                assertEquals(AppTestConstants.UUID_PROFILE_CHARLOTTE, profile2.getProfile().getId());
                assertEquals(0d, profile2.getRate());
                assertFalse(profile2.getMet());

                Object[] nextOffset = profile2.getOffset().toArray();

                pageParam.setOffset(nextOffset);

                profiles = this.profileRepository.findProfile(pageParam,
                                AppTestConstants.LOCATION_PROFILE_LIAM, AppConstants.WOMAN,
                                AppTestConstants.UUID_GROUP_DATING,
                                direction, 0, false, null, ageRange);

                assertEquals(1, profiles.size());

                profile1 = profiles.get(0);
                assertEquals(2000.0, profile1.getGroupKey());
                assertEquals(AppTestConstants.UUID_PROFILE_AMELIA, profile1.getProfile().getId());
                assertEquals(0d, profile1.getRate());
                assertFalse(profile1.getMet());
        }

        @Test
        public void testShouldFindProfilesRateGiveForOliver() {
                double direction = 1.0;
                int step = 1000;
                int limit = 2;

                PageParam pageParam = new PageParam();
                pageParam.setOffset(OFFSET_START_DEFAULT);
                pageParam.setLimit(limit);
                pageParam.setStep(step);
                pageParam.setId(AppTestConstants.UUID_PROFILE_OLIVER);

                List<ProfileDTO> profiles = this.profileRepository.findProfile(
                                pageParam,
                                AppTestConstants.LOCATION_PROFILE_OLIVER, AppConstants.WOMAN,
                                AppTestConstants.UUID_GROUP_DATING,
                                direction, 0, false, null, ageRange);

                assertEquals(1, profiles.size());
                ProfileDTO profile1 = profiles.get(0);
                assertEquals(2000.0, profile1.getGroupKey());
                assertEquals(AppTestConstants.UUID_PROFILE_CHARLOTTE, profile1.getProfile().getId());
                assertEquals(9d, profile1.getRate());
                assertFalse(profile1.getMet());
        }

        @Test
        @DisplayName("Find Profiles Rate Receive For Oliver with Larger Page Size Than the Result")
        public void testShouldFindProfilesRateReceiveForOliver() {
                double direction = 2.0;
                int step = 1000;
                int limit = 3;

                PageParam pageParam = new PageParam();
                pageParam.setOffset(OFFSET_START_DEFAULT);
                pageParam.setLimit(limit);
                pageParam.setStep(step);
                pageParam.setId(AppTestConstants.UUID_PROFILE_OLIVER);

                List<ProfileDTO> profiles = this.profileRepository.findProfile(
                                pageParam,
                                AppTestConstants.LOCATION_PROFILE_OLIVER, AppConstants.WOMAN,
                                AppTestConstants.UUID_GROUP_DATING,
                                direction, 0, false, null, ageRange);

                assertEquals(2, profiles.size());

                ProfileDTO profile1 = profiles.get(0);
                assertEquals(1000.0, profile1.getGroupKey());
                assertEquals(AppTestConstants.UUID_PROFILE_LILY, profile1.getProfile().getId());
                assertEquals(0d, profile1.getRate()); // does not show the rate of received (avoid bias)
                assertFalse(profile1.getMet());

                ProfileDTO profile2 = profiles.get(1);
                assertEquals(1000.0, profile2.getGroupKey());
                assertEquals(AppTestConstants.UUID_PROFILE_EVELYIN, profile2.getProfile().getId());
                assertEquals(0d, profile2.getRate()); // does not show the rate of received (avoid bias)
                assertFalse(profile2.getMet());
        }

        @Test
        @DisplayName("Find Profiles Rate Both For Oliver")
        public void testShouldFindProfilesRateBothForOliver() {
                double direction = 1.5;
                int step = 1000;
                int limit = 3;

                PageParam pageParam = new PageParam();
                pageParam.setOffset(OFFSET_START_DEFAULT);
                pageParam.setLimit(limit);
                pageParam.setStep(step);
                pageParam.setId(AppTestConstants.UUID_PROFILE_OLIVER);

                List<ProfileDTO> profiles = this.profileRepository.findProfile(
                                pageParam,
                                AppTestConstants.LOCATION_PROFILE_OLIVER, AppConstants.WOMAN,
                                AppTestConstants.UUID_GROUP_DATING,
                                direction, 0, false, null, ageRange);

                assertEquals(1, profiles.size());

                ProfileDTO profile1 = profiles.get(0);
                assertEquals(1000.0, profile1.getGroupKey());
                assertEquals(AppTestConstants.UUID_PROFILE_MIA, profile1.getProfile().getId());
                assertEquals(6d, profile1.getRate());
                assertFalse(profile1.getMet());
        }

        @Test
        public void testShouldFindProfilesRateBothForOliverMet() {

                Profile profileOliver = this.profileRepository.findById(AppTestConstants.UUID_PROFILE_OLIVER).get();
                Profile profileMia = this.profileRepository.findById(AppTestConstants.UUID_PROFILE_MIA).get();

                Event event = new Event();
                event.setId(UUID.randomUUID());
                event.setStatus("A");
                Set<Member> members = new HashSet<>();
                Member memberOliver = new Member(profileOliver);
                members.add(memberOliver);
                Member memberMia = new Member(profileMia);
                members.add(memberMia);
                event.setMembers(members);

                this.eventRepository.save(event);

                double direction = 1.5;
                int step = 1000;
                int limit = 3;

                PageParam pageParam = new PageParam();
                pageParam.setOffset(OFFSET_START_DEFAULT);
                pageParam.setLimit(limit);
                pageParam.setStep(step);
                pageParam.setId(AppTestConstants.UUID_PROFILE_OLIVER);

                List<ProfileDTO> profiles = this.profileRepository.findProfile(pageParam,
                                AppTestConstants.LOCATION_PROFILE_OLIVER, AppConstants.WOMAN,
                                AppTestConstants.UUID_GROUP_DATING,
                                direction, 0, true, null, ageRange);

                assertEquals(1, profiles.size());

                ProfileDTO profile1 = profiles.get(0);
                assertEquals(1000.0, profile1.getGroupKey());
                assertEquals(AppTestConstants.UUID_PROFILE_MIA, profile1.getProfile().getId());
                assertEquals(6d, profile1.getRate());
                assertTrue(profile1.getMet());
        }
}

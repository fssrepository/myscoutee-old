package com.raxim.myscoutee.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.common.config.JsonConfig;
import com.raxim.myscoutee.data.mongo.TestProfile;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.repository.mongo.ProfileEventHandler;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;
import com.raxim.myscoutee.profile.service.ProfileService;
import com.raxim.myscoutee.profile.util.ProfileUtil;
import com.raxim.myscoutee.util.TestJsonUtil;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(classes = JsonConfig.class)
public class ProfileServiceRepositoryTest {

    @InjectMocks
    private ProfileService profileService;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileEventHandler profileEventHandler;

    @Autowired
    @Spy
    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<Profile> captorProfile;

    @Test
    public void shouldSaveProfile() throws FileNotFoundException, IOException {

        MockMultipartFile voice = new MockMultipartFile("file", "img.canvas", "multipart/form-data",
                new FileInputStream(Paths.get("src/test/resources/file", "img.canvas").toFile()));

        Profile[] profileArray = TestJsonUtil.loadJson(this, "rest/profiles.json",
                TestProfile[].class,
                objectMapper);
        List<Profile> profiles = Arrays.asList(profileArray);

        try (MockedStatic<ProfileUtil> mockedProfileUtil = mockStatic(ProfileUtil.class)) {
            mockedProfileUtil.when(() -> ProfileUtil.saveVoice(eq(voice), any()))
                    .thenAnswer((Answer<Void>) invocation -> null);
            mockedProfileUtil.when(() -> ProfileUtil.score(any(Profile.class))).thenReturn(10);
        }

        when(profileRepository.findById(profiles.get(0).getId()))
                .thenReturn(Optional.of(profiles.get(0)));

        this.profileService.saveProfile(UUID.randomUUID(), null,
                UUID.randomUUID(), profiles.get(0), voice);

        Mockito.verify(profileRepository).save(captorProfile.capture());
        Profile capturedProfile = captorProfile.getValue();

        assertNotEquals(profiles.get(0).getId(), capturedProfile.getId());

        this.profileService.saveProfile(UUID.randomUUID(), profiles.get(0).getId(),
                UUID.randomUUID(), profiles.get(0), voice);

        Mockito.verify(profileRepository, Mockito.times(2)).save(captorProfile.capture());
        capturedProfile = captorProfile.getValue();

        assertEquals(profiles.get(0).getId(), capturedProfile.getId());
    }
}

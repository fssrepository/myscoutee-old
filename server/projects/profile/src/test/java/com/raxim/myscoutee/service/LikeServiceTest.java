package com.raxim.myscoutee.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.algo.AbstractAlgoTest;
import com.raxim.myscoutee.common.config.JsonConfig;
import com.raxim.myscoutee.data.mongo.TestLike;
import com.raxim.myscoutee.data.mongo.TestProfile;
import com.raxim.myscoutee.profile.data.document.mongo.Like;
import com.raxim.myscoutee.profile.data.document.mongo.LikeGroup;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Sequence;
import com.raxim.myscoutee.profile.data.dto.rest.LikeDTO;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.SequenceRepository;
import com.raxim.myscoutee.profile.service.LikeService;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(classes = JsonConfig.class)
public class LikeServiceTest extends AbstractAlgoTest {

        private static final UUID UUID_PROFILE_SOPHIA = UUID.fromString("39402632-a452-57be-2518-53cc117b1abc");
        private static final String SEQENCE_KEY = "likes";

        @InjectMocks
        private LikeService likeService;

        @Mock
        private ProfileRepository profileRepository;

        @Mock
        private LikeRepository likeRepository;

        @Mock
        private SequenceRepository sequenceRepository;

        @Captor
        private ArgumentCaptor<List<Like>> captorLikes;

        @Autowired
        @Spy
        private ObjectMapper objectMapper;

        @Test
        public void testShouldAllLikesSave() throws IOException {
                objectMapper.addMixIn(Profile.class, TestProfile.class);
                objectMapper.addMixIn(Like.class, TestLike.class);

                LikeDTO[] likeArray = loadJson(this, "rest/likesForGroupSave.json",
                                LikeDTO[].class, objectMapper);
                Profile[] profileArray = loadJson(this, "rest/profiles.json",
                                TestProfile[].class,
                                objectMapper);
                LikeGroup[] likeGroupsArray = loadJson(this, "mongo/likeGroups.json",
                                LikeGroup[].class,
                                objectMapper);

                List<LikeDTO> likes = Arrays.asList(likeArray);
                List<LikeGroup> likeGroups = Arrays.asList(likeGroupsArray);
                List<Profile> profiles = Arrays.asList(profileArray);

                when(likeRepository.findByParty(eq(UUID_PROFILE_SOPHIA), anyList())).thenReturn(likeGroups);
                when(profileRepository.findAllById(anySet())).thenReturn(profiles);

                final Sequence sequence = new Sequence(SEQENCE_KEY, 2L + 2L); // two existing and two new group
                when(sequenceRepository.nextValue(eq(SEQENCE_KEY), eq(2L))).thenReturn(sequence);

                // Sophia is the first profile
                likeService.saveLikes(profiles.get(0), likes);

                Mockito.verify(likeRepository).saveAll(captorLikes.capture());
                List<Like> capturedLikes = captorLikes.getValue();

                assertEquals(4, capturedLikes.size());

                Like like1 = capturedLikes.get(0);
                assertEquals("Sophia", like1.getFrom().getFirstName());
                assertEquals("Oliver", like1.getTo().getFirstName());
                assertEquals("A", like1.getStatus());
                assertEquals(8d, like1.getRate());
                assertEquals(1, like1.getCnt());

                Like like2 = capturedLikes.get(1);
                assertEquals("Sophia", like2.getFrom().getFirstName());
                assertEquals("Ethan", like2.getTo().getFirstName());
                assertEquals("A", like2.getStatus());
                assertEquals(9d, like2.getRate());
                assertEquals(3, like2.getCnt());

                Like like3 = capturedLikes.get(2);
                assertEquals("Sophia", like3.getFrom().getFirstName());
                assertEquals("Noah", like3.getTo().getFirstName());
                assertEquals("A", like3.getStatus());
                assertEquals(2d, like3.getRate());
                assertEquals(2, like3.getCnt());

                Like like4 = capturedLikes.get(3);
                assertEquals("Lucas", like4.getFrom().getFirstName());
                assertEquals("Emma", like4.getTo().getFirstName());
                assertEquals("D", like4.getStatus());
                assertEquals(6d, like4.getRate());
                assertEquals(4, like4.getCnt());

        }
}

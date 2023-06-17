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
import org.mockito.junit.jupiter.MockitoExtension;

import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Like;
import com.raxim.myscoutee.profile.data.document.mongo.LikeGroup;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.ProfileForGroup;
import com.raxim.myscoutee.profile.data.dto.rest.LikeDTO;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.service.LikeService;

@ExtendWith(MockitoExtension.class)
public class LikeServiceTest {

    private static final UUID UUID_PROFILE_SOPHIA = UUID.fromString("39402632-a452-57be-2518-53cc117b1abc");

    @InjectMocks
    private LikeService likeService;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private LikeRepository likeRepository;

    @Captor
    private ArgumentCaptor<List<Like>> captorLikes;

    @Test
    public void testShouldAllLikesSave() throws IOException {
        LikeDTO[] likeArray = JsonUtil.loadJson(this, "rest/likesForGroupSave.json", LikeDTO[].class);
        Profile[] profileArray = JsonUtil.loadJson(this, "rest/profiles.json", ProfileForGroup[].class);

        LikeGroup[] likeGroupsArray = JsonUtil.loadJson(this, "mongo/likeGroups.json", LikeGroup[].class);

        List<LikeDTO> likes = Arrays.asList(likeArray);
        List<LikeGroup> likeGroups = Arrays.asList(likeGroupsArray);
        List<Profile> profiles = Arrays.asList(profileArray);

        when(likeRepository.findByParty(eq(UUID_PROFILE_SOPHIA), anyList()))
                .thenReturn(likeGroups);
        when(profileRepository.findAllById(anySet()))
                .thenReturn(profiles);

        // Sophia is the first profile
        likeService.saveLikes(profiles.get(0), likes);

        Mockito.verify(likeRepository).saveAll(captorLikes.capture());
        List<Like> capturedLikes = captorLikes.getValue();

        assertEquals(5, capturedLikes.size());

        // cnt implementation!!!

        Like like1 = capturedLikes.get(0);
        assertEquals("Sophia", like1.getFrom().getFirstName());
        assertEquals("Oliver", like1.getTo().getFirstName());
        assertEquals("P", like1.getStatus());
        assertEquals(8d, like1.getRate());

        Like like2 = capturedLikes.get(1);
        assertEquals("Oliver", like2.getFrom().getFirstName());
        assertEquals("Sophia", like2.getTo().getFirstName());
        assertEquals("P", like2.getStatus());
        assertEquals(7d, like2.getRate());
        assertEquals(1, like2.getCnt());

        Like like3 = capturedLikes.get(2);
        assertEquals("Sophia", like3.getFrom().getFirstName());
        assertEquals("Ethan", like3.getTo().getFirstName());
        assertEquals("A", like3.getStatus());
        assertEquals(9d, like3.getRate());

        Like like4 = capturedLikes.get(3);
        assertEquals("Sophia", like4.getFrom().getFirstName());
        assertEquals("Noah", like4.getTo().getFirstName());
        assertEquals("A", like4.getStatus());
        assertEquals(2d, like4.getRate());

        Like like5 = capturedLikes.get(4);
        assertEquals("Lucas", like5.getFrom().getFirstName());
        assertEquals("Emma", like5.getTo().getFirstName());
        assertEquals("D", like5.getStatus());
        assertEquals(6d, like5.getRate());

    }
}

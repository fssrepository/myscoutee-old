package com.raxim.myscoutee.profile.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.profile.data.document.mongo.Group;
import com.raxim.myscoutee.profile.data.document.mongo.Link;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.data.dto.rest.LinkDTO;
import com.raxim.myscoutee.profile.data.dto.rest.LinkInfoDTO;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.data.dto.rest.RewardDTO;
import com.raxim.myscoutee.profile.repository.mongo.GroupRepository;
import com.raxim.myscoutee.profile.repository.mongo.LinkRepository;
import com.raxim.myscoutee.profile.service.ProfileService;

@RepositoryRestController
@RequestMapping("user")
public class UserProfileRestController {
    private final ProfileService profileService;
    private final GroupRepository groupRepository;
    private final LinkRepository linkRepository;

    public UserProfileRestController(ProfileService profileService, GroupRepository groupRepository,
            LinkRepository linkRepository) {
        this.profileService = profileService;
        this.groupRepository = groupRepository;
        this.linkRepository = linkRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileDTO> getProfile(Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();

        if (profile != null) {
            ProfileDTO profileDto = new ProfileDTO(profile);
            return ResponseEntity.ok(profileDto);
        } else {
            // profile not exists
            ProfileDTO profileDto = new ProfileDTO(new com.raxim.myscoutee.profile.data.document.mongo.Profile());
            return ResponseEntity.ok(profileDto);
        }
    }

    @PostMapping(value = "/profile", consumes = "multipart/form-data")
    @Transactional
    public ResponseEntity<ProfileDTO> saveProfile(Authentication auth,
            @RequestPart("profile") com.raxim.myscoutee.profile.data.document.mongo.Profile profile,
            @RequestPart(value = "voice", required = false) MultipartFile voice) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        User user = principal.getUser();
        UUID profileId = user.getProfile().getId();
        UUID group = user.getGroup();

        try {
            ProfileDTO profileDto = profileService.saveProfile(user.getId(), profileId, group, profile, voice);

            if (profileDto == null) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.ok(profileDto);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/profile/rewards")
    @Transactional
    public ResponseEntity<List<RewardDTO>> getRewards(Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        User user = principal.getUser();
        UUID profileId = user.getProfile().getId();

        List<RewardDTO> rewards = linkRepository.findRewards(profileId);

        return ResponseEntity.ok(rewards);
    }

    @GetMapping("/profile/share")
    @Transactional
    public ResponseEntity<LinkDTO> shareGroup(Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        User user = principal.getUser();
        UUID profileId = user.getProfile().getId();

        Link link = new Link();
        link.setKey(UUID.randomUUID());
        link.setRefId(user.getGroup());
        link.setType("g");
        link.setCreatedBy(profileId);

        Link linkSaved = linkRepository.save(link);

        Optional<Group> group = groupRepository.findById(user.getGroup());

        if (group.isPresent()) {
            Group groupReq = group.get();
            LinkDTO linkResp = new LinkDTO(
                    linkSaved,
                    new LinkInfoDTO("Please be invited for " + groupReq.getName() + " group!", groupReq.getDesc()));
            return ResponseEntity.ok(linkResp);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

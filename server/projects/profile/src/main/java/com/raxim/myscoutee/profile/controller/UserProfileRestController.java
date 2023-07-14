package com.raxim.myscoutee.profile.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.raxim.myscoutee.common.config.firebase.FirebaseService;
import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.ControllerUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Group;
import com.raxim.myscoutee.profile.data.document.mongo.Link;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;
import com.raxim.myscoutee.profile.data.dto.rest.LinkDTO;
import com.raxim.myscoutee.profile.data.dto.rest.LinkInfoDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.data.dto.rest.RewardDTO;
import com.raxim.myscoutee.profile.data.dto.rest.UserDTO;
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
            ProfileDTO profileDto = new ProfileDTO(new Profile());
            return ResponseEntity.ok(profileDto);
        }
    }

    @PostMapping(value = "/profile", consumes = "multipart/form-data")
    @Transactional
    public ResponseEntity<ProfileDTO> saveProfile(Authentication auth,
            @RequestPart("profile") Profile profile,
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

    // Type is a group link
    @GetMapping("/profile/teams/{id}/share")
    @Transactional
    public ResponseEntity<LinkDTO> shareTeam() {
        return null;
    }

    // list groups of the current profile
    @GetMapping("/profile/teams")
    public ResponseEntity<PageDTO<GroupDTO>> getGroups(
            Authentication auth,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset) {

        String[] tOffset;
        if (offset != null && offset.length == 1) {
            tOffset = new String[] { CommonUtil.decode(offset[0]) };
        } else {
            tOffset = new String[] { "1900-01-01" };
        }

        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        User user = principal.getUser();
        UUID profileId = user.getProfile().getId();
        Optional<Group> group = groupRepository.findById(user.getGroup());
        boolean isAdmin = group.get().getType().equals("b");

        List<GroupDTO> groups = userRepository.findGroupsByEmail(
                auth.getName(), FirebaseService.ROLE_ADMIN, isAdmin, profileId, 20, step != null ? step : 5, tOffset);

        List<Object> lOffset;
        if (!groups.isEmpty()) {
            lOffset = groups.get(groups.size() - 1).getOffset();
        } else {
            lOffset = Arrays.asList(tOffset, List.class);
        }

        return ResponseEntity.ok(new PageDTO<>(groups, lOffset));
    }

    // teams list and inside teams list you can look at profiles if the group is not
    // dicreet
    @GetMapping("/profile/teams/{teamId}/profiles")
    public ResponseEntity<PageDTO<ProfileDTO>> getProfilesByGroup(
            Authentication auth,
            @PathVariable String groupId,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset) {

        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        UUID profileId = principal.getUser().getProfile().getId();

        String[] tOffset;
        if (offset != null && offset.length == 1) {
            tOffset = new String[] { CommonUtil.decode(offset[0]) };
        } else {
            tOffset = new String[] { "1900-01-01" };
        }

        List<ProfileDTO> profiles = profileRepository.findProfilesByGroup(
                profileId, CommonUtil.parseUUID(groupId), 20, step != null ? step : 5, tOffset);

        List<Object> lOffset;
        if (!profiles.isEmpty()) {
            lOffset = profiles.get(profiles.size() - 1).getOffset();
        } else {
            lOffset = Arrays.asList(tOffset, List.class);
        }

        return ResponseEntity.ok(new PageDTO<>(profiles, lOffset));
    }

    // Suspend/activate account from a group or all groups managed by a particular
    // user
    @PatchMapping("/profile/teams/{teamId}/profiles/{profileId}")
    @Transactional
    public ResponseEntity<ProfileDTO> suspendProfile(
            Authentication auth,
            @PathVariable String profileId,
            @RequestBody Profile pProfile) {

        ResponseEntity<ProfileDTO> response = ControllerUtil.handle(
                (i, a) -> profileService.saveProfile(profileId, pProfile),
                profileId, pProfile,
                HttpStatus.OK);
        return response;
    }
}

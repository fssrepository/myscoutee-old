package com.raxim.myscoutee.profile.controller;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.common.config.firebase.FirebaseService;
import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.config.properties.ConfigProperties;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.ControllerUtil;
import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Badge;
import com.raxim.myscoutee.profile.data.document.mongo.Group;
import com.raxim.myscoutee.profile.data.document.mongo.Link;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;
import com.raxim.myscoutee.profile.data.dto.rest.LinkDTO;
import com.raxim.myscoutee.profile.data.dto.rest.LinkInfoDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.data.dto.rest.UserDTO;
import com.raxim.myscoutee.profile.repository.mongo.GroupRepository;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.repository.mongo.LinkRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;
import com.raxim.myscoutee.profile.service.ProfileService;

@RepositoryRestController
@RequestMapping("user")
public class UserGroupRestController {
    private final GroupRepository groupRepository;
    private final ProfileService profileService;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final LinkRepository linkRepository;
    private final ConfigProperties config;
    private final ObjectMapper objectMapper;

    public UserGroupRestController(GroupRepository groupRepository, ProfileService profileService,
            ProfileRepository profileRepository,
            UserRepository userRepository, LikeRepository likeRepository,
            LinkRepository linkRepository, ConfigProperties config,
            ObjectMapper objectMapper) {
        this.groupRepository = groupRepository;
        this.profileService = profileService;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.linkRepository = linkRepository;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    // Suspend/activate account from a group or all groups managed by a particular
    // user
    @PatchMapping("/groups/{groupId}/profiles/{profileId}")
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

    @PostMapping("/groups/{groupId}/leave")
    public ResponseEntity<UserDTO> join(@PathVariable String groupId, Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        User user = principal.getUser();
        Profile profile = user.getProfile();

        User dbUser = userRepository.findById(user.getId()).get();

        Profile profilesToDel = dbUser.getProfiles().stream()
                .filter(p -> p.getGroup().equals(UUID.fromString(groupId))
                        && (p.getStatus().equals("A") || p.getStatus().equals("F") || p.getStatus().equals("I")))
                .findFirst().orElse(null);
        profilesToDel.setStatus("D");
        profileRepository.save(profilesToDel);

        Profile newProfile = dbUser.getProfiles().stream()
                .filter(p -> !p.getGroup().equals(UUID.fromString(groupId))
                        && (p.getStatus().equals("A") || p.getStatus().equals("F") || p.getStatus().equals("I")))
                .findFirst().orElse(null);
        dbUser.setProfile(newProfile);
        dbUser.setGroup(dbUser.getProfile().getGroup());

        User userSaved = userRepository.save(dbUser);

        boolean adminUser = config.getAdminUser().equals(auth.getName());
        List<GroupDTO> groups = userRepository.findAllGroupsByEmail(auth.getName()).stream()
                .filter(group -> group.getRole().equals("ROLE_USER")
                        || (adminUser && group.getGroup().getType().equals("b")))
                .collect(Collectors.toList());

        List<Badge> likes = likeRepository.newLikesByProfile(profile.getId(),
                profile.getLastLogin().format(DateTimeFormatter.ISO_DATE_TIME));

        return ResponseEntity.ok(new UserDTO(userSaved, groups, likes));
    }

    // List groups with system groups if Role_Admin has in admin group
    @GetMapping("/groups")
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

    @GetMapping("/groups/{groupId}/profiles")
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

    @PostMapping("/groups")
    public ResponseEntity<Group> saveGroup(
            Authentication auth,
            @RequestBody Group group) {
        Group groupSaved = groupRepository.save(group);

        Profile profile = new Profile();
        profile.setGroup(groupSaved.getId());
        profile.setRole("A");
        Profile profileSaved = profileRepository.save(profile);

        User user = userRepository.findUserByEmail(auth.getName());
        Profile authProfile = user.getProfile();

        Group clonedGroup = JsonUtil.clone(groupSaved, objectMapper);
        clonedGroup.setCreatedBy(authProfile.getId());
        clonedGroup.setPosition(authProfile.getPosition());

        Group groupProfileSaved = groupRepository.save(clonedGroup);

        user.getProfiles().add(profileSaved);
        userRepository.save(user);

        return ResponseEntity.ok(groupProfileSaved);
    }

    @PatchMapping("/groups/{id}")
    @Transactional
    public ResponseEntity<Group> patchGroup(
            Authentication auth,
            @PathVariable String id,
            @RequestBody Group group) {

        Optional<Group> groupSaved = groupRepository.findById(UUID.fromString(id)).map(groupOld -> {
            Group groupToSave = JsonUtil.clone(group, objectMapper);
            groupToSave.setId(groupOld.getId());
            groupToSave.setCreatedDate(groupOld.getCreatedDate());
            groupToSave.setCreatedBy(groupOld.getCreatedBy());
            return groupRepository.save(groupToSave);
        });

        if (groupSaved.isPresent()) {
            return ResponseEntity.ok(groupSaved.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Type is a group link
    @GetMapping("/groups/{id}/share")
    @Transactional
    public ResponseEntity<LinkDTO> shareGroup(
            Authentication auth,
            @PathVariable String id) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        UUID profileId = principal.getUser().getProfile().getId();

        Link link = new Link();
        link.setKey(UUID.randomUUID());
        link.setRefId(UUID.fromString(id));
        link.setType("g");
        link.setCreatedBy(profileId);
        Link linkSaved = linkRepository.save(link);

        Optional<Group> group = groupRepository.findById(UUID.fromString(id));

        if (group.isPresent()) {
            Group groupReq = group.get();
            LinkInfoDTO linkInfo = new LinkInfoDTO("Please be invited for " + groupReq.getName() + " group!",
                    groupReq.getDesc());
            LinkDTO linkResp = new LinkDTO(linkSaved, linkInfo);
            return ResponseEntity.ok(linkResp);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}

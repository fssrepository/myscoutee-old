package com.raxim.myscoutee.profile.controller;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.rest.webmvc.RepositoryRestController;
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
import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Badge;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.EventItem;
import com.raxim.myscoutee.profile.data.document.mongo.Group;
import com.raxim.myscoutee.profile.data.document.mongo.Link;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Role;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.data.dto.rest.ErrorDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventItemDTO;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;
import com.raxim.myscoutee.profile.data.dto.rest.LinkDTO;
import com.raxim.myscoutee.profile.data.dto.rest.LinkInfoDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileStatusDTO;
import com.raxim.myscoutee.profile.data.dto.rest.UserDTO;
import com.raxim.myscoutee.profile.service.EventService;
import com.raxim.myscoutee.profile.service.ProfileService;
import com.raxim.myscoutee.profile.util.EventItemUtil;

@RepositoryRestController
@RequestMapping("user")
public class UserGroupRestController {
    private final GroupRepository groupRepository;
    private final EventService eventService;
    private final EventRepository eventRepository;
    private final ProfileService profileService;
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final LinkRepository linkRepository;
    private final ConfigProperties config;
    private final ObjectMapper objectMapper;

    public UserGroupRestController(GroupRepository groupRepository, EventService eventService,
            EventRepository eventRepository, ProfileService profileService,
            ProfileRepository profileRepository, RoleRepository roleRepository,
            UserRepository userRepository, LikeRepository likeRepository,
            LinkRepository linkRepository, ConfigProperties config,
            ObjectMapper objectMapper) {
        this.groupRepository = groupRepository;
        this.eventService = eventService;
        this.eventRepository = eventRepository;
        this.profileService = profileService;
        this.profileRepository = profileRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.linkRepository = linkRepository;
        this.config = config;
    }

    @GetMapping("/groups/{groupId}/events")
    @Transactional
    public ResponseEntity<?> getEvents(@PathVariable String groupId, @RequestParam(name = "step") String step,
            @RequestParam(name = "offset") String[] offset, Authentication auth) {
        String[] tOffset;
        if (offset != null && offset.length == 1) {
            tOffset = new String[] { CommonUtil.decode(offset[0]) };
        } else {
            tOffset = new String[] { "1900-01-01" };
        }

        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();

        if (profile.getPosition() != null) {
            List<EventDTO> events = eventService.getEventsByStatus(tOffset, UUID.fromString(groupId), "U");

            List<Object> lOffset;
            if (!events.isEmpty()) {
                lOffset = events.get(events.size() - 1).getOffset();
            } else {
                lOffset = Arrays.asList(tOffset, List.class);
            }

            PageDTO<EventDTO> pageDto = new PageDTO<>(events, lOffset);
            pageDto.setScroll(0);
            return ResponseEntity.ok(pageDto);
        } else {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
        }
    }

    @PatchMapping("/groups/{groupId}/events/{id}")
    @Transactional
    public ResponseEntity<?> patchEvent(@PathVariable String id, @RequestBody EventItem eventItem,
            Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();
        return EventItemUtil.update(eventService, eventItem, id, profile);
    }

    @PostMapping("/groups/{groupId}/events/{id}/items")
    public ResponseEntity<EventItemDTO> addItem(@PathVariable String id, @RequestBody EventItem eventItem,
            Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();
        return EventItemUtil.save(eventService, eventItem, id, profile);
    }

    @PatchMapping("/groups/{groupId}/events/{id}/items/{itemId}")
    public ResponseEntity<EventItemDTO> patchItem(@PathVariable String id, @PathVariable String itemId,
            @RequestBody EventItem eventItem, Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();
        return EventItemUtil.update(eventService, eventItem, id, itemId, profile);
    }

    @GetMapping("/groups/{groupId}/events/{id}/items")
    public ResponseEntity<PageDTO<EventItemDTO>> items(@PathVariable String id, @RequestParam("step") Integer step,
            @RequestParam("offset") String[] offset, Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();

        String[] tOffset;
        if (offset != null && offset.length == 2) {
            tOffset = new String[] { CommonUtil.decode(offset[0]), CommonUtil.decode(offset[1]) };
        } else {
            tOffset = new String[] { "1900-01-01", "1900-01-01" };
        }

        List<EventItemDTO> eventItems = eventService.getEventItems(UUID.fromString(id), step, tOffset, profile.getId());

        List<Object> lOffset;
        if (!eventItems.isEmpty()) {
            EventItemDTO lastEventItem = eventItems.get(eventItems.size() - 1);
            lOffset = lastEventItem.getOffset();
        } else {
            lOffset = Arrays.asList(tOffset, List.class);
        }

        return ResponseEntity.ok(new PageDTO<>(eventItems, lOffset));
    }

    @PostMapping("/groups/{id}/events/{eventId}/publish")
    public ResponseEntity<EventDTO> publish(@PathVariable String eventId, Authentication auth) {
        Event event = eventRepository.findById(UUID.fromString(eventId)).get();
        event.setStatus("R");
        event = eventRepository.save(event);

        EventDTO resEvent = new EventDTO(
                event);
        return ResponseEntity.ok(resEvent);
    }

    @PostMapping("/groups/{id}/events/{eventId}/reject")
    public ResponseEntity<EventDTO> reject(@PathVariable String eventId, Authentication auth) {
        Event event = eventRepository.findById(UUID.fromString(eventId)).get();
        event.setStatus("D");
        event = eventRepository.save(event);

        EventDTO resEvent = new EventDTO(
                event);
        return ResponseEntity.ok(resEvent);
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

    // Suspend/activate account from a group or all groups managed by a particular
    // user
    @PatchMapping("/groups/{groupId}/profiles/{profileId}")
    @Transactional
    public ResponseEntity<Void> suspendProfile(
            Authentication auth,
            @PathVariable String groupId,
            @PathVariable String profileId,
            @RequestBody ProfileStatusDTO profileStatus) {

        ProfileDTO profile = profileService.saveProfileStatus(profileId, profileStatus);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok().build();
        }
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
        Profile profileSaved = profileRepository.save(profile);

        User user = userRepository.findUserByEmail(auth.getName());
        Profile authProfile = user.getProfile();

        Group clonedGroup = JsonUtil.clone(groupSaved, objectMapper);
        clonedGroup.setCreatedBy(authProfile.getId());
        clonedGroup.setPosition(authProfile.getPosition());

        Group groupProfileSaved = groupRepository.save(clonedGroup);

        user.getProfiles().add(profileSaved);
        userRepository.save(user);

        Role roleToSave = new Role();
        roleToSave.setId(UUID.randomUUID());
        roleToSave.setProfileId(profileSaved.getId());
        roleToSave.setRole(null);
        UUID.randomUUID(), profileSaved.getId(), FirebaseService.ROLE_ADMIN);
        roleRepository.save(roleToSave);

        return ResponseEntity.ok(groupProfileSaved);
    }

    @PatchMapping("/groups/{id}")
    @Transactional
    public ResponseEntity<Group> patchGroup(
            Authentication auth,
            @PathVariable String id,
            @RequestBody Group group) {

        Optional<Group> groupSaved = groupRepository.findById(UUID.fromString(id)).map(groupOld -> {
            Group groupToSave = group.copy(groupOld.getId(), groupOld.getCreatedDate(), groupOld.getCreatedBy());
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

package com.raxim.myscoutee.profile.controller;

import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Group;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Role;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.data.dto.rest.ErrorDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventItemDTO;
import com.raxim.myscoutee.profile.data.dto.rest.FeedbackDTO;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.GroupRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.RoleRepository;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;
import com.raxim.myscoutee.profile.service.EventService;
import com.raxim.myscoutee.profile.util.EventUtil;

@RepositoryRestController
@RequestMapping("recommendations")
public class RecommendationRestController {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final EventService eventService;
    private final EventRepository eventRepository;
    private final GroupRepository groupRepository;
    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper;

    public RecommendationRestController(
            ProfileRepository profileRepository,
            UserRepository userRepository,
            EventService eventService,
            EventRepository eventRepository,
            GroupRepository groupRepository,
            RoleRepository roleRepository,
            ObjectMapper objectMapper) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.eventService = eventService;
        this.eventRepository = eventRepository;
        this.groupRepository = groupRepository;
        this.roleRepository = roleRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping("groups")
    @Transactional
    public ResponseEntity<Object> groups(
            @RequestParam("step") Integer step,
            @RequestParam("offset") String[] offset,
            Authentication auth) {
        Object[] tOffset;
        if (offset != null && offset.length == 5) {
            tOffset = new Double[] {
                    Double.parseDouble(offset[0]),
                    Double.parseDouble(offset[1]),
                    Double.parseDouble(offset[2])
            };
        } else {
            tOffset = new Object[] { 0.0, "1900-01-01", 0.0 };
        }

        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        User user = firebasePrincipal.getUser();
        Profile profile = user.getProfile();

        if (profile.getPosition() != null) {
            Group group = groupRepository.findById(user.getGroup()).get();

            List<UUID> groupUUIDs = user.getProfiles()
                    .stream()
                    .filter(p -> !p.getStatus().equals("D"))
                    .map(Profile::getGroup)
                    .collect(Collectors.toList());

            List<GroupDTO> groups = groupRepository.findGroupByProfile(
                    group.getType(),
                    CommonUtil.point(profile.getPosition()),
                    20,
                    step != null ? step : 5,
                    groupUUIDs,
                    tOffset);

            // http://dolszewski.com/spring/how-to-bind-requestparam-to-object/

            List<Object> lOffset;
            if (!groups.isEmpty()) {
                lOffset = groups.get(groups.size() - 1).getOffset();
            } else {
                lOffset = Arrays.asList(tOffset);
            }

            return ResponseEntity.ok(
                    new PageDTO<>(groups, lOffset, 0));
        } else {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
        }
    }

    @PostMapping("groups/{groupId}/join")
    public ResponseEntity<Group> join(
            @PathVariable String groupId,
            Authentication auth) {
        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        User user = firebasePrincipal.getUser();

        boolean isProfile = user.getProfiles()
                .stream()
                .anyMatch(profile -> profile.getGroup().equals(UUID.fromString(groupId))
                        && !profile.getStatus().equals("D"));

        if (!isProfile) {
            Profile profileToSave = JsonUtil.clone(user.getProfile(), objectMapper);
            profileToSave.setId(UUID.randomUUID());
            profileToSave.setGroup(UUID.fromString(groupId));
            profileToSave.setStatus("P");
            Profile profileSaved = profileRepository.save(profileToSave);

            Role role = new Role();
            role.setId(UUID.randomUUID());
            role.setProfileId(profileSaved.getId());
            role.setRole("ROLE_USER");
            roleRepository.save(role);

            User userToSave = userRepository.findById(user.getId()).get();
            userToSave.getProfiles().add(profileSaved);
            userRepository.save(userToSave);
        }

        Optional<Group> group = groupRepository.findById(UUID.fromString(groupId));

        return group.map(value -> ResponseEntity.ok(value)).orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("events")
    @Transactional
    public ResponseEntity<Object> events(
            @RequestParam("step") Integer step,
            @RequestParam("offset") String[] offset,
            Authentication auth) {
        Object[] tOffset;
        if (offset != null && offset.length == 2) {
            tOffset = new Double[] {
                    Double.parseDouble(offset[0]),
                    Double.parseDouble(offset[1])
            };
        } else {
            tOffset = new Object[] { 0.0, "1900-01-01" };
        }

        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        if (profile.getPosition() != null) {
            List<EventDTO> events = eventRepository.findEventsByProfile(
                    profile.getId(),
                    CommonUtil.point(profile.getPosition()),
                    20,
                    step != null ? step : 5,
                    profile.getGroup(),
                    tOffset);

            // http://dolszewski.com/spring/how-to-bind-requestparam-to-object/

            List<Object> lOffset;
            if (!events.isEmpty()) {
                lOffset = events.get(events.size() - 1).getOffset();
            } else {
                lOffset = Arrays.asList(tOffset);
            }

            return ResponseEntity.ok(
                    new PageDTO<>(events, lOffset, 0));
        } else {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
        }
    }

    //TODO: to be fixed
    /*@PostMapping("events/{id}/clone")
    public ResponseEntity<EventDTO> cloneEvent(
            @PathVariable String id,
            @RequestParam("step") Integer step,
            @RequestParam("offset") String[] offset,
            Authentication auth) {
        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        Optional<Event> eventDto = eventService.cloneEvent(UUID.fromString(id), profile);
        if (eventDto.isPresent()) {
            EventDTO event = EventUtil.transform(eventDto.get());
            return ResponseEntity.ok(event);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }*/

    // People who you haven't met, but your surroundings are ordered by rate
    @Deprecated
    @GetMapping("profiles")
    @Transactional
    public ResponseEntity<Object> getInvitations(
            @RequestParam("step") Integer step,
            @RequestParam("offset") String[] offset,
            Authentication auth) {
        Object[] tOffset;
        if (offset != null && offset.length == 5) {
            tOffset = new Object[] {
                    Double.parseDouble(offset[0]),
                    Double.parseDouble(offset[1]),
                    Double.parseDouble(offset[2]),
                    Double.parseDouble(offset[3]),
                    offset[4]
            };
        } else {
            tOffset = new Object[] { 0.0, 0.0, 0.0, 0.0,
                    LocalDate.now().atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME) };
        }

        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        if (profile.getPosition() != null) {
            List<ProfileDTO> profiles = profileRepository.findPeopleByProfile(
                    profile.getId(),
                    CommonUtil.point(profile.getPosition()),
                    20,
                    step != null ? step : 5,
                    profile.getGender(),
                    profile.getGroup(),
                    tOffset,
                    1.5);

            // http://dolszewski.com/spring/how-to-bind-requestparam-to-object/

            List<Object> lOffset;
            if (!profiles.isEmpty()) {
                lOffset = profiles.get(profiles.size() - 1).getOffset();
            } else {
                lOffset = Arrays.asList(tOffset);
            }

            return ResponseEntity.ok(
                    new PageDTO<>(profiles, lOffset, 0));
        } else {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
        }
    }

    @GetMapping(value = { "events/{id}/items", "invitations/{id}/items" })
    public ResponseEntity<PageDTO<EventItemDTO>> items(
            @PathVariable String id,
            @RequestParam("step") Integer step,
            @RequestParam("offset") String[] offset,
            Authentication auth) {
        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        String[] tOffset;
        if (offset != null && offset.length == 2) {
            tOffset = new String[] { offset[0], offset[1] };
        } else {
            tOffset = new String[] { "1900-01-01", "1900-01-01" };
        }

        List<EventItemDTO> eventItems = eventRepository.findItemsByEvent(
                UUID.fromString(id),
                20,
                step != null ? step : 5,
                "%Y-%m-%d",
                profile.getId(),
                tOffset);

        // http://dolszewski.com/spring/how-to-bind-requestparam-to-object/

        List<Object> lOffset;
        if (!eventItems.isEmpty()) {
            lOffset = eventItems.get(eventItems.size() - 1).getOffset();
        } else {
            lOffset = Arrays.asList(tOffset);
        }

        return ResponseEntity.ok(
                new PageDTO<>(eventItems, lOffset));
    }

    @GetMapping(value = { "events/{id}/feedbacks", "invitations/{id}/feedbacks" })
    public ResponseEntity<PageDTO<FeedbackDTO>> feedbacks(
            @PathVariable String id,
            @RequestParam("step") Integer step,
            @RequestParam("offset") String[] offset) {
        String[] tOffset;
        if (offset != null && offset.length == 1) {
            tOffset = new String[] { offset[0] };
        } else {
            tOffset = new String[] { "1900-01-01" };
        }

        List<FeedbackDTO> feedbacks = eventRepository.findFeedbacksByEvent(
                UUID.fromString(id),
                20,
                step != null ? step : 5,
                tOffset);

        // http://dolszewski.com/spring/how-to-bind-requestparam-to-object/

        List<Object> lOffset;
        if (!feedbacks.isEmpty()) {
            lOffset = feedbacks.get(feedbacks.size() - 1).getOffset();
        } else {
            lOffset = Arrays.asList(tOffset);
        }

        return ResponseEntity.ok(
                new PageDTO<>(feedbacks, lOffset));
    }
}

package com.raxim.myscoutee.profile.controller;

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
import com.raxim.myscoutee.profile.data.document.mongo.Group;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Role;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.data.dto.rest.ErrorDTO;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.repository.mongo.GroupRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.RoleRepository;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;

@RepositoryRestController
@RequestMapping("recommendations")
public class RecommendationRestController {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper;

    public RecommendationRestController(
            ProfileRepository profileRepository,
            UserRepository userRepository,
            GroupRepository groupRepository,
            RoleRepository roleRepository,
            ObjectMapper objectMapper) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
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

    // TODO: promotion fix
    /*
     * @PostMapping("events/{id}/clone")
     * public ResponseEntity<EventDTO> cloneEvent(
     * 
     * @PathVariable String id,
     * 
     * @RequestParam("step") Integer step,
     * 
     * @RequestParam("offset") String[] offset,
     * Authentication auth) {
     * Profile profile = ((FirebasePrincipal)
     * auth.getPrincipal()).getUser().getProfile();
     * 
     * Optional<Event> eventDto = eventService.cloneEvent(UUID.fromString(id),
     * profile);
     * if (eventDto.isPresent()) {
     * EventDTO event = EventUtil.transform(eventDto.get());
     * return ResponseEntity.ok(event);
     * } else {
     * return ResponseEntity.badRequest().build();
     * }
     * }
     */

    // TODO: promotion fix
    /*
     * @GetMapping("/promotions")
     * 
     * @Transactional
     * public ResponseEntity<Object> getPromotions(
     * 
     * @RequestParam(value = "step", required = false) Integer step,
     * 
     * @RequestParam(value = "offset", required = false) String[] offset,
     * Authentication auth) {
     * // Convert offset array to List<Object>
     * List<Object> lOffset;
     * if (offset != null && offset.length == 5) {
     * lOffset = Arrays.asList(
     * Double.parseDouble(offset[0]),
     * Double.parseDouble(offset[1]),
     * Double.parseDouble(offset[2]),
     * Double.parseDouble(offset[3]),
     * CommonUtil.decode(offset[4]));
     * } else {
     * lOffset = Arrays.asList(
     * 0.0,
     * 0.0,
     * 0.0,
     * 0.0,
     * LocalDate.now().atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME));
     * }
     * 
     * FirebasePrincipal firebasePrincipal = (FirebasePrincipal)
     * auth.getPrincipal();
     * Profile profile = firebasePrincipal.getUser().getProfile();
     * 
     * if (profile.getPosition() != null) {
     * List<PromotionDTO> promotions = promotionRepository.findPromotionsByRec(
     * profile.getId(),
     * CommonUtil.point(profile.getPosition()),
     * 20,
     * step != null ? step : 5,
     * "j",
     * profile.getGroup(),
     * lOffset.toArray());
     * 
     * List<Object> lastOffset;
     * if (!promotions.isEmpty()) {
     * lastOffset = promotions.get(promotions.size() - 1).getOffset();
     * } else {
     * lastOffset = lOffset;
     * }
     * 
     * return ResponseEntity.ok(new PageDTO<>(promotions, lastOffset, 0));
     * } else {
     * return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
     * }
     * }
     */

    // TODO: promotion fix
    /*
     * @PostMapping("/promotions/{promoId}/events/{id}/clone")
     * public ResponseEntity<?> cloneEvent(
     * 
     * @PathVariable String promoId,
     * 
     * @PathVariable String id,
     * 
     * @RequestParam(value = "step", required = false) Integer step,
     * 
     * @RequestParam(value = "offset", required = false) String[] offset,
     * Authentication auth) {
     * FirebasePrincipal firebasePrincipal = (FirebasePrincipal)
     * auth.getPrincipal();
     * Profile profile = firebasePrincipal.getUser().getProfile();
     * 
     * List<Event> events = eventRepository.findPendingEvents(UUID.fromString(id),
     * profile.getId());
     * Event promoter = events.stream()
     * .filter(event -> event.getInfo().getMembers().stream()
     * .anyMatch(member -> member.getProfile().getId().equals(profile.getId())))
     * .findFirst()
     * .orElse(null);
     * 
     * if (promoter == null) {
     * Optional<Event> event = eventService.cloneEvent(UUID.fromString(id),
     * profile);
     * if (event.isPresent()) {
     * EventDTO tEvent = EventUtil.transform(event.get());
     * return ResponseEntity.ok(tEvent);
     * } else {
     * return ResponseEntity.badRequest().build();
     * }
     * } else {
     * return ResponseEntity.badRequest().body(new ErrorDTO(450,
     * "err.clone_not_allowed"));
     * }
     * }
     */
}

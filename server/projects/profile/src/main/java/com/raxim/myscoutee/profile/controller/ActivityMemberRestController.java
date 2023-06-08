package com.raxim.myscoutee.profile.controller;

import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.EventItem;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.*;
import com.raxim.myscoutee.profile.repository.mongo.*;
import com.raxim.myscoutee.profile.service.ProfileService;
import com.raxim.myscoutee.profile.service.StatusService;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RepositoryRestController
@RequestMapping("activity")
public class ActivityMemberRestController {
    private final EventRepository eventRepository;
    private final EventItemRepository eventItemRepository;
    private final ProfileService profileService;
    private final ProfileRepository profileRepository;
    private final StatusService statusService;

    public ActivityMemberRestController(EventRepository eventRepository,
            EventItemRepository eventItemRepository,
            ProfileService profileService, 
            ProfileRepository profileRepository,
            StatusService statusService) {
        this.eventRepository = eventRepository;
        this.eventItemRepository = eventItemRepository;
        this.profileService = profileService;
        this.profileRepository = profileRepository;
        this.statusService = statusService;
    }

    @PostMapping("events/{id}/items/{itemId}/join")
    public ResponseEntity<?> joinItem(@PathVariable String id, @PathVariable String itemId,
            @RequestBody EventItem eventItem, Authentication auth) {
        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();
        return statusService.itemStatus(itemId, "A", profile.getId());
    }

    @PostMapping("events/{id}/items/{itemId}/wait")
    public ResponseEntity<?> waitItem(@PathVariable String id, @PathVariable String itemId,
            @RequestBody EventItem eventItem, Authentication auth) {
        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();
        return statusService.itemStatus(itemId, "W", profile.getId());
    }

    @PostMapping("events/{id}/items/{itemId}/leave")
    public ResponseEntity<?> leaveItem(@PathVariable String id, @PathVariable String itemId,
            @RequestBody EventItem eventItem, Authentication auth) {
        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();
        return statusService.itemStatus(itemId, "L", profile.getId());
    }

    @GetMapping(value = { "events/{eventId}/items/{id}/members" })
    public ResponseEntity<PageDTO<MemberDTO>> itemMembers(@PathVariable String id, @RequestParam("step") Integer step,
            @RequestParam("offset") String[] offset) {
        String[] tOffset = (offset != null && offset.length == 1)
                ? new String[] { CommonUtil.decode(offset[0]), CommonUtil.decode(offset[0]) }
                : new String[] { "A", "1900-01-01" };

        List<MemberDTO> members = eventItemRepository.findMembersByItem(UUID.fromString(id), 20,
                step != null ? step : 5, new String[] { "A", "I", "J" }, tOffset);

        List<Object> lOffset = members.isEmpty() ? Arrays.asList(tOffset) : members.get(members.size() - 1).getOffset();

        return ResponseEntity.ok(new PageDTO<>(members, lOffset));
    }

    @GetMapping(value = { "events/{id}/members", "invitations/{id}/members" })
    public ResponseEntity<PageDTO<MemberDTO>> members(@PathVariable String id, @RequestParam("step") Integer step,
            @RequestParam("offset") String[] offset, Authentication auth) {
        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();

        String[] tOffset = (offset != null && offset.length == 1)
                ? new String[] { CommonUtil.decode(offset[0]), CommonUtil.decode(offset[0]) }
                : new String[] { "A", "1900-01-01" };

        List<MemberDTO> members = eventRepository.findMembersByEvent(UUID.fromString(id), 20,
                step != null ? step : 5, profile.getId(), new String[] { "A", "I", "J" }, tOffset);

        List<Object> lOffset = members.isEmpty() ? Arrays.asList(tOffset) : members.get(members.size() - 1).getOffset();

        Object lRole = null;
        if(!members.isEmpty()) {
            lRole = Arrays.asList(tOffset);
        } else {
            lRole = members.get(members.size() - 1).getRole();
        }

        return ResponseEntity.ok(new PageDTO<>(members, lOffset, 1, null, lRole));
    }

    @GetMapping(value = { "events/{eventId}/members/{id}/schools" })
    public ResponseEntity<PageDTO<SchoolDTO>> getSchools(@PathVariable String id, Authentication auth,
            @RequestParam("step") Integer step,
            @RequestParam("offset") String[] offset) {
        String[] tOffset = (offset != null && offset.length == 3)
                ? new String[] { CommonUtil.decode(offset[0]), CommonUtil.decode(offset[1]), CommonUtil.decode(offset[2]) }
                : new String[] { "a", "1900-01-01", "1900-01-01" };

        List<SchoolDTO> schools = profileService.getSchools(UUID.fromString(id), step, tOffset);

        List<Object> lOffset = schools.isEmpty() ? Arrays.asList(tOffset) : schools.get(schools.size() - 1).getOffset();

        return ResponseEntity.ok(new PageDTO<>(schools, lOffset));
    }

    @GetMapping("events/{id}/code")
    public ResponseEntity<CodeDTO> code(@PathVariable String id, Authentication auth) {
        UUID eventUUId = UUID.fromString(id);
        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();

        Optional<CodeDTO> code = eventRepository.findCodeByEvent(eventUUId, profile.getId());

        return code.map(c -> ResponseEntity.ok(c)).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("events/{id}/verify")
    public ResponseEntity<MemberDTO> verify(@PathVariable String id, @RequestBody String code) {
        Optional<MemberDTO> member = eventRepository.findMemberByCode(UUID.fromString(id), code);
        return member.map(m -> ResponseEntity.ok(m)).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("events/{id}/leave")
    public ResponseEntity<?> leave(@PathVariable String id, Authentication auth) {
        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();
        return statusService.status(id, "L", profile.getId());
    }

    @PostMapping(value = { "invitations/{id}/accept", "promotions/{id}/accept" })
    public ResponseEntity<?> join(@PathVariable String id, Authentication auth) {
        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();
        return statusService.status(id, "A", profile.getId());
    }

    @PostMapping(value = { "invitations/{id}/wait" })
    public ResponseEntity<?> wait(@PathVariable String id, Authentication auth) {
        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();
        return statusService.status(id, "W", profile.getId());
    }

    @PostMapping(value = { "invitations/{id}/reject", "promotions/{id}/reject" })
    public ResponseEntity<?> reject(@PathVariable String id, Authentication auth) {
        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();
        return statusService.status(id, "R", profile.getId());
    }

    @PostMapping("events/{id}/members")
    @Transactional
    public ResponseEntity<List<MemberDTO>> addMember(@PathVariable String id, @RequestBody List<String> profiles) {
        return eventRepository.findById(UUID.fromString(id)).map(event -> {
            List<UUID> uProfiles = profiles.stream().map(UUID::fromString).collect(Collectors.toList());
            List<Profile> pProfiles = profileRepository.findAllById(uProfiles);

            Set<Member> tMembers = pProfiles.stream().map(profile -> {
                Member member = event.getInfo().getMembers().stream()
                        .filter(m -> m.getId().equals(profile.getId()))
                        .findFirst().orElse(null);

                if (member == null) {
                    String code = null;
                    if (event.getInfo().isTicket()) {
                        code = UUID.randomUUID().toString();
                    }

                    member = new Member(profile.getId(), profile, "I", new Date(), code, "U");
                } else {
                    member.setStatus("I");
                }
                return member;
            }).collect(Collectors.toSet());

            tMembers.addAll(event.getInfo().getMembers());
            event.getInfo().setMembers(tMembers);
            eventRepository.save(event);

            List<Member> membersDto = tMembers.stream().map(member -> {
                return new Member(member, Arrays.asList(new Date(), member.getStatus()));
            }).collect(Collectors.toList());

            return ResponseEntity.status(HttpStatus.CREATED).body(membersDto);
        }).orElse(ResponseEntity.notFound().build());
    }
}

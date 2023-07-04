package com.raxim.myscoutee.profile.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Group;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.ErrorDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventItemDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.handler.EventParamHandler;
import com.raxim.myscoutee.profile.handler.ParamHandlers;
import com.raxim.myscoutee.profile.repository.mongo.EventItemRepository;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.GroupRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.PromotionRepository;
import com.raxim.myscoutee.profile.repository.mongo.SettingRepository;
import com.raxim.myscoutee.profile.service.EventService;
import com.raxim.myscoutee.profile.util.EventUtil;

@RepositoryRestController
@RequestMapping("activity")
public class ActivityRestController {

    private final ProfileRepository profileRepository;
    private final EventRepository eventRepository;
    private final EventItemRepository eventItemRepository;
    private final SettingRepository settingRepository;
    private final EventService eventService;
    private final PromotionRepository promotionRepository;
    private final GroupRepository groupRepository;
    private final ParamHandlers paramHandlers;

    public ActivityRestController(ProfileRepository profileRepository, EventRepository eventRepository,
            EventItemRepository eventItemRepository,
            SettingRepository settingRepository, EventService eventService, PromotionRepository promotionRepository,
            GroupRepository groupRepository, ParamHandlers paramHandlers) {
        this.profileRepository = profileRepository;
        this.eventRepository = eventRepository;
        this.eventItemRepository = eventItemRepository;
        this.settingRepository = settingRepository;
        this.eventService = eventService;
        this.promotionRepository = promotionRepository;
        this.groupRepository = groupRepository;
        this.paramHandlers = paramHandlers;
    }

    @GetMapping("events")
    @Transactional
    public ResponseEntity<Object> getEvents(PageParam pageParam, Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();

        //override page param
        pageParam = paramHandlers.handle(profile, pageParam, EventParamHandler.EVENT);

        if (profile.getPosition() != null) {
            List<EventDTO> events = eventService.getEvents(pageParam, profile.getId(),
                    new String[] { "A", "P", "C" });

            List<Object> lOffset = !events.isEmpty() ? events.get(events.size() - 1).getOffset() : List.of();

            return ResponseEntity.ok(
                    new PageDTO<>(events, lOffset, 0, pageParam.getStep()));
        } else {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
        }
    }

    // TODO: to be fixed
    /*
     * @PostMapping("events")
     * 
     * @Transactional
     * public ResponseEntity<EventDTO> createEvent(@RequestBody EventItem eventItem,
     * Authentication auth) {
     * FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
     * Profile profile = principal.getUser().getProfile();
     * return EventItemUtil.save(eventService, eventItem, profile, false);
     * }
     */

    // TODO: to be fixed
    /*
     * @PatchMapping("events/{id}")
     * 
     * @Transactional
     * public ResponseEntity<?> patchEvent(@PathVariable String id, @RequestBody
     * EventItem eventItem,
     * Authentication auth) {
     * FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
     * Profile profile = principal.getUser().getProfile();
     * return EventItemUtil.update(eventService, eventItem, id, profile);
     * }
     */

    // TODO: to be fixed
    /*
     * @PostMapping("events/{id}/items")
     * public ResponseEntity<EventItemDTO> addItem(@PathVariable String
     * id, @RequestBody EventItem eventItem,
     * Authentication auth) {
     * FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
     * Profile profile = principal.getUser().getProfile();
     * return EventItemUtil.save(eventService, eventItem, id, profile);
     * }
     */

    // TODO: to be fixed
    /*
     * @PatchMapping("events/{id}/items/{itemId}")
     * public ResponseEntity<EventItemDTO> patchItem(@PathVariable String
     * id, @PathVariable String itemId,
     * 
     * @RequestBody EventItem eventItem, Authentication auth) {
     * FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
     * Profile profile = principal.getUser().getProfile();
     * return EventItemUtil.update(eventService, eventItem, id, itemId, profile);
     * }
     */

    // delete date align
    // TODO: to be fixed
    /*
     * @DeleteMapping("events/{id}/items/{itemId}")
     * public ResponseEntity<Object> deleteItem(@PathVariable String
     * id, @PathVariable String itemId) {
     * Event event = eventRepository.findById(UUID.fromString(id)).get();
     * if (event.getInfo().getId().equals(UUID.fromString(itemId))) {
     * return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.first_item"));
     * }
     * 
     * EventItem item = eventItemRepository.findById(UUID.fromString(itemId)).get();
     * item.setStatus("D");
     * eventItemRepository.save(item);
     * 
     * return ResponseEntity.noContent().build();
     * }
     */

    @GetMapping(value = { "events/{id}/items", "invitations/{id}/items", "promotions/{id}/items" })
    public ResponseEntity<PageDTO<EventItemDTO>> items(@PathVariable String id,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset, Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();

        String[] tOffset = offset != null && offset.length == 2
                ? new String[] { CommonUtil.decode(offset[0]), CommonUtil.decode(offset[1]) }
                : new String[] { "1900-01-01", "1900-01-01" };

        List<EventItemDTO> eventItems = eventService.getEventItems(UUID.fromString(id), step, tOffset, profile.getId());

        List<Object> lOffset = !eventItems.isEmpty() ? eventItems.get(eventItems.size() - 1).getOffset() : List.of();

        return ResponseEntity.ok(new PageDTO<>(eventItems, lOffset));
    }

    // TODO: to be fixed
    /*
     * @PostMapping("events/{id}/recommend")
     * public ResponseEntity<EventDTO> recommend(@PathVariable String id,
     * 
     * @RequestParam(value = "step", required = false) Integer step) {
     * Optional<EventDTO> eventDto =
     * eventService.recommendEvent(UUID.fromString(id));
     * 
     * return eventDto.map(event -> ResponseEntity.ok(event))
     * .orElse(ResponseEntity.badRequest().build());
     * }
     */

    @GetMapping("invitations")
    @Transactional
    public ResponseEntity<?> getInvitations(@RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset, Authentication auth) {

        String[] tOffset = offset != null && offset.length == 5 ? new String[] {
                CommonUtil.decode(offset[0]),
                CommonUtil.decode(offset[1]),
                CommonUtil.decode(offset[2]),
                CommonUtil.decode(offset[3]),
                CommonUtil.decode(offset[4])
        }
                : new String[] { "0.0", "0.0", "0.0", "0.0",
                        LocalDate.now().atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME) };

        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();

        if (profile.getPosition() != null) {
            List<EventDTO> events = profileRepository.findInvitationByProfile(profile.getId(),
                    CommonUtil.point(profile.getPosition()), 20,
                    step != null ? step : 5, profile.getGroup(), tOffset, 1.5);

            List<Object> lOffset = !events.isEmpty() ? events.get(events.size() - 1).getOffset() : List.of();

            return ResponseEntity.ok(new PageDTO<>(events, lOffset, 0));
        } else {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
        }
    }

    @GetMapping("promotions")
    @Transactional
    public ResponseEntity<?> getPromotions(@RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "direction", required = false) Integer direction,
            @RequestParam(value = "offset", required = false) String[] offset, Authentication auth) {

        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();

        if (profile.getPosition() != null) {
            String[] tOffset;
            Optional<Group> group = groupRepository.findById(profile.getGroup());
            List<EventDTO> events;
            if (group.isPresent()) {
                Group tGroup = group.get();
                if (tGroup.getType().equals("b")) {
                    tOffset = offset != null && offset.length == 3 ? new String[] {
                            CommonUtil.decode(offset[0]), CommonUtil.decode(offset[1])
                    } : new String[] { "1900-01-01", "1900-01-01" };

                    events = promotionRepository.findFullEventsByPromoter(profile.getId(), 20, step != null ? step : 5,
                            "%Y-%m-%d", tOffset);
                } else {
                    tOffset = offset != null && offset.length == 3 ? new String[] {
                            CommonUtil.decode(offset[0]), CommonUtil.decode(offset[1]), CommonUtil.decode(offset[2])
                    } : new String[] { "1900-01-01", "10", "1900-01-01" };

                    events = eventRepository.findEventsByRated(profile.getId(), 20, step != null ? step : 5, "%Y-%m-%d",
                            tOffset);
                }
            } else {
                tOffset = new String[] {};
                events = Collections.emptyList();
            }

            List<Object> lOffset = !events.isEmpty() ? events.get(events.size() - 1).getOffset() : List.of();

            return ResponseEntity.ok(new PageDTO<>(events, lOffset, 0, step));
        } else {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
        }
    }
}

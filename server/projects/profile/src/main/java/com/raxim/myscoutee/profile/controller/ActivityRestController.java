package com.raxim.myscoutee.profile.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.ControllerUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.ErrorDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.handler.EventItemParamHandler;
import com.raxim.myscoutee.profile.handler.EventParamHandler;
import com.raxim.myscoutee.profile.handler.ParamHandlers;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.service.EventService;

enum EventAction {
    lock("A"),
    unlock("P"); // based on gracePeriod it can be A, if mincapacity is ok

    private final String type;

    EventAction(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}

@RepositoryRestController
@RequestMapping("activity")
public class ActivityRestController {

    private final ProfileRepository profileRepository;
    private final EventService eventService;
    private final ParamHandlers paramHandlers;

    public ActivityRestController(ProfileRepository profileRepository,
            EventService eventService, ParamHandlers paramHandlers) {
        this.profileRepository = profileRepository;
        this.eventService = eventService;
        this.paramHandlers = paramHandlers;
    }

    @GetMapping("events")
    @Transactional
    public ResponseEntity<Object> getEvents(PageParam pageParam, Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();

        // override page param
        pageParam = paramHandlers.handle(profile, pageParam, EventParamHandler.TYPE);

        if (profile.getPosition() != null) {
            List<EventDTO> events = eventService.getEvents(pageParam,
                    new String[] { "A", "P", "C" }); // the statuses is coming from the page filter (SettingsRepository)

            List<Object> lOffset = CommonUtil.offset(events, pageParam.getOffset());

            return ResponseEntity.ok(
                    new PageDTO<>(events, lOffset, 0, pageParam.getType()));
        } else {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
        }
    }

    // TODO: multislot fix - saveEvent and saveItem should be the same method!!!
    // status needs to be filled in the event either T or P
    @PostMapping("events")
    @Transactional
    public ResponseEntity<EventDTO> createEvent(@RequestBody Event event,
            Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();

        event.setStatus("P");
        ResponseEntity<EventDTO> response = ControllerUtil.handle((p, e) -> eventService.saveEvent(p, e), profile,
                event,
                HttpStatus.CREATED);
        return response;
    }

    // can lock event, filter for own events (organized by me)
    @PatchMapping("events/{id}")
    @Transactional
    public ResponseEntity<?> patchEvent(@PathVariable String id, @RequestBody Event event,
            Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();

        ResponseEntity<EventDTO> response = ControllerUtil.handle((p, e) -> eventService.saveEvent(p, e), profile,
                event,
                HttpStatus.OK);
        return response;
    }

    @PostMapping("events/{id}/items")
    public ResponseEntity<EventDTO> addItem(@PathVariable String id, @RequestBody Event eventItem,
            Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();

        ResponseEntity<EventDTO> response = ControllerUtil.handle((p, i, ei) -> eventService.saveItem(p, i, ei),
                profile, id, eventItem,
                HttpStatus.CREATED);
        return response;
    }

    @PatchMapping("events/{id}/items/{itemId}")
    public ResponseEntity<EventDTO> patchItem(@PathVariable String id, @PathVariable String itemId,
            @RequestBody Event eventItem, Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();

        eventItem.setId(UUID.fromString(itemId));

        ResponseEntity<EventDTO> response = ControllerUtil.handle((p, i, ei) -> eventService.saveItem(p, i, ei),
                profile, id, eventItem,
                HttpStatus.OK);
        return response;
    }

    @DeleteMapping("events/{id}/items/{itemId}")
    public ResponseEntity<EventDTO> deleteItem(@PathVariable String id, @PathVariable String itemId,
            Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();

        Event eventItem = new Event(UUID.fromString(itemId));
        eventItem.setStatus("D");

        ResponseEntity<EventDTO> response = ControllerUtil.handle((p, i, ei) -> eventService.saveItem(p, i, ei),
                profile, id, eventItem,
                HttpStatus.NO_CONTENT);
        return response;
    }

    //TODO: promotion fix, only events for the current stage can be shown
    @GetMapping(value = { "events/{eventId}/items", "events/{id}/items/{eventId}/items",
            "invitations/{eventId}/items", "promotions/{id}/items" })
    public ResponseEntity<PageDTO<EventDTO>> getItems(@PathVariable String eventId,
            PageParam pageParam, Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();

        // override page param
        pageParam = paramHandlers.handle(profile, pageParam, EventItemParamHandler.TYPE);

        List<EventDTO> eventItems = eventService.getEventItems(pageParam, eventId);
        List<Object> lOffset = CommonUtil.offset(eventItems, pageParam.getOffset());

        return ResponseEntity.ok(new PageDTO<>(eventItems, lOffset));
    }

    // TODO: promotion fix
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

    // TODO invitation fix
    /*@GetMapping("invitations")
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
            List<EventDTO> events = eventService.getInvitations(profile.getId(),
                    CommonUtil.point(profile.getPosition()), 20,
                    step != null ? step : 5, profile.getGroup(), tOffset, 1.5);

            List<Object> lOffset = !events.isEmpty() ? events.get(events.size() - 1).getOffset() : List.of();

            return ResponseEntity.ok(new PageDTO<>(events, lOffset, 0));
        } else {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
        }
    }*/

    // TODO: recommendation fix
    @GetMapping("recommendations")
    @Transactional
    public ResponseEntity<?> getRecommendations(PageParam pageParam, Authentication auth) {
        return null;
    }
}

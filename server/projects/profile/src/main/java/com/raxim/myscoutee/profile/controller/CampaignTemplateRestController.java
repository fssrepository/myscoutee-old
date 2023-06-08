package com.raxim.myscoutee.profile.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.rest.webmvc.RepositoryRestController;
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
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.EventItem;
import com.raxim.myscoutee.profile.data.dto.rest.ErrorDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventItemDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.repository.mongo.EventItemRepository;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.service.EventService;
import com.raxim.myscoutee.profile.util.EventItemUtil;

@RepositoryRestController
@RequestMapping("promotions")
public class CampaignTemplateRestController {
    private final EventService eventService;
    private final EventRepository eventRepository;
    private final EventItemRepository eventItemRepository;

    public CampaignTemplateRestController(
            EventService eventService,
            EventRepository eventRepository,
            EventItemRepository eventItemRepository) {
        this.eventService = eventService;
        this.eventRepository = eventRepository;
        this.eventItemRepository = eventItemRepository;
    }

    @GetMapping("/templates")
    @Transactional
    public ResponseEntity<PageDTO<EventDTO>> getEvents(
            @RequestParam("step") String step,
            @RequestParam(value = "direction", defaultValue = "1") Integer direction,
            @RequestParam("offset") String[] offset,
            Authentication auth) {
        String[] tOffset;
        if (offset != null && offset.length == 2) {
            if ("Month".equals(step)) {
                String from = LocalDate.parse(CommonUtil.decode(offset[0]), DateTimeFormatter.ISO_DATE_TIME)
                        .withDayOfMonth(1)
                        .atStartOfDay()
                        .format(DateTimeFormatter.ISO_DATE_TIME);

                tOffset = new String[] { from, CommonUtil.decode(offset[1]) };
            } else {
                tOffset = new String[] { CommonUtil.decode(offset[0]), CommonUtil.decode(offset[1]) };
            }
        } else {
            if ("Month".equals(step)) {
                String from = LocalDate.now()
                        .withDayOfMonth(1)
                        .atStartOfDay()
                        .format(DateTimeFormatter.ISO_DATE_TIME);
                tOffset = new String[] { from, "1900-01-01" };
            } else {
                tOffset = new String[] { LocalDate.now()
                        .atStartOfDay()
                        .format(DateTimeFormatter.ISO_DATE_TIME), "1900-01-01" };
            }
        }

        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        UUID profileId = principal.getUser().getProfile().getId();

        List<EventDTO> events = eventService.getEvents(step, direction, tOffset, profileId, new String[] { "T" });

        List<Object> newOffset;
        if (!events.isEmpty()) {
            newOffset = events.get(events.size() - 1).getOffset();
        } else {
            newOffset = Arrays.asList(tOffset);
        }

        return ResponseEntity.ok(new PageDTO<>(events, newOffset, 0));
    }

    @PostMapping("/templates")
    @Transactional
    public ResponseEntity<EventDTO> createEvent(
            @RequestBody EventItem eventItem,
            Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        ResponseEntity<EventDTO> savedEventItem = EventItemUtil.save(eventService, eventItem,
                principal.getUser().getProfile(), true);
        return savedEventItem;
    }

    @PatchMapping("/templates/{id}")
    @Transactional
    public ResponseEntity<?> patchEvent(
            @PathVariable String id,
            @RequestBody EventItem eventItem,
            Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        ResponseEntity<?> response = EventItemUtil.update(eventService, eventItem, id,
                principal.getUser().getProfile());
        return response;
    }

    @PostMapping("/templates/{id}/items")
    public ResponseEntity<EventItemDTO> addItem(
            @PathVariable String id,
            @RequestBody EventItem eventItem,
            Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        ResponseEntity<EventItemDTO> savedEventItem = EventItemUtil.save(eventService, eventItem, id,
                principal.getUser().getProfile());
        return savedEventItem;
    }

    @PatchMapping("/templates/{id}/items/{itemId}")
    public ResponseEntity<EventItemDTO> patchItem(
            @PathVariable String id,
            @PathVariable String itemId,
            @RequestBody EventItem eventItem,
            Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        ResponseEntity<EventItemDTO> updatedEventItem = EventItemUtil.update(eventService, eventItem, id, itemId,
                principal.getUser().getProfile());
        return updatedEventItem;
    }

    @DeleteMapping("templates/{id}/items/{itemId}")
    public ResponseEntity<?> deleteItem(
            @PathVariable String id,
            @PathVariable String itemId) {
        Optional<Event> eventOptional = eventRepository.findById(UUID.fromString(id));
        if (eventOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.first_item"));
        }

        Event event = eventOptional.get();
        if (event.getInfo() != null && event.getInfo().getId().equals(UUID.fromString(itemId))) {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.first_item"));
        }

        Optional<EventItem> itemOptional = eventItemRepository.findById(UUID.fromString(itemId));
        if (itemOptional.isPresent()) {
            EventItem item = itemOptional.get();
            item.setStatus("D");
            eventItemRepository.save(item);
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = { "/templates/{id}/items" })
    public ResponseEntity<PageDTO<EventItemDTO>> items(
            @PathVariable String id,
            @RequestParam("step") Integer step,
            @RequestParam("offset") String[] offset,
            Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();

        Object[] tOffset;
        if (offset != null && offset.length == 2) {
            tOffset = new Object[] { CommonUtil.decode(offset[0]), CommonUtil.decode(offset[1]) };
        } else {
            tOffset = new Object[] { "1900-01-01", "1900-01-01" };
        }

        List<EventItemDTO> eventItems = eventService.getEventItems(UUID.fromString(id), step, tOffset,
                principal.getUser().getProfile().getId());

        List<Object> newOffset;
        if (!eventItems.isEmpty()) {
            newOffset = eventItems.get(eventItems.size() - 1).getOffset();
        } else {
            newOffset = Arrays.asList(tOffset);
        }

        return ResponseEntity.ok(new PageDTO<>(eventItems, newOffset));
    }
}

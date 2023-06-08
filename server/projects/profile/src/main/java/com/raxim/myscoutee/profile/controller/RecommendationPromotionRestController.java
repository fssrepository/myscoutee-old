package com.raxim.myscoutee.profile.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.config.properties.ConfigProperties;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.data.dto.rest.ErrorDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventItemDTO;
import com.raxim.myscoutee.profile.data.dto.rest.FeedbackDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PromotionDTO;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.PromotionRepository;
import com.raxim.myscoutee.profile.service.EventService;
import com.raxim.myscoutee.profile.util.EventUtil;

@RestController
@RequestMapping("/api")
public class RecommendationPromotionController {
    private final PromotionRepository promotionRepository;
    private final ConfigProperties config;
    private final EventService eventService;
    private final EventRepository eventRepository;

    public RecommendationPromotionController(
            PromotionRepository promotionRepository,
            ConfigProperties config,
            EventService eventService,
            EventRepository eventRepository) {
        this.promotionRepository = promotionRepository;
        this.config = config;
        this.eventService = eventService;
        this.eventRepository = eventRepository;
    }

    @GetMapping("/promotions")
    @Transactional
    public ResponseEntity<Object> getPromotions(
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset,
            Authentication auth) {
        // Convert offset array to List<Object>
        List<Object> lOffset;
        if (offset != null && offset.length == 5) {
            lOffset = Arrays.asList(
                    Double.parseDouble(offset[0]),
                    Double.parseDouble(offset[1]),
                    Double.parseDouble(offset[2]),
                    Double.parseDouble(offset[3]),
                    CommonUtil.decode(offset[4]));
        } else {
            lOffset = Arrays.asList(
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    LocalDate.now().atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME));
        }

        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();

        if (profile.getPosition() != null) {
            List<PromotionDTO> promotions = promotionRepository.findPromotionsByRec(
                    profile.getId(),
                    CommonUtil.point(profile.getPosition()),
                    20,
                    step != null ? step : 5,
                    "j",
                    profile.getGroup(),
                    lOffset.toArray());

            List<Object> lastOffset;
            if (!promotions.isEmpty()) {
                lastOffset = promotions.get(promotions.size() - 1).getOffset();
            } else {
                lastOffset = lOffset;
            }

            return ResponseEntity.ok(new PageDTO<>(promotions, lastOffset, 0));
        } else {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
        }
    }

    @GetMapping("/promotions/{promoId}/events")
    public ResponseEntity<PageDTO<EventDTO>> getEvents(
            @PathVariable String promoId,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset,
            Authentication auth) {
        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        User user = firebasePrincipal.getUser();
        UUID profileId = user.getProfile().getId();

        List<Object> tOffset;
        if (offset != null && offset.length == 3) {
            tOffset = Arrays.asList(CommonUtil.decode(offset[0]), CommonUtil.decode(offset[1]),
                    CommonUtil.decode(offset[2]));
        } else {
            tOffset = Arrays.asList("1900-01-01", 10, "1900-01-01");
        }

        List<EventDTO> events = eventService.getEventsByPromotion(
                profileId,
                UUID.fromString(promoId),
                step,
                tOffset.toArray());

        List<Object> lastOffset;
        if (!events.isEmpty()) {
            lastOffset = events.get(events.size() - 1).getOffset();
        } else {
            lastOffset = tOffset;
        }

        return ResponseEntity.ok(new PageDTO<>(events, lastOffset));
    }

    @PostMapping("/promotions/{promoId}/events/{id}/clone")
    public ResponseEntity<?> cloneEvent(
            @PathVariable String promoId,
            @PathVariable String id,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset,
            Authentication auth) {
        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();

        List<Event> events = eventRepository.findPendingEvents(UUID.fromString(id), profile.getId());
        Event promoter = events.stream()
                .filter(event -> event.getInfo().getMembers().stream()
                        .anyMatch(member -> member.getProfile().getId().equals(profile.getId())))
                .findFirst()
                .orElse(null);

        if (promoter == null) {
            Optional<Event> event = eventService.cloneEvent(UUID.fromString(id), profile);
            if (event.isPresent()) {
                EventDTO tEvent = EventUtil.transform(event.get());
                return ResponseEntity.ok(tEvent);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.clone_not_allowed"));
        }
    }

    @GetMapping("/promotions/{promoId}/events/{id}/feedbacks")
    public ResponseEntity<PageDTO<FeedbackDTO>> getEventFeedbacks(
            @PathVariable String id,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset) {
        List<Object> tOffset;
        if (offset != null && offset.length == 1) {
            tOffset = Arrays.asList(CommonUtil.decode(offset[0]));
        } else {
            tOffset = Collections.singletonList("1900-01-01");
        }

        List<FeedbackDTO> feedbacks = eventRepository.findFeedbacksByEvent(
                UUID.fromString(id),
                20,
                step != null ? step : 5,
                tOffset.toArray());

        List<Object> lastOffset;
        if (!feedbacks.isEmpty()) {
            lastOffset = feedbacks.get(feedbacks.size() - 1).getOffset();
        } else {
            lastOffset = tOffset;
        }

        return ResponseEntity.ok(new PageDTO<>(feedbacks, lastOffset));
    }

    @GetMapping("/promotions/{promoId}/events/{id}/items")
    public ResponseEntity<PageDTO<EventItemDTO>> getEventItems(
            @PathVariable String id,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset,
            Authentication auth) {
        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();

        List<Object> tOffset;
        if (offset != null && offset.length == 2) {
            tOffset = Arrays.asList(CommonUtil.decode(offset[0]), CommonUtil.decode(offset[1]));
        } else {
            tOffset = Arrays.asList("1900-01-01", "1900-01-01");
        }

        List<EventItemDTO> eventItems = eventService.getEventItems(
                UUID.fromString(id),
                step,
                tOffset.toArray(),
                profile.getId());

        List<Object> lastOffset;
        if (!eventItems.isEmpty()) {
            lastOffset = eventItems.get(eventItems.size() - 1).getOffset();
        } else {
            lastOffset = tOffset;
        }

        return ResponseEntity.ok(new PageDTO<>(eventItems, lastOffset));
    }
}

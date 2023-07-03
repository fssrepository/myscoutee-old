package com.raxim.myscoutee.profile.controller;

import java.util.Arrays;
import java.util.List;
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
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Promotion;
import com.raxim.myscoutee.profile.data.dto.rest.ErrorDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventItemDTO;
import com.raxim.myscoutee.profile.data.dto.rest.FeedbackDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.data.dto.rest.SchoolDTO;
import com.raxim.myscoutee.profile.repository.mongo.EventItemRepository;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.service.CampaignService;
import com.raxim.myscoutee.profile.service.EventService;
import com.raxim.myscoutee.profile.service.ProfileService;
import com.raxim.myscoutee.profile.service.PromotionService;
import com.raxim.myscoutee.profile.util.EventItemUtil;
import com.raxim.myscoutee.profile.util.PromotionUtil;

@RepositoryRestController
@RequestMapping("promotions")
public class CampaignEventRestController {
    private final EventService eventService;
    private final PromotionService promotionService;
    private final EventRepository eventRepository;
    private final EventItemRepository eventItemRepository;
    private final ProfileService profileService;
    private final CampaignService campaignService;

    public CampaignEventRestController(
            EventService eventService,
            EventRepository eventRepository,
            EventItemRepository eventItemRepository,
            ProfileService profileService,
            CampaignService campaignService,
            PromotionService promotionService) {
        this.eventService = eventService;
        this.eventRepository = eventRepository;
        this.eventItemRepository = eventItemRepository;
        this.profileService = profileService;
        this.campaignService = campaignService;
        this.promotionService = promotionService;
    }

    @GetMapping("{promoId}/events")
    public ResponseEntity<PageDTO<EventDTO>> events(
            @PathVariable String promoId,
            Authentication auth,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        UUID profileId = principal.getUser().getProfile().getId();

        Object[] tOffset;
        if (offset != null && offset.length == 3) {
            tOffset = new Object[] { CommonUtil.decode(offset[0]), CommonUtil.decode(offset[1]),
                    CommonUtil.decode(offset[2]) };
        } else {
            tOffset = new Object[] { "1900-01-01", 10, "1900-01-01" };
        }

        List<EventDTO> events = this.promotionService.getEventsByPromotion(profileId, UUID.fromString(promoId), step,
                tOffset);
        events.forEach(event -> {
            event.setRate(null);
        });

        List<Object> lOffset;
        if (!events.isEmpty()) {
            lOffset = events.get(events.size() - 1).getOffset();
        } else {
            lOffset = Arrays.asList(tOffset);
        }

        return ResponseEntity.ok(new PageDTO<>(events, lOffset));
    }

    //TODO: to be fixed 
    /*@PatchMapping("{promoId}/events/{id}")
    @Transactional
    public ResponseEntity<?> patchPromotionEvent(
            @PathVariable String promoId,
            @PathVariable String id,
            @RequestBody EventItem eventItem,
            Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();

        ResponseEntity<?> eventResp = EventItemUtil.update(eventService, eventItem, id, profile);

        Promotion promotion = new Promotion();
        promotion.setId(UUID.fromString(promoId));
        PromotionUtil.update(campaignService, promotion, profile, true);

        return eventResp;
    }*/

    @GetMapping("{promoId}/events/{id}/items")
    public ResponseEntity<PageDTO<EventItemDTO>> eventItems(
            @PathVariable String id,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset,
            Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        UUID profileId = principal.getUser().getProfile().getId();

        Object[] tOffset;
        if (offset != null && offset.length == 2) {
            tOffset = new Object[] { CommonUtil.decode(offset[0]), CommonUtil.decode(offset[1]) };
        } else {
            tOffset = new Object[] { "1900-01-01", "1900-01-01" };
        }

        List<EventItemDTO> eventItems = this.eventService.getEventItems(UUID.fromString(id), step, tOffset, profileId);

        List<Object> lOffset;
        if (!eventItems.isEmpty()) {
            lOffset = eventItems.get(eventItems.size() - 1).getOffset();
        } else {
            lOffset = Arrays.asList(tOffset);
        }

        return ResponseEntity.ok(new PageDTO<>(eventItems, lOffset));
    }

    //TODO: to be fixed 
    /*@PostMapping("{promoId}/events/{id}/items")
    public ResponseEntity<EventItemDTO> addPromoEventItem(
            @PathVariable String promoId,
            @PathVariable String id,
            @RequestBody EventItem eventItem,
            Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();

        ResponseEntity<EventItemDTO> eventResp = EventItemUtil.save(eventService, eventItem, id, profile);

        Promotion promotion = new Promotion();
        promotion.setId(UUID.fromString(promoId));
        PromotionUtil.update(campaignService, promotion, profile, true);

        return eventResp;
    }*/

    //TODO: to be fixed 
    /*@PatchMapping("{promoId}/events/{id}/items/{itemId}")
    public ResponseEntity<EventItemDTO> patchPromoEventItem(
            @PathVariable String promoId,
            @PathVariable String id,
            @PathVariable String itemId,
            @RequestBody EventItem eventItem,
            Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();

        ResponseEntity<EventItemDTO> eventResp = EventItemUtil.update(eventService, eventItem, id, itemId, profile);

        Promotion promotion = new Promotion();
        promotion.setId(UUID.fromString(promoId));
        PromotionUtil.update(campaignService, promotion, profile, true);

        return eventResp;
    }*/

    //TODO: to be fixed
    /*@DeleteMapping("{promoId}/events/{id}/items/{itemId}")
    public ResponseEntity<?> deletePromoItem(
            @PathVariable String id,
            @PathVariable String itemId) {
        Event event = this.eventRepository.findById(UUID.fromString(id)).get();
        if (event.getInfo().getId().equals(UUID.fromString(itemId))) {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.first_item"));
        }

        EventItem item = this.eventItemRepository.findById(UUID.fromString(itemId)).get();
        item.setStatus("D");
        this.eventItemRepository.save(item);

        return ResponseEntity.noContent().build();
    }*/

    @GetMapping("{promoId}/events/{id}/members")
    public ResponseEntity<PageDTO<ProfileDTO>> eventMembers(
            @PathVariable String id,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset,
            Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        UUID profileId = principal.getUser().getProfile().getId();

        Object[] tOffset;
        if (offset != null && offset.length == 3) {
            tOffset = new Object[] { CommonUtil.decode(offset[0]), CommonUtil.decode(offset[1]),
                    CommonUtil.decode(offset[2]) };
        } else {
            tOffset = new Object[] { "A", "1900-01-01", "1900-01-01" };
        }

        List<ProfileDTO> profiles = this.eventRepository.findProfilesByPromotion(UUID.fromString(id), 20,
                step != null ? step : 5, profileId, tOffset);

        List<Object> lOffset;
        if (!profiles.isEmpty()) {
            lOffset = profiles.get(profiles.size() - 1).getOffset();
        } else {
            lOffset = Arrays.asList(tOffset);
        }

        return ResponseEntity.ok(new PageDTO<>(profiles, lOffset));
    }

    @GetMapping(value = { "{promoId}/events/{eventId}/members/{id}/schools", "{promoId}/members/{id}/schools" })
    public ResponseEntity<PageDTO<SchoolDTO>> getSchools(
            @PathVariable String id,
            Authentication auth,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset) {
        Object[] tOffset;
        if (offset != null && offset.length == 3) {
            tOffset = new Object[] { CommonUtil.decode(offset[0]), CommonUtil.decode(offset[1]),
                    CommonUtil.decode(offset[2]) };
        } else {
            tOffset = new Object[] { "a", "1900-01-01", "1900-01-01" };
        }

        List<SchoolDTO> schools = this.profileService.getSchools(UUID.fromString(id), step, tOffset);

        List<Object> lOffset;
        if (!schools.isEmpty()) {
            lOffset = schools.get(schools.size() - 1).getOffset();
        } else {
            lOffset = Arrays.asList(tOffset);
        }

        return ResponseEntity.ok(new PageDTO<>(schools, lOffset));
    }

    @GetMapping("{promoId}/events/{id}/feedbacks")
    public ResponseEntity<PageDTO<FeedbackDTO>> feedbacks(
            @PathVariable String id,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset) {
        Object[] tOffset;
        if (offset != null && offset.length == 1) {
            tOffset = new Object[] { CommonUtil.decode(offset[0]) };
        } else {
            tOffset = new Object[] { "1900-01-01" };
        }

        List<FeedbackDTO> feedbacks = this.eventRepository.findFeedbacksByEvent(UUID.fromString(id), 20,
                step != null ? step : 5, tOffset);

        List<Object> lOffset;
        if (!feedbacks.isEmpty()) {
            lOffset = feedbacks.get(feedbacks.size() - 1).getOffset();
        } else {
            lOffset = Arrays.asList(tOffset);
        }

        return ResponseEntity.ok(new PageDTO<>(feedbacks, lOffset));
    }
}

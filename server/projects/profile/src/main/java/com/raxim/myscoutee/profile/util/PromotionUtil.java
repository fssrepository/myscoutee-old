package com.raxim.myscoutee.profile.util;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Promotion;
import com.raxim.myscoutee.profile.data.dto.rest.ErrorDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PromotionDTO;
import com.raxim.myscoutee.profile.service.CampaignService;

public class PromotionUtil {

    public static ResponseEntity<PromotionDTO> save(CampaignService campaignService, Promotion pPromotion, Profile profile) {
        Optional<Pair<Promotion, Boolean>> promotion = campaignService.getPromotion(pPromotion, profile, false, false);
        if (promotion.isPresent()) {
            Promotion oldPromotion = promotion.get().getFirst();
            Optional<Promotion> savedPromotion = campaignService.savePromotion(oldPromotion, profile, false, false);
            if (savedPromotion.isPresent()) {
                return new ResponseEntity<>(
                        new PromotionDTO(savedPromotion.get()),
                        HttpStatus.CREATED);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public static ResponseEntity<?> update(CampaignService campaignService, Promotion pPromotion, Profile profile,
            boolean isEvent) {
        Optional<Pair<Promotion, Boolean>> promotion = campaignService.getPromotion(pPromotion, profile, true, isEvent);
        if (promotion.isPresent()) {
            Promotion oldPromotion = promotion.get().getFirst();

            UUID[] refIds = oldPromotion.getEvents().stream()
                    .filter(event -> !"D".equals(event.getStatus()))
                    .map(event -> event.getId())
                    .toArray(UUID[]::new);

            List<Event> events = campaignService.getAllActiveEvents(refIds);
            if (events.size() > 0) {
                return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.has_active_events"));
            } else {
                Optional<Promotion> savedPromotion = campaignService.savePromotion(
                        oldPromotion,
                        profile,
                        isEvent,
                        promotion.get().getSecond());
                if (savedPromotion.isPresent()) {
                    return new ResponseEntity<>(
                            new PromotionDTO(savedPromotion.get()),
                            HttpStatus.CREATED);
                } else {
                    return ResponseEntity.badRequest().build();
                }
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

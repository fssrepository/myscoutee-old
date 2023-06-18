package com.raxim.myscoutee.profile.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.repository.mongo.PromotionRepository;

@Service
public class PromotionService {
    private final PromotionRepository promotionRepository;

    public PromotionService(
            PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    public List<EventDTO> getEventsByPromotion(UUID profileId, UUID eventId, Integer step, Object[] tOffset) {
        return promotionRepository.findEventsByPromotion(eventId, 20, step != null ? step : 5, "%Y-%m-%d", profileId,
                tOffset);
    }
}

package com.raxim.myscoutee.profile.controller;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Idea;
import com.raxim.myscoutee.profile.data.document.mongo.Job;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Promotion;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.data.dto.rest.ErrorDTO;
import com.raxim.myscoutee.profile.data.dto.rest.IdeaDTO;
import com.raxim.myscoutee.profile.data.dto.rest.JobDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PromotionDTO;
import com.raxim.myscoutee.profile.repository.mongo.PromotionRepository;
import com.raxim.myscoutee.profile.service.CampaignService;
import com.raxim.myscoutee.profile.util.PromotionUtil;

@RepositoryRestController
@RequestMapping("promotions")
public class CampaignRestController {

    private final CampaignService campaignService;
    private final PromotionRepository promotionRepository;

    public CampaignRestController(CampaignService campaignService, PromotionRepository promotionRepository) {
        this.campaignService = campaignService;
        this.promotionRepository = promotionRepository;
    }

    @GetMapping("")
    public ResponseEntity<Object> getPromotions(Authentication auth,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset) {

        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();

        Object[] tOffset;
        if (offset != null && offset.length == 2) {
            tOffset = new Object[] { CommonUtil.decode(offset[0]), CommonUtil.decode(offset[1]) };
        } else {
            tOffset = new Object[] { "1900-01-01", "1900-01-01" };
        }

        if (profile == null || profile.getId() == null) {
            return ResponseEntity.notFound().build();
        } else {
            if (profile.getPosition() != null) {
                List<PromotionDTO> promotions = campaignService.getPromotions(profile.getId(), step, tOffset);

                List<Object> lOffset;
                if (!promotions.isEmpty()) {
                    lOffset = promotions.get(promotions.size() - 1).getOffset();
                } else {
                    lOffset = Arrays.asList(tOffset);
                }

                return ResponseEntity.ok(new PageDTO<>(promotions, lOffset));
            } else {
                return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
            }
        }
    }

    //TODO: to be fixed 
    /*@PostMapping("")
    @Transactional
    public ResponseEntity<PromotionDTO> addPromotion(Authentication auth, @RequestBody Promotion promotion) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();
        return PromotionUtil.save(this.campaignService, promotion, profile);
    }*/

    //TODO: to be fixed 
    /*@PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<?> patchPromotion(Authentication auth, @PathVariable String id,
            @RequestBody Promotion promotion) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();
        return PromotionUtil.update(this.campaignService, promotion, profile, false);
    }*/

    @GetMapping("/{promoId}/members")
    public ResponseEntity<PageDTO<ProfileDTO>> members(@PathVariable String promoId,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset,
            Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        User user = principal.getUser();
        UUID profileId = user.getProfile().getId();

        Object[] tOffset;
        if (offset != null && offset.length == 3) {
            tOffset = new Object[] { CommonUtil.decode(offset[0]), CommonUtil.decode(offset[1]),
                    CommonUtil.decode(offset[2]) };
        } else {
            tOffset = new Object[] { "A", "1900-01-01", "1900-01-01" };
        }

        List<ProfileDTO> profiles = promotionRepository.findProfilesByPromotion(UUID.fromString(promoId), 20,
                step != null ? step : 5, profileId, tOffset);

        List<Object> lOffset;
        if (!profiles.isEmpty()) {
            lOffset = profiles.get(profiles.size() - 1).getOffset();
        } else {
            lOffset = Arrays.asList(tOffset);
        }

        return ResponseEntity.ok(new PageDTO<>(profiles, lOffset));
    }

    @Deprecated
    @GetMapping("/ideas")
    public ResponseEntity<PageDTO<IdeaDTO>> getIdeas(Authentication auth,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset) {

        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        User user = principal.getUser();
        UUID profileId = user.getProfile().getId();

        Object[] tOffset;
        if (offset != null && offset.length == 1) {
            tOffset = new Object[] { CommonUtil.decode(offset[0]) };
        } else {
            tOffset = new Object[] { "1900-01-01" };
        }

        if (profileId == null) {
            return ResponseEntity.notFound().build();
        } else {
            List<IdeaDTO> ideas = campaignService.getIdeas(profileId, step, tOffset);

            List<Object> lOffset;
            if (!ideas.isEmpty()) {
                lOffset = ideas.get(ideas.size() - 1).getOffset();
            } else {
                lOffset = Arrays.asList(tOffset);
            }

            return ResponseEntity.ok(new PageDTO<>(ideas, lOffset));
        }
    }

    @Deprecated
    @PostMapping("/ideas")
    @Transactional
    public ResponseEntity<IdeaDTO> addIdea(Authentication auth,
            @RequestBody Idea idea) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        User user = principal.getUser();
        UUID profileId = user.getProfile().getId();

        if (profileId == null) {
            return ResponseEntity.notFound().build();
        } else {
            IdeaDTO ideaDto = campaignService.saveIdea(profileId, null, idea);
            return ResponseEntity.status(HttpStatus.CREATED).body(ideaDto);
        }
    }

    @Deprecated
    @PatchMapping("/ideas/{id}")
    @Transactional
    public ResponseEntity<IdeaDTO> patchIdea(Authentication auth,
            @PathVariable String id, @RequestBody Idea idea) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        User user = principal.getUser();
        UUID profileId = user.getProfile().getId();

        if (profileId == null) {
            return ResponseEntity.notFound().build();
        } else {
            IdeaDTO ideaDto = campaignService.saveIdea(profileId, CommonUtil.parseUUID(id),
                    idea);
            return ResponseEntity.ok(ideaDto);
        }
    }

    @Deprecated
    @GetMapping("/jobs")
    public ResponseEntity<PageDTO<JobDTO>> getJobs(Authentication auth,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset) {

        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        User user = principal.getUser();
        UUID profileId = user.getProfile().getId();

        Object[] tOffset;
        if (offset != null && offset.length == 1) {
            tOffset = new Object[] { CommonUtil.decode(offset[0]) };
        } else {
            tOffset = new Object[] { "1900-01-01" };
        }

        if (profileId == null) {
            return ResponseEntity.notFound().build();
        } else {
            List<JobDTO> jobs = campaignService.getJobs(profileId, step, tOffset);

            List<Object> lOffset;
            if (!jobs.isEmpty()) {
                lOffset = jobs.get(jobs.size() - 1).getOffset();
            } else {
                lOffset = Arrays.asList(tOffset);
            }

            return ResponseEntity.ok(new PageDTO<>(jobs, lOffset));
        }
    }

    @Deprecated
    @PostMapping("/jobs")
    @Transactional
    public ResponseEntity<JobDTO> addJob(Authentication auth,
            @RequestBody Job job) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        User user = principal.getUser();
        UUID profileId = user.getProfile().getId();

        if (profileId == null) {
            return ResponseEntity.notFound().build();
        } else {
            JobDTO jobDto = campaignService.saveJob(profileId, null, job);
            return ResponseEntity.status(HttpStatus.CREATED).body(jobDto);
        }
    }

    @Deprecated
    @PatchMapping("/jobs/{id}")
    @Transactional
    public ResponseEntity<JobDTO> patchJob(Authentication auth,
            @PathVariable String id, @RequestBody Job job) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        User user = principal.getUser();
        UUID profileId = user.getProfile().getId();

        if (profileId == null) {
            return ResponseEntity.notFound().build();
        } else {
            JobDTO jobDto = campaignService.saveJob(profileId, CommonUtil.parseUUID(id), job);
            return ResponseEntity.ok(jobDto);
        }
    }
}

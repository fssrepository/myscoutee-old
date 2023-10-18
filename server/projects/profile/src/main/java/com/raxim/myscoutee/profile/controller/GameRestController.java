package com.raxim.myscoutee.profile.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.data.dto.rest.SchoolDTO;
import com.raxim.myscoutee.profile.exception.MessageException;
import com.raxim.myscoutee.profile.handler.LikeParamHandler;
import com.raxim.myscoutee.profile.handler.ParamHandlers;
import com.raxim.myscoutee.profile.handler.SchoolParamHandler;
import com.raxim.myscoutee.profile.service.ProfileService;
import com.raxim.myscoutee.profile.service.SchoolService;

@RestController
@RequestMapping("games")
public class GameRestController {

    private final ProfileService profileService;
    private final SchoolService schoolService;
    private final ParamHandlers paramHandlers;

    public GameRestController(ProfileService profileService, SchoolService schoolService, ParamHandlers paramHandlers) {
        this.profileService = profileService;
        this.schoolService = schoolService;
        this.paramHandlers = paramHandlers;
    }

    @GetMapping("/rate_none")
    public ResponseEntity<Object> nonRated(PageParam pageParam,
            Authentication auth) {
        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        pageParam = paramHandlers.handle(profile, pageParam, LikeParamHandler.TYPE);

        List<ProfileDTO> profileDTOs;
        try {
            profileDTOs = this.profileService
                    .getProfiles(pageParam, profile, "/games/rate_none", 0.0, profile.getScore());
            List<Object> lOffset = CommonUtil.offset(profileDTOs, pageParam.getOffset());
            return ResponseEntity.ok(new PageDTO<>(profileDTOs, lOffset));
        } catch (MessageException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getErrorDTO());
        }
    }

    @GetMapping("/rate_give")
    public ResponseEntity<Object> rateGive(PageParam pageParam,
            Authentication auth) {
        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        pageParam = paramHandlers.handle(profile, pageParam, LikeParamHandler.TYPE);

        List<ProfileDTO> profileDTOs;
        try {
            profileDTOs = this.profileService
                    .getProfiles(pageParam, profile, "/games/rate_give", 1.0, 0);
            List<Object> lOffset = CommonUtil.offset(profileDTOs, pageParam.getOffset());
            return ResponseEntity.ok(new PageDTO<>(profileDTOs, lOffset));
        } catch (MessageException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getErrorDTO());
        }
    }

    @GetMapping("/rate_receive")
    public ResponseEntity<Object> rateGet(PageParam pageParam,
            Authentication auth) {
        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        pageParam = paramHandlers.handle(profile, pageParam, LikeParamHandler.TYPE);

        List<ProfileDTO> profileDTOs;
        try {
            profileDTOs = this.profileService
                    .getProfiles(pageParam, profile, "/games/rate_receive", 2.0, 0);
            List<Object> lOffset = CommonUtil.offset(profileDTOs, pageParam.getOffset());
            return ResponseEntity.ok(new PageDTO<>(profileDTOs, lOffset));
        } catch (MessageException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getErrorDTO());
        }
    }

    @GetMapping("/rate_both")
    public ResponseEntity<?> rateBoth(PageParam pageParam,
            Authentication auth) {
        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        pageParam = paramHandlers.handle(profile, pageParam, LikeParamHandler.TYPE);

        List<ProfileDTO> profileDTOs;
        try {
            profileDTOs = this.profileService
                    .getProfiles(pageParam, profile, "/games/rate_both", 1.5, 0);
            List<Object> lOffset = CommonUtil.offset(profileDTOs, pageParam.getOffset());
            return ResponseEntity.ok(new PageDTO<>(profileDTOs, lOffset));
        } catch (MessageException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getErrorDTO());
        }
    }

    @GetMapping("/rate_double")
    public ResponseEntity<?> doubleNone(PageParam pageParam,
            Authentication auth) {
        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        pageParam = paramHandlers.handle(profile, pageParam, LikeParamHandler.TYPE);

        List<ProfileDTO> profileDTOs;
        try {
            // direction is ignored, however it is always rate_give
            profileDTOs = this.profileService
                    .getProfiles(pageParam, profile, "/games/rate_double", 1.0, 0);
            List<Object> lOffset = CommonUtil.offset(profileDTOs, pageParam.getOffset());
            return ResponseEntity.ok(new PageDTO<>(profileDTOs, lOffset));
        } catch (MessageException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getErrorDTO());
        }
    }

    // TODO: school fix - discreet group - isBusiness/isSchool event - discreet
    // level
    @GetMapping(value = { "/rate_none/{id}/schools", "/rate_give/{id}/schools",
            "rate_receive/{id}/schools", "/rate_double/{id}/schools",
            "/rate_both/{id}/schools" })
    public ResponseEntity<PageDTO<SchoolDTO>> getSchools(@PathVariable String id, PageParam pageParam,
            Authentication auth) {
        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();

        pageParam = paramHandlers.handle(profile, pageParam, SchoolParamHandler.TYPE);

        List<SchoolDTO> schoolDTOs = schoolService.getSchools(UUID.fromString(id), pageParam);
        List<Object> lOffset = CommonUtil.offset(schoolDTOs, pageParam.getOffset());

        return ResponseEntity.ok(new PageDTO<>(schoolDTOs, lOffset));
    }

}
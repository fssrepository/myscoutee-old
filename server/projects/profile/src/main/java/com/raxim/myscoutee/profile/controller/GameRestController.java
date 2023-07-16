package com.raxim.myscoutee.profile.controller;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
import com.raxim.myscoutee.profile.service.ProfileService;

@RepositoryRestController
@RequestMapping("games")
public class GameRestController {

    private final ProfileService profileService;
    private final ParamHandlers paramHandlers;

    public GameRestController(ProfileService profileService, ParamHandlers paramHandlers) {
        this.profileService = profileService;
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

    // TODO: school fix - discreet group - isBusiness/isSchool event - discreet level
    @GetMapping(value = { "/rate_none/{id}/schools", "/rate_give/{id}/schools",
            "/rate_give/{id}/schools", "/rate_give/{id}/schools", "/rate_give/{id}/schools",
            "rate_receive/{id}/schools",
            "/rate_double/{id}/schools", "/rate_double/{id}/schools",
            "/rate_both/{id}/schools" })
    public ResponseEntity<PageDTO<SchoolDTO>> getSchools(
            @PathVariable String id, Authentication auth,
            @RequestParam(name = "step", required = false) Integer step,
            @RequestParam(name = "offset", required = false) String[] offset) {
        Object[] tOffset;
        if (offset != null && offset.length == 3) {
            tOffset = new Object[] {
                    CommonUtil.decode(offset[0]),
                    CommonUtil.decode(offset[1]),
                    CommonUtil.decode(offset[2])
            };
        } else {
            tOffset = new Object[] { "a", "1900-01-01", "1900-01-01" };
        }

        List<SchoolDTO> schools = this.profileService
                .getSchools(UUID.fromString(id), step, tOffset);

        // http://dolszewski.com/spring/how-to-bind-requestparam-to-object/
        List<Object> lOffset;
        if (!schools.isEmpty()) {
            lOffset = schools.get(schools.size() - 1).getOffset();
        } else {
            lOffset = Arrays.asList(tOffset);
        }

        PageDTO<SchoolDTO> pageDTO = new PageDTO<>(schools, lOffset);
        return ResponseEntity.ok().body(pageDTO);
    }

}
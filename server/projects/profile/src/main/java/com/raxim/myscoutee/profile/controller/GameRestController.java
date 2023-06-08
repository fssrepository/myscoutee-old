package com.raxim.myscoutee.profile.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.ErrorDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.data.dto.rest.SchoolDTO;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;
import com.raxim.myscoutee.profile.service.ProfileService;

@RepositoryRestController
@RequestMapping("games")
public class GameRestController {

    private final ProfileRepository profileRepository;
    private final EventRepository eventRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final ObjectMapper objectMapper;

    public GameRestController(ProfileRepository profileRepository,
            EventRepository eventRepository,
            LikeRepository likeRepository,
            UserRepository userRepository,
            ProfileService profileService,
            ObjectMapper objectMapper) {
        this.profileRepository = profileRepository;
        this.eventRepository = eventRepository;
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
        this.profileService = profileService;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = { "/rate_none/{id}/schools", "/rate_give/{id}/schools",
            "/rate_give/{id}/schools", "/rate_give/{id}/schools", "/rate_give/{id}/schools",
            "rate_receive/{id}/schools", "/rate_double/{id}/rated/{id}/schools",
            "/rate_double/{id}/none/{id}/schools",
            "/rate_both/{id}/schools", "/rate_met/{id}/schools" })
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

    @GetMapping("/rate_none")
    public ResponseEntity<Object> nonRated(
            @RequestParam("step") Integer step,
            @RequestParam("offset") String[] offset,
            Authentication auth) {
        // parameter ? man : woman, filter out type = double, filter out from, to field
        // is the session user and type = single
        // (add field for history rated by)

        Object[] tOffset;
        if (offset != null && offset.length == 4) {
            tOffset = new Object[] {
                    Double.valueOf(CommonUtil.decode(offset[0])),
                    Double.valueOf(CommonUtil.decode(offset[1])),
                    Double.valueOf(CommonUtil.decode(offset[2])),
                    CommonUtil.decode(offset[3])
            };
        } else {
            tOffset = new Object[] { 0.0, 0.0, 0.0, "1900-01-01" };
        }

        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();
        String gender = profile.getGender().equals("m") ? "w" : "m";

        if (profile.getStatus().equals("F")) {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.friends_only"));
        }

        if (profile.getStatus().equals("I")) {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.invisible"));
        }

        if (profile.getPosition() != null) {
            List<ProfileDTO> profiles = profileRepository.findProfile(
                    CommonUtil.point(profile.getPosition()),
                    tOffset,
                    20,
                    step != null ? step : 5,
                    profile.getId(),
                    gender,
                    profile.getGroup(),
                    0.0,
                    profile.getScore());

            // http://dolszewski.com/spring/how-to-bind-requestparam-to-object/

            List<Object> lOffset;
            if (!profiles.isEmpty()) {
                lOffset = profiles.get(profiles.size() - 1).getOffset();
            } else {
                lOffset = Arrays.asList(tOffset);
            }

            return ResponseEntity.ok(new PageDTO<>(profiles, lOffset));
        } else {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
        }
    }

    @GetMapping("/rate_give")
    public ResponseEntity<Object> rateGive(
            @RequestParam("step") Integer step,
            @RequestParam("offset") String[] offset,
            Authentication auth) {
        // parameter ? man : woman, filter out type = double, filter out from, to field
        // is the session user and type = single
        // (add field for history rated by)

        Object[] tOffset;
        if (offset != null && offset.length == 4) {
            tOffset = new Object[] {
                    Double.valueOf(CommonUtil.decode(offset[0])),
                    Double.valueOf(CommonUtil.decode(offset[1])),
                    Double.valueOf(CommonUtil.decode(offset[2])),
                    CommonUtil.decode(offset[3])
            };
        } else {
            tOffset = new Object[] { 0.0, 0.0, 0.0, "1900-01-01" };
        }

        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();
        String gender = profile.getGender().equals("m") ? "w" : "m";

        if (profile.getStatus().equals("F")) {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.friends_only"));
        }

        if (profile.getStatus().equals("I")) {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.invisible"));
        }

        if (profile.getPosition() != null) {
            List<ProfileDTO> profiles = profileRepository.findProfile(
                    CommonUtil.point(profile.getPosition()),
                    tOffset,
                    20,
                    step != null ? step : 5,
                    profile.getId(),
                    gender,
                    profile.getGroup(),
                    1.0);

            // http://dolszewski.com/spring/how-to-bind-requestparam-to-object/

            List<Object> lOffset;
            if (!profiles.isEmpty()) {
                lOffset = profiles.get(profiles.size() - 1).getOffset();
            } else {
                lOffset = Arrays.asList(tOffset);
            }

            return ResponseEntity.ok(new PageDTO<>(profiles, lOffset));
        } else {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
        }
    }

    @GetMapping("/rate_receive")
    public ResponseEntity<Object> rateGet(
            @RequestParam("step") Integer step,
            @RequestParam("offset") String[] offset,
            Authentication auth) {
        // parameter ? man : woman, filter out type = double, filter out from, to field
        // is the session user and type = single
        // (add field for history rated by)

        Object[] tOffset;
        if (offset != null && offset.length == 4) {
            tOffset = new Object[] {
                    Double.valueOf(CommonUtil.decode(offset[0])),
                    Double.valueOf(CommonUtil.decode(offset[1])),
                    Double.valueOf(CommonUtil.decode(offset[2])),
                    CommonUtil.decode(offset[3])
            };
        } else {
            tOffset = new Object[] { 0.0, 0.0, 0.0, "1900-01-01" };
        }

        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();
        String gender = profile.getGender().equals("m") ? "w" : "m";

        if (profile.getStatus().equals("F")) {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.friends_only"));
        }

        if (profile.getStatus().equals("I")) {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.invisible"));
        }

        if (profile.getPosition() != null) {
            List<ProfileDTO> fProfiles = profileRepository.findProfile(
                    CommonUtil.point(profile.getPosition()),
                    tOffset,
                    20,
                    step != null ? step : 5,
                    profile.getId(),
                    gender,
                    profile.getGroup(),
                    2.0);

            // nullify rate received
            List<ProfileDTO> profiles = fProfiles.stream().map(fProfile -> {
                ProfileDTO clonedProfile = JsonUtil.clone(fProfile, objectMapper);
                clonedProfile.setRate(0);
                return clonedProfile;
            })
                    .collect(Collectors.toList());

            // http://dolszewski.com/spring/how-to-bind-requestparam-to-object/

            List<Object> lOffset;
            if (!profiles.isEmpty()) {
                lOffset = profiles.get(profiles.size() - 1).getOffset();
            } else {
                lOffset = Arrays.asList(tOffset);
            }

            return ResponseEntity.ok(new PageDTO<>(profiles, lOffset));
        } else {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
        }
    }

    @GetMapping("/rate_double/{id}/rated")
    public ResponseEntity<?> rateDouble(
            @PathVariable String id,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset,
            Authentication auth) {
        // parameter ? man : woman, filter out type = double, filter out from, to field
        // is the session user and type = single
        // (add field for history rated by)

        Object[] tOffset;
        if (offset != null && offset.length == 1) {
            tOffset = new Object[] { CommonUtil.decode(offset[0]) };
        } else {
            tOffset = new Object[] { "1900-01-01" };
        }

        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        if (profile.getPosition() != null) {
            List<ProfileDTO> profiles = likeRepository.findDoubleById(
                    profile.getId(),
                    UUID.fromString(id),
                    20,
                    step != null ? step : 5,
                    new String[] { "A", "F" },
                    tOffset);

            // http://dolszewski.com/spring/how-to-bind-requestparam-to-object/

            List<Object> lOffset;
            if (!profiles.isEmpty()) {
                lOffset = profiles.get(profiles.size() - 1).getOffset();
            } else {
                lOffset = Arrays.asList(tOffset);
            }

            return ResponseEntity.ok(new PageDTO<>(profiles, lOffset));
        } else {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
        }
    }

    @GetMapping("/rate_double/{id}/none")
    public ResponseEntity<?> doubleNone(
            @PathVariable String id,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset,
            Authentication auth) {
        // parameter ? man : woman, filter out type = double, filter out from, to field
        // is the session user and type = single
        // (add field for history rated by)

        Optional<Profile> selected = profileRepository.findById(UUID.fromString(id));

        Profile sProfile;
        if (selected.isPresent()) {
            sProfile = selected.get();
        } else {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
        }

        String sGender;
        if (sProfile.getGender().equals("w")) {
            sGender = "m";
        } else {
            sGender = "w";
        }

        Object[] tOffset;
        if (offset != null && offset.length == 4) {
            tOffset = new Object[] {
                    Double.valueOf(CommonUtil.decode(offset[0])),
                    Double.valueOf(CommonUtil.decode(offset[1])),
                    Double.valueOf(CommonUtil.decode(offset[2])),
                    CommonUtil.decode(offset[3])
            };
        } else {
            tOffset = new Object[] { 0, 0, 0, "1900-01-01" };
        }

        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        if (profile.getPosition() != null) {
            List<ProfileDTO> profiles = profileRepository.findProfileNoType(
                    CommonUtil.point(profile.getPosition()),
                    tOffset,
                    20,
                    step != null ? step : 5,
                    sProfile.getId(),
                    sGender,
                    profile.getGroup(),
                    profile.getId(),
                    0.0 // last parameter not used
            );

            // http://dolszewski.com/spring/how-to-bind-requestparam-to-object/

            List<Object> lOffset;
            if (!profiles.isEmpty()) {
                lOffset = profiles.get(profiles.size() - 1).getOffset();
            } else {
                lOffset = Arrays.asList(tOffset);
            }

            return ResponseEntity.ok(new PageDTO<>(profiles, lOffset));
        } else {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
        }
    }

    @GetMapping("/rate_both")
    public ResponseEntity<?> rateBoth(
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset,
            Authentication auth) {
        // parameter ? man : woman, filter out type = double, filter out from, to field
        // is the session user and type = single
        // (add field for history rated by)

        Object[] tOffset;
        if (offset != null && offset.length == 4) {
            tOffset = new Object[] {
                    Double.valueOf(CommonUtil.decode(offset[0])),
                    Double.valueOf(CommonUtil.decode(offset[1])),
                    Double.valueOf(CommonUtil.decode(offset[2])),
                    CommonUtil.decode(offset[3])
            };
        } else {
            tOffset = new Object[] { 0, 0, 0, "1900-01-01" };
        }

        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();
        String gender = profile.getGender().equals("m") ? "w" : "m";

        if (profile.getStatus().equals("F")) {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.friends_only"));
        }

        if (profile.getStatus().equals("I")) {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.invisible"));
        }

        if (profile.getPosition() != null) {
            List<ProfileDTO> profiles = profileRepository.findProfile(
                    CommonUtil.point(profile.getPosition()),
                    tOffset,
                    20,
                    step != null ? step : 5,
                    profile.getId(),
                    gender,
                    profile.getGroup(),
                    1.5);

            // http://dolszewski.com/spring/how-to-bind-requestparam-to-object/

            List<Object> lOffset;
            if (!profiles.isEmpty()) {
                lOffset = profiles.get(profiles.size() - 1).getOffset();
            } else {
                lOffset = Arrays.asList(tOffset);
            }

            return ResponseEntity.ok(new PageDTO<>(profiles, lOffset));
        } else {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
        }
    }

    @GetMapping("/rate_met")
    public ResponseEntity<PageDTO<ProfileDTO>> history(
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset,
            Authentication auth) {
        // parameter ? man : woman, filter out type = double, filter out from, to field
        // is the session user and type = single
        // (add field for history rated by)

        Object[] tOffset;
        if (offset != null && offset.length == 4) {
            tOffset = new Object[] {
                    CommonUtil.decode(offset[1]),
                    CommonUtil.decode(offset[2]),
                    CommonUtil.decode(offset[3])
            };
        } else {
            tOffset = new Object[] {
                    LocalDate.now().atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME),
                    "1900-01-01",
                    "1900-01-01"
            };
        }

        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        List<ProfileDTO> profiles = eventRepository.findProfileByEvent(
                profile.getId(),
                20,
                step != null ? step : 5,
                tOffset,
                "A");

        // http://dolszewski.com/spring/how-to-bind-requestparam-to-object/

        List<Object> lOffset;
        if (!profiles.isEmpty()) {
            lOffset = profiles.get(profiles.size() - 1).getOffset();
        } else {
            lOffset = Arrays.asList(tOffset);
        }

        PageDTO<ProfileDTO> pageDTO = new PageDTO<>(profiles, lOffset);
        pageDTO.setScroll(0);

        return ResponseEntity.ok(pageDTO);
    }

}
package com.raxim.myscoutee.profile.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.config.properties.ConfigProperties;
import com.raxim.myscoutee.common.util.ControllerUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Group;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Setting;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.data.dto.rest.SettingDTO;
import com.raxim.myscoutee.profile.data.dto.rest.StatDTO;
import com.raxim.myscoutee.profile.data.dto.rest.UserDTO;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;
import com.raxim.myscoutee.profile.service.SettingsService;
import com.raxim.myscoutee.profile.service.UserService;

@RestController
@RequestMapping("user")
public class UserRestController {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final LikeRepository likeRepository;
    private final SettingsService settingsService;
    private final ConfigProperties config;

    public UserRestController(ProfileRepository profileRepository,
            UserRepository userRepository,
            UserService userService,
            LikeRepository likeRepository,
            SettingsService settingsService,
            ConfigProperties config) {
        this.profileRepository = profileRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.settingsService = settingsService;
        this.config = config;
    }

    // user.profiles.name is the dropdownbox content, not the group name
    @PostMapping()
    public ResponseEntity<UserDTO> saveUser(Authentication auth, @RequestPart Group group) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        User user = principal.getUser();

        User userToSave = userRepository.findUserByEmail(auth.getName());

        Profile profile = userToSave.getProfiles().stream()
                .filter(p -> p.getGroup().equals(group.getId()))
                .findFirst().orElse(null);

        // user logged in to different profile
        if (profile != null && !profile.getId().equals(user.getProfile().getId())) {
            profile.setLastActive(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS));
            profileRepository.save(profile);
        }

        userToSave.setGroup(group.getId());
        userToSave.setProfile(profile);
        User userSaved = userRepository.save(userToSave);

        return ResponseEntity.ok(new UserDTO(userSaved));
    }

    @GetMapping()
    public ResponseEntity<UserDTO> getUser(Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        User user = principal.getUser();
        Profile profile = user.getProfile();

        profile.setLastActive(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS));
        profileRepository.save(profile);

        return ResponseEntity.ok(new UserDTO(user));
    }

    @GetMapping("/stats")
    public ResponseEntity<List<StatDTO>> getStats(Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        User user = principal.getUser();

        return ControllerUtil.handleList((u) -> userService.getStats(u),
                user.getId(), HttpStatus.OK);
    }

    @GetMapping("/settings")
    public ResponseEntity<SettingDTO> getSetting(Authentication auth, @RequestParam("key") String key) {
        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        return ControllerUtil.handle((i, k) -> settingsService.getSetting(i, k),
                profile.getId(), key,
                HttpStatus.OK);
    }

    @PostMapping("/settings")
    public ResponseEntity<SettingDTO> saveSetting(Authentication auth, @RequestParam("key") String key,
            @RequestBody Setting setting) {
        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        return ControllerUtil.handle((i, k, s) -> settingsService.saveSetting(i, k, s),
                profile.getId(), key, setting,
                HttpStatus.OK);
    }
}

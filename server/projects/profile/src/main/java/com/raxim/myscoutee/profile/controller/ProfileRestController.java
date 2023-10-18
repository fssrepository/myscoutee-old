package com.raxim.myscoutee.profile.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.LikeDTO;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.service.LikeService;

@RepositoryRestController("profiles")
public class ProfileRestController {

    private final ProfileRepository profileRepository;
    private final LikeService likeService;

    public ProfileRestController(ProfileRepository profileRepository,
            LikeService likeService) {
        this.profileRepository = profileRepository;
        this.likeService = likeService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileDTO> getProfile(@PathVariable String id) {
        Optional<Profile> profile = profileRepository.findById(UUID.fromString(id));
        if (profile.isPresent()) {
            ProfileDTO profileDto = new ProfileDTO(profile.get());
            return ResponseEntity.ok(profileDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/like")
    @Transactional
    public ResponseEntity<List<LikeDTO>> like(@RequestBody List<LikeDTO> pfLikes, Authentication auth) {
        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();

        List<LikeDTO> likesAll = likeService.saveLikes(profile, pfLikes);

        return ResponseEntity.ok(likesAll);
    }

}

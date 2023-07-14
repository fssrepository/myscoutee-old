package com.raxim.myscoutee.profile.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Group;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.data.dto.rest.ErrorDTO;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.repository.mongo.GroupRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;

@RepositoryRestController
@RequestMapping("recommendations")
public class RecommendationRestController {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ObjectMapper objectMapper;

    public RecommendationRestController(
            ProfileRepository profileRepository,
            UserRepository userRepository,
            GroupRepository groupRepository,
            ObjectMapper objectMapper) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.objectMapper = objectMapper;
    }

}

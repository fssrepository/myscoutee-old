package com.raxim.myscoutee.common.config.firebase;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.config.properties.ConfigProperties;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Group;
import com.raxim.myscoutee.profile.data.document.mongo.Link;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.repository.mongo.GroupRepository;
import com.raxim.myscoutee.profile.repository.mongo.LinkRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;

@Service
public class FirebaseService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ConfigProperties config;
    private final LinkRepository linkRepository;
    private final ObjectMapper objectMapper;

    public FirebaseService(
            ProfileRepository profileRepository,
            UserRepository userRepository,
            GroupRepository groupRepository,
            ConfigProperties config,
            LinkRepository linkRepository,
            ObjectMapper objectMapper) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.config = config;
        this.linkRepository = linkRepository;
        this.objectMapper = objectMapper;
    }

    public UserDetails loadUserById(String userid) {
        Optional<User> userOpt = this.userRepository.findById(UUID.fromString(CommonUtil.asUUID(userid)));
        if(userOpt.isPresent()) {
            User user = userOpt.get();
            String role = (user.getProfile() != null
                && user.getProfile().getRole() != null) ? user.getProfile().getRole()
                        : "U";
            return new FirebasePrincipal(user, role);
        }
        return null;
    }

    public UserDetails loadUserByUsername(String username, String xLink) {
        User user = this.userRepository.findUserByEmail(username);

        if (user == null) {
            List<Group> groups = this.groupRepository.findSystemGroups();

            List<Profile> profiles = groups.stream().map(group -> {
                Profile profile = new Profile(UUID.randomUUID());
                profile.setFirstName(group.getName());
                profile.setGroup(group.getId());

                String role = config.getAdminUser().equals(username) ? "A" : "U";
                profile.setRole(role);
                return profile;
            }).toList();

            List<Profile> savedProfiles = this.profileRepository.saveAll(profiles);

            User userToSave = new User();
            userToSave.setId(UUID.randomUUID());
            userToSave.setEmail(username);
            userToSave.setCreatedDate(new Date());
            userToSave.setGroup(savedProfiles.get(0).getGroup());
            userToSave.setProfile(savedProfiles.get(0));
            userToSave.setProfiles(new HashSet<>(savedProfiles));

            user = this.userRepository.save(userToSave);
        }

        // last active date needs to be saved on every call
        user.getProfile().setLastActive(LocalDateTime.now());
        this.profileRepository.save(user.getProfile());

        handleLink(username, xLink, user);

        String role = (user.getProfile() != null
                && user.getProfile().getRole() != null) ? user.getProfile().getRole()
                        : "U";

        return new FirebasePrincipal(user, role);
    }

    private void handleLink(String username, String xLink, User user) {
        if (xLink != null) {
            Link link = this.linkRepository.findByKey(UUID.fromString(xLink));
            if (link != null) {
                List<String> usedBy = link.getUsedBys().stream().filter(u -> Objects.equals(u, username))
                        .collect(Collectors.toList());
                if (usedBy.isEmpty()) {
                    switch (link.getType()) {
                        case "g":
                            Optional<Group> optionalGroup = this.groupRepository.findById(link.getRefId());
                            if (optionalGroup.isPresent()) {
                                Profile profile = new Profile();
                                profile.setGroup(optionalGroup.get().getId());
                                profile.setRole("U");
                                Profile lProfileSaved = this.profileRepository.save(profile);

                                User userNew = JsonUtil.clone(user, objectMapper);
                                userNew.getProfiles().add(profile);
                                userNew.setGroup(optionalGroup.get().getId());
                                userNew.setProfile(lProfileSaved);
                                this.userRepository.save(userNew);
                            }
                            break;
                        default:
                            break;
                    }

                    link.getUsedBys().add(username);
                    this.linkRepository.save(link);
                }
            }
        }
    }
}

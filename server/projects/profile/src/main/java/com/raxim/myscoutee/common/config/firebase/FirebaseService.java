package com.raxim.myscoutee.common.config.firebase;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.google.firebase.auth.FirebaseAuth;
import com.raxim.myscoutee.common.config.ConfigProperties;
import com.raxim.myscoutee.profile.data.document.mongo.Group;
import com.raxim.myscoutee.profile.data.document.mongo.Link;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Role;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.repository.mongo.GroupRepository;
import com.raxim.myscoutee.profile.repository.mongo.LinkRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.RoleRepository;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;

@Service
public class FirebaseService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final GroupRepository groupRepository;
    private final ConfigProperties config;
    private final LinkRepository linkRepository;

    public FirebaseService(
            ProfileRepository profileRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            GroupRepository groupRepository,
            ConfigProperties config,
            LinkRepository linkRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.groupRepository = groupRepository;
        this.config = config;
        this.linkRepository = linkRepository;
    }

    public FirebaseTokenHolder parseToken(String firebaseToken) {
        try {
            com.google.firebase.auth.FirebaseToken decodedToken = FirebaseAuth.getInstance()
                    .verifyIdToken(firebaseToken);
            return new FirebaseTokenHolder(decodedToken);
        } catch (Exception e) {
            throw new FirebaseTokenInvalidException(e.getMessage());
        }
    }

    public UserDetails loadUserByUsername(String username, String xLink) {
        User user = this.userRepository.findUserByEmail(username);
        List<Profile> profileSaved;

        if (user == null) {
            Group group;
            if (config.getAdminUser().equals(username)) {
                List<Group> groups = this.groupRepository.findSystemGroups();
                List<Profile> profiles = groups.stream()
                        .map(lGroup -> new Profile(lGroup.getId()))
                        .toList();

                profileSaved = this.profileRepository.saveAll(profiles);

                List<Role> roles = profileSaved.stream()
                        .map(profile -> new Role(UUID.randomUUID(), profile.getId(), "ROLE_ADMIN"))
                        .toList();

                this.roleRepository.saveAll(roles);
                group = groups.stream().filter(g -> g.getType().equals("b")).findFirst().orElse(null);
            } else {
                List<Group> groups = new ArrayList<>(this.groupRepository.findSystemGroups());
                List<Profile> profiles = groups.stream()
                        .map(lGroup -> new Profile(lGroup.getId()))
                        .toList();

                profileSaved = this.profileRepository.saveAll(profiles);

                List<Role> roles = profileSaved.stream()
                        .map(profile -> new Role(UUID.randomUUID(), profile.getId(), "ROLE_USER"))
                        .toList();
                this.roleRepository.saveAll(roles);

                group = groups.stream().filter(g -> g.getType().equals("d")).findFirst().orElse(null);
            }

            Profile profile = profileSaved.stream()
                    .filter(p -> Objects.equals(p.getGroup(), group.getId()))
                    .findFirst().orElse(null);

            User userToSave = new User(
                    UUID.randomUUID(),
                    username,
                    new Date(),
                    group.getId(),
                    profile,
                    new HashSet<>(profileSaved));
            user = this.userRepository.save(userToSave);
        }

        if (xLink != null) {
            Optional<Link> oLink = this.linkRepository.findByKey(UUID.fromString(xLink));
            if (oLink.isPresent()) {
                Link link = oLink.get();
                List<String> usedBy = link.getUsedBys().stream()
                        .filter(u -> u.equals(username))
                        .toList();

                if (usedBy.isEmpty()) {
                    if ("g".equals(link.getType())) {
                        Optional<Group> optionalGroup = this.groupRepository.findById(link.getRefId());
                        if (optionalGroup.isPresent()) {
                            Group group = optionalGroup.get();
                            Profile profile = new Profile(group.getId());
                            Profile lProfileSaved = this.profileRepository.save(profile);

                            Role role = new Role(UUID.randomUUID(), lProfileSaved.getId(), "ROLE_USER");
                            this.roleRepository.save(role);

                            user.getProfiles().add(profile);

                            User userNew = new User(user.getId(), user.getEmail(), user.getCreatedDate(), group.getId(),
                                    profile, user.getProfiles());
                            this.userRepository.save(userNew);
                        }
                    } else {
                        // Handle other types of links
                    }

                    link.getUsedBys().add(username);
                    this.linkRepository.save(link);
                }
            }
            ;
        }

        List<Role> roles = user.getProfile() != null
                ? this.roleRepository.findRoleByProfile(user.getProfile().getId())
                : List.of();

        return new FirebasePrincipal(user, roles);
    }
}

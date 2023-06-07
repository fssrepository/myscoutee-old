package com.raxim.myscoutee.common.config.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.raxim.myscoutee.common.config.ConfigProperties;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Role;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.repository.mongo.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;

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
            LinkRepository linkRepository
    ) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.groupRepository = groupRepository;
        this.config = config;
        this.linkRepository = linkRepository;
    }

    public UserDetails loadUserByUsername(String username, String xLink) {
        User user = this.userRepository.findUserByEmail(username);
        List<Profile> profileSaved;

        if (user == null) {
            Group group;
            if (config.getAdminUser().equals(username)) {
                List<Group> groups = this.groupRepository.findSystemGroups();
                List<Profile> profiles = new ArrayList<>();
                for (Group grp : groups) {
                    Profile profile = new Profile();
                    profile.setGroup(grp.getId());
                    profiles.add(profile);
                }

                profileSaved = this.profileRepository.saveAll(profiles);

                List<Role> roles = new ArrayList<>();
                for (Profile profile : profileSaved) {
                    Role role = new Role(UUID.randomUUID(), profile.getId(), "ROLE_ADMIN");
                    roles.add(role);
                }

                this.roleRepository.saveAll(roles);

                group = groups.stream().filter(g -> g.getType().equals("b")).findFirst().orElse(null);
            } else {
                List<Group> groups = new ArrayList<>(this.groupRepository.findSystemGroups());

                List<Profile> profiles = new ArrayList<>();
                for (Group grp : groups) {
                    Profile profile = new Profile();
                    profile.setGroup(grp.getId());
                    profiles.add(profile);
                }

                profileSaved = this.profileRepository.saveAll(profiles);

                List<Role> roles = new ArrayList<>();
                for (Profile profile : profileSaved) {
                    Role role = new Role(UUID.randomUUID(), profile.getId(), "ROLE_USER");
                    roles.add(role);
                }
                this.roleRepository.saveAll(roles);

                group = groups.stream().filter(g -> g.getType().equals("d")).findFirst().orElse(null);
            }

            Profile profile = profileSaved.stream().filter(p -> Objects.equals(p.getGroup(), group.getId())).findFirst().orElse(null);
            User userToSave = new User(UUID.randomUUID(), username, new Date(), group.getId(), profile, new HashSet<>(profileSaved));
            user = this.userRepository.save(userToSave);
        }

        if (xLink != null) {
            this.linkRepository.findByKey(UUID.fromString(xLink)).ifPresent(link -> {
                List<String> usedBy = link.getUsedBys().stream().filter(u -> Objects.equals(u, username)).collect(Collectors.toList());
                if (usedBy.isEmpty()) {
                    switch (link.getType()) {
                        case "g":
                            Optional<Group> optionalGroup = this.groupRepository.findById(link.getRefId());
                            if (optionalGroup.isPresent()) {
                                Profile profile = new Profile();
                                profile.setGroup(optionalGroup.get().getId());
                                Profile profileSaved = this.profileRepository.save(profile);

                                Role role = new Role(UUID.randomUUID(), profileSaved.getId(), "ROLE_USER");
                                this.roleRepository.save(role);

                                user.getProfiles().add(profile);
                                User userNew = user.withGroup(optionalGroup.get().getId()).withProfile(profile);
                                this.userRepository.save(userNew);
                            }
                            break;
                        default:
                            break;
                    }

                    link.getUsedBys().add(username);
                    this.linkRepository.save(link);
                }
            });
        }

        List<Role> roles = user.getProfile() != null ? this.roleRepository.findRoleByProfile(user.getProfile().getId()) : Collections.emptyList();

        return new FirebasePrincipal(user, roles);
    }
}

package com.raxim.myscoutee.profile.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Badge;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;
import com.raxim.myscoutee.profile.data.dto.rest.UserDTO;
import com.raxim.myscoutee.profile.repository.mongo.GroupRepository;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ProfileService profileService;
    private final LikeRepository likeRepository;
    private final GroupRepository groupRepository;

    public UserService(UserRepository userRepository, ProfileRepository profileRepository,
            ProfileService profileService, LikeRepository likeRepository, GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.profileService = profileService;
        this.likeRepository = likeRepository;
        this.groupRepository = groupRepository;
    }

    public Optional<UserDTO> changeStatus(User user, String groupId, String status) {
        User dbUser = this.userRepository.findById(user.getId()).get();

        UUID groupUuid = UUID.fromString(groupId);

        List<Profile> profiles = dbUser.getProfiles().stream()
                .filter(profile -> profile.getGroup().equals(groupUuid))
                .toList();
        if (!profiles.isEmpty()) {
            Profile profile = profiles.get(0);
            profile.setStatus(status);
            this.profileService.saveProfile(profile.getId().toString(), profile);

            List<Profile> activeProfiles = dbUser.getProfiles().stream()
                    .filter(p -> !"S".equals(p.getStatus()) || !"L".equals(p.getStatus())).toList();

            List<UUID> groupUuids = activeProfiles.stream().map(activeProfile -> activeProfile.getGroup()).toList();
            List<GroupDTO> groupDTOs = this.groupRepository.findAllById(groupUuids).stream()
                    .map(group -> new GroupDTO(group))
                    .toList();

            Profile currProfile = profile;
            if ("S".equals(status) || "L".equals(status)) {
                if (!activeProfiles.isEmpty()) {
                    currProfile = activeProfiles.get(0);
                } else {
                    currProfile = null;
                }
            }

            if (currProfile != null) {
                dbUser.setProfile(currProfile);
                dbUser.setGroup(dbUser.getProfile().getGroup());
                User userSaved = this.userRepository.save(dbUser);

                List<Badge> likes = this.likeRepository.getBadges(currProfile.getId(),
                        currProfile.getLastLogin().format(DateTimeFormatter.ISO_DATE_TIME));

                return Optional.of(new UserDTO(userSaved, groupDTOs, likes));
            }
        } else {
            if ("J".equals(status)) {
                Profile profileToSave = user.getProfile().clone();
                profileToSave.setId(UUID.randomUUID());
                profileToSave.setGroup(UUID.fromString(groupId));
                profileToSave.setStatus("P");
                profileToSave.setRole("U");
                Profile profileSaved = profileRepository.save(profileToSave);

                User userToSave = userRepository.findById(user.getId()).get();
                userToSave.getProfiles().add(profileSaved);
                userRepository.save(userToSave);
            }
        }

        return Optional.empty();
    }
}

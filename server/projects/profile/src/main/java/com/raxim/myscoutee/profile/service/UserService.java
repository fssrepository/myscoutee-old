package com.raxim.myscoutee.profile.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ProfileService profileService;
    private final LikeRepository likeRepository;

    public UserService(UserRepository userRepository, ProfileRepository profileRepository,
            ProfileService profileService, LikeRepository likeRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.profileService = profileService;
        this.likeRepository = likeRepository;
    }

    public List<GroupDTO> getGroupsByUser(UUID userId, PageParam pageParam) {
        return this.userRepository.findGroupsByUser(userId, pageParam);
    }

    // send notification
    public List<ProfileDTO> invite(String groupId, List<String> profileids, UUID createdBy) {
        List<UUID> profileUuids = profileids.stream().map(profileid -> UUID.fromString(profileid)).toList();

        List<User> users = this.userRepository.findUsersByProfiles(profileUuids);
        // create new profiles
        List<Profile> profilesToSave = new ArrayList<>();

        List<User> iUsers = users.stream().map(user -> {
            Profile iProfile = user.getProfile().clone();
            iProfile.setId(UUID.randomUUID());
            iProfile.setCreatedDate(LocalDateTime.now());
            iProfile.setCreatedBy(createdBy);
            iProfile.setRole("U");
            // invited
            iProfile.setStatus("INV");
            iProfile.setGroup(UUID.fromString(groupId));

            profilesToSave.add(iProfile);
            user.getProfiles().add(iProfile);
            return user;
        }).toList();

        List<Profile> profiles = this.profileRepository.saveAll(profilesToSave);

        this.userRepository.saveAll(iUsers);

        List<ProfileDTO> profileDTOs = profiles.stream().map(profile -> new ProfileDTO(profile)).toList();
        return profileDTOs;
    }

    public Optional<ProfileDTO> changeStatus(UUID profileId, String groupId, String status)
            throws IllegalAccessException {
        return changeStatus(profileId, groupId, status, null);
    }

    public Optional<ProfileDTO> changeStatus(UUID profileId, String groupId, String status, UUID byUuid)
            throws IllegalAccessException {

        Optional<Profile> optAdmin = Optional.empty();
        if (byUuid != null) {
            List<User> dbUsers = this.userRepository.findUsersByProfiles(List.of(byUuid));
            User dbUser = dbUsers.get(0);

            optAdmin = dbUser.getProfiles().stream()
                    .filter(profile -> byUuid.equals(profile.getId())
                            && "A".equals(profile.getRole()))
                    .findFirst();
        }

        List<User> dbUsers = this.userRepository.findUsersByProfiles(List.of(profileId));
        if ((byUuid != null && !optAdmin.isPresent())
                || dbUsers.isEmpty()) {
            throw new IllegalAccessException();
        }

        User dbUser = dbUsers.get(0);

        UUID groupUuid = UUID.fromString(groupId);

        List<Profile> profiles = dbUser.getProfiles().stream()
                .filter(profile -> profile.getGroup().equals(groupUuid))
                .toList();
        if (!profiles.isEmpty()) {
            Profile profile = profiles.get(0);
            profile.setStatus(status);
            if (byUuid != null) {
                profile.setUpdatedBy(byUuid);
            } else {
                profile.setUpdatedBy(profileId);
            }
            profile.setUpdatedDate(LocalDateTime.now());
            this.profileService.saveProfile(profile.getId().toString(), profile);

            List<Profile> activeProfiles = dbUser.getProfiles().stream()
                    .filter(p -> !"S".equals(p.getStatus()) || !"L".equals(p.getStatus())).toList();

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
                this.userRepository.save(dbUser);
                return Optional.of(new ProfileDTO(currProfile));
            }
        } else {
            if ("J".equals(status)) {
                Profile profileToSave = dbUser.getProfile().clone();
                profileToSave.setId(UUID.randomUUID());
                profileToSave.setGroup(UUID.fromString(groupId));
                profileToSave.setStatus("P");
                profileToSave.setRole("U");
                Profile profileSaved = profileRepository.save(profileToSave);

                User userToSave = userRepository.findById(dbUser.getId()).get();
                userToSave.getProfiles().add(profileSaved);
                userRepository.save(userToSave);
                return Optional.of(new ProfileDTO(profileSaved));
            }
        }

        return Optional.empty();
    }
}

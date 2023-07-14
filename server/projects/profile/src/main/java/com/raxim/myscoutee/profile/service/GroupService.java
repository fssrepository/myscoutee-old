package com.raxim.myscoutee.profile.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.profile.data.document.mongo.Group;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;
import com.raxim.myscoutee.profile.repository.mongo.GroupRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository, ProfileRepository profileRepository,
            UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    public Optional<GroupDTO> saveGroup(Group pGroup, User user) throws CloneNotSupportedException {
        Optional<Group> dbGroup = (pGroup.getId() != null) ? this.groupRepository.findById(pGroup.getId())
                : Optional.empty();
        if (dbGroup.isPresent()) {
            Group tGroup = dbGroup.get();
            Group group = (Group) pGroup.clone();
            group.setId(tGroup.getId());
            group.setCreatedBy(tGroup.getCreatedBy());
            group.setCreatedDate(tGroup.getCreatedDate());
            group.setPosition(tGroup.getPosition());

            Group groupSaved = this.groupRepository.save(pGroup);
            return Optional.of(new GroupDTO(groupSaved));
        } else {

            Profile profile = new Profile();
            profile.setId(UUID.randomUUID());
            profile.setRole("A");
            profile.setPosition(user.getProfile().getPosition());

            pGroup.setId(UUID.randomUUID());
            pGroup.setCreatedBy(profile.getId());
            pGroup.setCreatedDate(LocalDateTime.now());
            pGroup.setPosition(profile.getPosition());
            Group groupSaved = this.groupRepository.save(pGroup);

            profile.setGroup(groupSaved.getId());
            Profile profileSaved = this.profileRepository.save(profile);

            user.getProfiles().add(profileSaved);
            userRepository.save(user);

            return Optional.of(new GroupDTO(groupSaved));
        }
    }
}

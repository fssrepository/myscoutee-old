package com.raxim.myscoutee.profile.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mongodb.client.model.geojson.Point;
import com.raxim.myscoutee.profile.data.document.mongo.Group;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.GroupRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;
import com.raxim.myscoutee.profile.util.AppConstants;

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

    public List<GroupDTO> getAllGroups(PageParam pageParam, Point position, String access) {
        return this.groupRepository.findAllGroups(pageParam, position, access);
    }

    //recommendation
    public List<GroupDTO> getRecGroups(PageParam pageParam, Point position, UUID groupId) {
        return this.groupRepository.findRecGroups(pageParam, position, groupId);
    }

    public List<EventDTO> getEventsByGroup(UUID groupUuid, PageParam pageParam) {
        return this.groupRepository.findEventsByGroup(groupUuid, pageParam);
    }

    public List<ProfileDTO> getProfilesByGroup(UUID groupUuid, PageParam pageParam) {
        return this.groupRepository.findProfilesByGroup(groupUuid, pageParam);
    }

    public Optional<GroupDTO> joinGroup(UUID groupId, User user) throws CloneNotSupportedException {
        Optional<Group> dbGroup = (groupId != null) ? this.groupRepository.findById(groupId)
                : Optional.empty();
        if (dbGroup.isPresent()) {
            Group tGroup = dbGroup.get();

            Profile profile = new Profile();
            profile.setId(UUID.randomUUID());
            profile.setRole("A");
            profile.setPosition(user.getProfile().getPosition());
            profile.setGroup(tGroup.getId());

            Profile profileSaved = this.profileRepository.save(profile);

            user.getProfiles().add(profileSaved);
            userRepository.save(user);

            return Optional.of(new GroupDTO(tGroup));
        }

        return Optional.empty();
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
            Group groupSaved;

            pGroup.setId(UUID.randomUUID());
            pGroup.setCreatedBy(user.getProfile().getId());
            pGroup.setCreatedDate(LocalDateTime.now());
            pGroup.setPosition(user.getProfile().getPosition());

            if (AppConstants.SEPARATE_PROFILE.equals(pGroup.getType())) {
                groupSaved = this.groupRepository.save(pGroup);
            } else {
                pGroup.setGroup(user.getProfile().getGroup());
                groupSaved = this.groupRepository.save(pGroup);
            }

            Profile profile = new Profile();
            profile.setId(UUID.randomUUID());
            profile.setRole("A");
            profile.setPosition(user.getProfile().getPosition());
            profile.setGroup(groupSaved.getId());

            Profile profileSaved = this.profileRepository.save(profile);

            user.getProfiles().add(profileSaved);
            userRepository.save(user);

            return Optional.of(new GroupDTO(groupSaved));
        }
    }
}

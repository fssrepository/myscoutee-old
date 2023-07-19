package com.raxim.myscoutee.profile.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Like;
import com.raxim.myscoutee.profile.data.document.mongo.LikeGroup;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.exception.MessageException;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.util.AppConstants;

@Service
public class StatusService {
    private final ProfileRepository profileRepository;
    private final EventRepository eventRepository;
    private final LikeRepository likeRepository;

    public StatusService(
            ProfileRepository profileRepository,
            EventRepository eventRepository,
            LikeRepository likeRepository) {
        this.profileRepository = profileRepository;
        this.eventRepository = eventRepository;
        this.likeRepository = likeRepository;
    }

    public Optional<EventDTO> change(String id, String pProfileUid, String status)
            throws MessageException {
        return change(id, pProfileUid, status, null);
    }

    // approve, kick etc.
    public Optional<EventDTO> change(String id, String pProfileUid, String status, UUID byUuid)
            throws MessageException {
        Optional<Event> eventRes = id != null ? this.eventRepository.findById(UUID.fromString(id))
                : Optional.empty();

        UUID profileUid = UUID.fromString(pProfileUid);

        if (eventRes.isPresent()) {
            Event event = eventRes.get();

            Optional<Member> optCurrentMember = event.getMembers().stream()
                    .filter(member -> profileUid.equals(member.getProfile().getId())).findFirst();

            Optional<Member> optAdmin = Optional.empty();
            if (byUuid != null) {
                optAdmin = event.getMembers().stream()
                        .filter(member -> byUuid.equals(member.getProfile().getId())
                                && "A".equals(member.getRole()))
                        .findFirst();
            }

            if ((byUuid != null && !optAdmin.isPresent())
                    || !optCurrentMember.isPresent()) {
                throw new MessageException(AppConstants.ERR_ILLEGAL_ACCESS);
            }

            if (optCurrentMember.isPresent()) {
                Member currentMember = optCurrentMember.get();
                currentMember.setStatus(status);
                currentMember.setUpdatedDate(LocalDateTime.now());

                if (event.isPriority()) {
                    LikeGroup likeGroup = this.likeRepository.findLikeGroup(event.getCreatedBy(),
                            currentMember.getProfile().getId(), event.getId());
                    Like like = likeGroup.reduce();
                    currentMember.setScore(like.getRate() * like.getDistance());
                }
            } else {
                Set<String> allowedStatuses = Set.of("J", "PR");
                if (allowedStatuses.contains(status)) {
                    Optional<Profile> optProfile = profileRepository
                            .findById(profileUid);
                    if (optProfile.isPresent()) {
                        Member newMember = new Member();
                        Profile profile = optProfile.get();
                        newMember.setProfile(profile);
                        newMember.setUpdatedDate(LocalDateTime.now());
                        newMember.setCreatedDate(LocalDateTime.now());
                        newMember.setRole("U");
                        if (event.isAutoApprove() && event.isMultislot()) {
                            if ("J".equals(status)) {
                                status = "A";
                            }
                        }
                        newMember.setStatus(status);
                        event.getMembers().add(newMember);
                    }
                }
            }

            event.sync();

            Event dbEventItem = this.eventRepository.save(event);
            return Optional.of(new EventDTO(dbEventItem));
        }
        return Optional.empty();
    }
}

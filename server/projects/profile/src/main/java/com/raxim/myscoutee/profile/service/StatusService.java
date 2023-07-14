package com.raxim.myscoutee.profile.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.exception.IllegalAccessException;
import com.raxim.myscoutee.profile.exception.MessageException;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;

@Service
public class StatusService {
    private final ProfileRepository profileRepository;
    private final EventRepository eventRepository;

    public StatusService(
            ProfileRepository profileRepository,
            EventRepository eventRepository) {
        this.profileRepository = profileRepository;
        this.eventRepository = eventRepository;
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
                throw new IllegalAccessException();
            }

            if (optCurrentMember.isPresent()) {
                Member currentMember = optCurrentMember.get();
                currentMember.setStatus(status);
                currentMember.setUpdatedDate(LocalDateTime.now());
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

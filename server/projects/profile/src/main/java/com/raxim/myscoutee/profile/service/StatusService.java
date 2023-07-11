package com.raxim.myscoutee.profile.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.exception.IllegalAccessException;
import com.raxim.myscoutee.profile.exception.MessageException;
import com.raxim.myscoutee.profile.exception.PromotionFullException;
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

    // approve, kick etc.
    public Optional<EventDTO> change(String id, String profileUid, UUID byUuid, String status)
            throws MessageException {
        Optional<Event> eventRes = id != null ? this.eventRepository.findById(UUID.fromString(id))
                : Optional.empty();

        UUID memberId = UUID.fromString(profileUid);

        if (eventRes.isPresent()) {
            Event event = eventRes.get();

            Optional<Member> optCurrentMember = event.getMembers().stream()
                    .filter(member -> memberId.equals(member.getProfile().getId())).findFirst();

            Optional<Member> optAdmin = event.getMembers().stream()
                    .filter(member -> byUuid.equals(member.getProfile().getId())
                            && "A".equals(member.getRole()))
                    .findFirst();

            if (!optAdmin.isPresent() || !optCurrentMember.isPresent()) {
                throw new IllegalAccessException();
            }

            Member currentMember = optCurrentMember.get();
            currentMember.setStatus(status);
            currentMember.setUpdatedDate(LocalDateTime.now());

            event.sync();

            Event dbEventItem = this.eventRepository.save(event);
            return Optional.of(new EventDTO(dbEventItem));
        }
        return Optional.empty();
    }

    // promotion accept event -> message to participant, or cancel event
    public Optional<EventDTO> change(String id, UUID profileUid, String status)
            throws MessageException {

        Optional<Event> eventRes = id != null ? this.eventRepository.findById(UUID.fromString(id)) : Optional.empty();

        if (eventRes.isPresent()) {
            Event event = eventRes.get();

            if (event.getRef() != null
                    && ("A".equals(event.getAccess()) && Boolean.FALSE.equals(event.getAutoInvite()))) {
                Optional<Event> optPromotion = this.eventRepository.findById(event.getRef().getId());
                if (!optPromotion.isPresent()) {
                    throw new IllegalAccessException();
                }

                Event promotion = optPromotion.get();
                if (promotion.getNumOfEvents() == 0) {
                    throw new PromotionFullException();
                }

                /*
                 * promotion.sync();
                 * 
                 * this.promotionRepository.save(promotion);
                 */
            }

            Optional<Member> optCurrentMember = event.getMembers().stream()
                    .filter(member -> profileUid.equals(member.getProfile().getId())).findFirst();

            if (!"I".equals(event.getType())) {
                if (!optCurrentMember.isPresent()) {
                    throw new IllegalAccessException();
                }
            }

            if (optCurrentMember.isPresent()) {
                Member currentMember = optCurrentMember.get();
                currentMember.setStatus(status);
                currentMember.setUpdatedDate(LocalDateTime.now());
            } else {
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

            event.sync();
            Event dbEvent = eventRepository.save(event);

            // notification sent for any member with status A and any role (promoter also)

            return Optional.of(new EventDTO(dbEvent));
        }

        return Optional.empty();
    }
}

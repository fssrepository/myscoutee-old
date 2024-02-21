package com.raxim.myscoutee.profile.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mongodb.client.model.geojson.Point;
import com.raxim.myscoutee.algo.Fifa;
import com.raxim.myscoutee.profile.converter.Converters;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Match;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Result;
import com.raxim.myscoutee.profile.data.document.mongo.ScoreMatrix;
import com.raxim.myscoutee.profile.data.document.mongo.Token;
import com.raxim.myscoutee.profile.data.dto.rest.CloneDTO;
import com.raxim.myscoutee.profile.data.dto.rest.CodeDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.MatchDTO;
import com.raxim.myscoutee.profile.data.dto.rest.MemberDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.exception.MessageException;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.ScoreMatrixRepository;
import com.raxim.myscoutee.profile.util.AppConstants;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final ProfileRepository profileRepository;
    private final ScoreMatrixRepository scoreMatrixRepository;
    private final Converters converters;

    public EventService(EventRepository eventRepository,
            ProfileRepository profileRepository,
            Converters converters, ScoreMatrixRepository scoreMatrixRepository) {
        this.eventRepository = eventRepository;
        this.profileRepository = profileRepository;
        this.converters = converters;
        this.scoreMatrixRepository = scoreMatrixRepository;
    }

    public List<MemberDTO> getLeaderboard(String eventId) {
        Optional<Event> optEvent = eventRepository.findById(UUID.fromString(eventId));
        if (optEvent.isPresent()) {
            Event event = optEvent.get();
            List<MemberDTO> memberDTOs = event.getMembers().stream()
                    .sorted().map(member -> new MemberDTO(member))
                    .toList();
            return memberDTOs;
        }
        return List.of();
    }

    public List<MemberDTO> getMembersByEvent(PageParam pageParam, String eventId) {
        return eventRepository.findMembersByEvent(pageParam, UUID.fromString(eventId),
                new String[] { "A", "I", "J", "W" });
    }

    public Optional<MatchDTO> saveMatch(String id, Match pMatch) {
        Optional<Event> eventRes = id != null ? this.eventRepository.findById(UUID.fromString(id))
                : Optional.empty();

        if (eventRes.isPresent()) {
            Event event = eventRes.get();

            event.getMatches().stream()
                    .filter(match -> match.getId().equals(pMatch.getId()))
                    .forEach(match -> {
                        match.setScores(pMatch.getScores());
                    });

            if (event.getRule() != null
                    && event.getRule().getRankType() != null) {

                List<ScoreMatrix> scoreMatrices = this.scoreMatrixRepository.findByName(event.getRule().getRankType());

                // all matches what the memberId participated in.
                List<Match> matches = event.getMatches().stream()
                        .filter(match -> match.getMembers().stream()
                                .anyMatch(memberId -> pMatch.getMembers().contains(memberId)))
                        .toList();

                Fifa fifa = new Fifa(matches, scoreMatrices);
                Map<UUID, Result> results = fifa.calc();

                event.getMembers().stream()
                        .filter(member -> pMatch.getMembers().contains(member.getProfile().getId())).forEach(member -> {
                            member.setResult(results.get(member.getProfile().getId()));
                        });
            }

            this.eventRepository.save(event);

            return Optional.of(new MatchDTO(pMatch));
        }

        return Optional.empty();
    }

    public Optional<MemberDTO> score(String id, String pProfileUid, Double score, UUID byUuid) {
        Optional<Event> eventRes = id != null ? this.eventRepository.findById(UUID.fromString(id))
                : Optional.empty();

        UUID profileUid = UUID.fromString(pProfileUid);

        if (eventRes.isPresent()) {
            Event event = eventRes.get();

            Optional<Member> optCurrentMember = event.getMembers().stream()
                    .filter(member -> profileUid.equals(member.getProfile().getId())).findFirst();
            if (optCurrentMember.isPresent()) {
                Member member = optCurrentMember.get();
                member.setScore(score);
                event.getMembers().add(member);
                this.eventRepository.save(event);

                return Optional.of(new MemberDTO(member));
            }
        }
        return Optional.empty();
    }

    public List<EventDTO> getTemplates(PageParam pageParam) {
        return eventRepository.findTemplates(pageParam, new String[] { "A", "P", "C" });
    }

    public Optional<CodeDTO> getCodeByEvent(String eventId, UUID profileUuid) {
        UUID eventUUId = UUID.fromString(eventId);
        Optional<CodeDTO> code = this.eventRepository.findCodeByEvent(eventUUId, profileUuid);
        return code;
    }

    public Optional<MemberDTO> getMemberByCode(String eventId, String code) {
        UUID eventUUId = UUID.fromString(eventId);

        Optional<MemberDTO> member = this.eventRepository.findMemberByCode(eventUUId, code);
        return member;
    }

    /*
     * javascript
     * 
     * const isoOffsetDateTime = '2023-07-05T10:30:00+05:30'; // Example
     * ISO_OFFSET_DATE_TIME string
     * 
     * // Create a new Date object from the ISO_OFFSET_DATE_TIME string
     * const date = new Date(isoOffsetDateTime);
     * 
     * // Get the time zone offset in minutes
     * const timezoneOffsetMinutes = date.getTimezoneOffset();
     * 
     * // Convert the offset to hours and minutes
     * const offsetHours = Math.floor(Math.abs(timezoneOffsetMinutes) / 60);
     * const offsetMinutes = Math.abs(timezoneOffsetMinutes) % 60;
     * 
     * // Determine the sign of the offset
     * const sign = timezoneOffsetMinutes > 0 ? '-' : '+';
     * 
     * // Format the offset as a string (e.g., +05:30 or -05:00)
     * const offsetString =
     * `${sign}${padZero(offsetHours)}:${padZero(offsetMinutes)}`;
     * 
     * console.log('Offset String:', offsetString);
     * 
     * // Helper function to pad a number with leading zeros
     * function padZero(number) {
     * return number.toString().padStart(2, '0');
     * }
     */
    public List<EventDTO> getEvents(PageParam pageParam, String[] status) {
        List<EventDTO> events = Collections.emptyList();
        if (AppConstants.DAY.equals(pageParam.getType())) {
            if (pageParam.getDirection() == 1) {
                events = eventRepository.findEventDown(pageParam, status);
            } else {
                events = eventRepository.findEventUp(pageParam, status);
            }
        } else if (AppConstants.WEEK.equals(pageParam.getType())) {
            events = eventRepository.findEventDown(pageParam, status);
        } else if (AppConstants.MONTH.equals(pageParam.getType())) {
            events = eventRepository.findEventByMonth(pageParam, status);
        }
        return events;
    }

    public List<EventDTO> getRecEvents(PageParam pageParam, Point position, UUID groupId, String status) {
        return this.eventRepository.findRecEvents(pageParam, position, groupId, status);
    }

    public List<EventDTO> getInvitations(PageParam pageParam, String[] status) {
        return this.eventRepository.findInvitationDown(pageParam, status);
    }

    // check whether we clone inside a parent or not, clone event, where you are not
    // +41
    // a member
    public List<EventDTO> cloneBy(String eventId, Profile profile, CloneDTO cloneDTO)
            throws CloneNotSupportedException {
        UUID eventUid = UUID.fromString(eventId);
        List<Event> dbEvents = this.eventRepository.findParents(eventUid, 1);

        if (!dbEvents.isEmpty()) {
            Event event = dbEvents.get(0);

            List<Event> events = new ArrayList<>();

            Member newMember = new Member(profile);
            newMember.setUpdatedDate(LocalDateTime.now());
            newMember.setCreatedDate(LocalDateTime.now());
            newMember.setRole("A"); // Manager
            newMember.setStatus("A");

            events.add(event);
            for (int i = 0; i < cloneDTO.getNumberOfCopies(); i++) {
                Event clonedEvent = (Event) event.clone(profile);

                // clone event from promotion (for Group promotion)
                if (dbEvents.size() == 2) {
                    Event parent = dbEvents.get(1);
                    if (parent.getMembers().contains(newMember)) {
                        parent.getItems().add(clonedEvent);
                    } else {
                        // add Manager to an event, as he can invite members to it + lock/unlock ->
                        // status "A"/"P"
                        clonedEvent.getMembers().add(newMember);
                    }
                    events.add(parent);
                }
                events.addAll(clonedEvent.flatten());
            }

            events = this.eventRepository.saveAll(events);
            List<EventDTO> savedParents = events.stream()
                    .map(fEvent -> (EventDTO) converters.convert(fEvent).get())
                    .toList();

            return savedParents;
        } else {
            return List.of();
        }
    }

    public List<EventDTO> changeStatus(String eventId, String status) throws CloneNotSupportedException {
        UUID eventUuid = UUID.fromString(eventId);
        Optional<Event> dbEvent = this.eventRepository.findById(eventUuid);
        if (dbEvent.isPresent()) {
            List<Event> events = new ArrayList<>();

            Event event = dbEvent.get();

            if (event.getRef() != null) {
                List<Event> dbRefEvents = this.eventRepository.findParents(event.getRef(), 1);
                if (!dbRefEvents.isEmpty()) {
                    Event refEvent = null;
                    if ("A".equals(status)) {
                        refEvent = (Event) event.clone();
                        refEvent.setId(dbRefEvents.get(0).getId());
                    } else if ("P".equals(status)) { // unlock event
                        refEvent = dbRefEvents.get(0);
                        refEvent.setMembers(new HashSet<>());
                    }
                    events.add(refEvent);
                }

            }

            event.setStatus(status);
            events.add(event);

            List<EventDTO> savedParents = events.stream()
                    .map(fEvent -> (EventDTO) converters.convert(fEvent).get())
                    .toList();

            return savedParents;
        }
        return List.of();
    }

    public Optional<EventDTO> save(Profile profile, Event pEvent) throws MessageException {
        return save(profile, pEvent, null);
    }

    public Optional<EventDTO> save(Profile profile, Event pEvent, String parentId)
            throws MessageException {
        List<Event> dbEvents = this.eventRepository.findParents(pEvent.getId(), 2);

        if (!dbEvents.isEmpty()) {
            Event mainEvent = dbEvents.get(dbEvents.size() - 1);
            if ("A".equals(mainEvent.getStatus())) {
                throw new MessageException(AppConstants.ERR_NO_EDIT);
            }
        }

        List<Event> events = new ArrayList<>();

        try {
            Event lEvent = (Event) pEvent.clone();

            if (dbEvents.isEmpty()) {

                lEvent.setId(UUID.randomUUID());
                lEvent.setGroup(profile.getGroup());
                lEvent.setCreatedDate(LocalDateTime.now());
                lEvent.setCreatedBy(profile.getId());
                lEvent.setStatus(pEvent.getStatus());

                LocalDateTime lNow = LocalDateTime.now();

                Member newMember = new Member();
                newMember.setProfile(profile);
                newMember.setStatus("A");
                newMember.setUpdatedDate(lNow);
                newMember.setCreatedDate(lNow);
                newMember.setRole("A");
                lEvent.getMembers().add(newMember);
                lEvent.setNumOfMembers(lEvent.getMembers().size());

            } else {
                Event dbEvent = dbEvents.get(0);

                lEvent.setId(dbEvent.getId());
                lEvent.setGroup(dbEvent.getGroup());
                lEvent.setCreatedDate(dbEvent.getCreatedDate());
                lEvent.setCreatedBy(dbEvent.getCreatedBy());
                lEvent.setStatus(dbEvent.getStatus());
                lEvent.setParentId(dbEvent.getParentId());
                lEvent.setMembers(dbEvent.getMembers());

                lEvent.shift();
                lEvent.sync();

                if (dbEvents.size() > 1) {
                    Event dbParent = dbEvents.get(1);

                    // check whether it's participating in a clone, and if it's the same
                    // parent, change all the variable inside, and save both
                    List<Event> items = dbParent.getItems().stream()
                            .filter(item -> !item.getId().equals(dbEvent.getId())
                                    && (item.getId().equals(dbEvent.getRef())
                                            || (item.getRef() != null && item.getRef().equals(dbEvent.getRef()))))
                            .map(item -> {
                                Event tEvent;
                                try {
                                    tEvent = (Event) lEvent.clone();
                                    tEvent.setId(item.getId());
                                    return tEvent;
                                } catch (CloneNotSupportedException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }).filter(item -> item != null).toList();

                    if (!items.isEmpty()) {
                        dbParent.getItems().addAll(items);
                        events.addAll(dbParent.getItems());
                        dbParent.sync();
                        events.add(dbParent);
                    }
                }
            }

            if (parentId != null) {
                UUID tParentUuid = UUID.fromString(parentId);
                lEvent.setParentId(tParentUuid);

                List<Event> dbParents = this.eventRepository.findParents(tParentUuid, 2);

                if (dbParents.size() > 0) {
                    Event dbParent = dbParents.get(0);
                    dbParent.getItems().add(lEvent);
                    dbParent.sync();
                    events.add(dbParent);
                }
            }

            events.add(lEvent);

        } catch (CloneNotSupportedException e) {
            e.printStackTrace(); // logger is needed
            throw new MessageException(AppConstants.ERR_SAVE);
        }

        List<Event> savedEvents = this.eventRepository.saveAll(events);
        if (!savedEvents.isEmpty()) {
            Event tEvent = savedEvents.get(0);
            return converters.convert(tEvent).map(obj -> (EventDTO) obj);
        }
        return Optional.empty();
    }

    public List<EventDTO> getEventItems(PageParam pageParam, String eventId) {
        UUID eventUid = UUID.fromString(eventId);
        return eventRepository.findItemsByEvent(eventUid, pageParam);
    }

    public Optional<EventDTO> invite(String eventId, List<String> pProfileUuids, UUID byUuid)
            throws MessageException {
        Optional<Event> eventRes = eventId != null ? this.eventRepository.findById(UUID.fromString(eventId))
                : Optional.empty();

        if (eventRes.isPresent()) {
            Event dbEvent = eventRes.get();

            Optional<Member> optAdmin = dbEvent.getMembers().stream()
                    .filter(member -> byUuid.equals(member.getProfile().getId())
                            && "A".equals(member.getRole()))
                    .findFirst();

            if (!optAdmin.isPresent()) {
                throw new MessageException(AppConstants.ERR_ILLEGAL_ACCESS);
            }

            List<UUID> profileUUids = pProfileUuids.stream()
                    .map(UUID::fromString)
                    .filter(uuid -> dbEvent.getMembers().stream()
                            .noneMatch(member -> uuid.equals(member.getProfile().getId())))
                    .collect(Collectors.toList());

            List<Profile> profiles = this.profileRepository
                    .findAllById(profileUUids);

            // invited order does matter on member list screen
            Collections.sort(profiles, Comparator.comparing(Profile::getCreatedDate));

            AtomicReference<LocalDateTime> now = new AtomicReference<>(LocalDateTime.now());

            profiles.stream().forEach(profile -> {
                // mongodb precision is in millis
                LocalDateTime lNow = now.getAndSet(now.get().plus(1, ChronoUnit.MILLIS));

                String code = null;
                if (dbEvent.getTicket() != null
                        && dbEvent.getTicket().booleanValue()) {
                    code = UUID.randomUUID().toString();
                }

                Member newMember = new Member();
                newMember.setProfile(profile);
                newMember.setStatus("I");
                newMember.setUpdatedDate(lNow);
                newMember.setCreatedDate(lNow);
                newMember.setCode(code);
                newMember.setRole("U");
                dbEvent.getMembers().add(newMember);
            });

            eventRes = Optional.of(dbEvent);
        }

        if (eventRes.isPresent()) {
            Event lEvent = this.eventRepository.save(eventRes.get());
            return converters.convert(lEvent).map(obj -> (EventDTO) obj);
        }
        return Optional.empty();
    }
}

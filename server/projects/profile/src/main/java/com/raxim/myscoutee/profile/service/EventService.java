package com.raxim.myscoutee.profile.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.profile.converter.Converters;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Token;
import com.raxim.myscoutee.profile.data.dto.rest.CodeDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.MemberDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.exception.IllegalAccessException;
import com.raxim.myscoutee.profile.exception.MessageException;
import com.raxim.myscoutee.profile.handler.EventParamHandler;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.PromotionRepository;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final ProfileRepository profileRepository;
    private final Converters converters;

    public EventService(EventRepository eventRepository,
            PromotionRepository promotionRepository,
            ProfileRepository profileRepository,
            Converters converters) {
        this.eventRepository = eventRepository;
        this.profileRepository = profileRepository;
        this.converters = converters;
    }

    public List<EventDTO> getEventsByStatus(String[] tOffset, UUID group, String status) {
        return eventRepository.findEventsByStatus(group, 20, tOffset, status);
    }

    public List<Event> getAllActiveEvents(UUID[] refIds) {
        return eventRepository.findActiveEvents(new String[] { "A", "P" }, refIds);
    }

    public List<Token> getAllActiveTokens(UUID[] refIds) {
        return eventRepository.findTokensByEvent(refIds);
    }

    public List<MemberDTO> getMembersByEvent(PageParam pageParam, String eventId) {
        return eventRepository.findMembersByEvent(pageParam, UUID.fromString(eventId),
                new String[] { "A", "I", "J", "W" });
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
        if (EventParamHandler.DAY.equals(pageParam.getType())) {
            if (pageParam.getDirection() == 1) {
                events = eventRepository.findEventDown(pageParam, status);
            } else {
                events = eventRepository.findEventUp(pageParam, status);
            }
        } else if (EventParamHandler.WEEK.equals(pageParam.getType())) {
            events = eventRepository.findEventDown(pageParam, status);
        } else if (EventParamHandler.MONTH.equals(pageParam.getType())) {
            events = eventRepository.findEventByMonth(pageParam, status);
        }
        return events;
    }

    // TODO: promotion fix
    /*
     * public Optional<Event> cloneEvent(UUID eventId, Profile profile) {
     * Optional<Event> eventRes = eventRepository.findById(eventId);
     * 
     * if (eventRes.isPresent()) {
     * Event clonedEvent = EventUtil.cloneBy(eventRes.get(), profile, null, false,
     * objectMapper);
     * 
     * memberRepository.saveAll(clonedEvent.getInfo().getMembers());
     * eventItemRepository.saveAll(clonedEvent.getItems());
     * Event savedEvent = eventRepository.save(clonedEvent);
     * return Optional.of(savedEvent);
     * } else {
     * return Optional.empty();
     * }
     * }
     */

    // TODO: promotion fix
    /*
     * public Optional<EventDTO> recommendEvent(UUID eventId) {
     * Optional<Event> eventRes = eventRepository.findById(eventId);
     * 
     * if (eventRes.isPresent()) {
     * EventDTO eventDto;
     * 
     * Event event = eventRes.get();
     * List<EventItem> items = event.getItems().stream()
     * .filter(item -> !"pr".equals(item.getType()) &&
     * !"D".equals(item.getStatus()))
     * .map(item -> {
     * EventItem eventItem = JsonUtil.clone(item, objectMapper);
     * eventItem.setId(UUID.randomUUID());
     * return eventItem;
     * })
     * .collect(Collectors.toList());
     * event.setItems(eventItemRepository.saveAll(items));
     * 
     * event.getInfo().setMembers(new HashSet<>());
     * 
     * Event clonedEvent = JsonUtil.clone(event, objectMapper);
     * clonedEvent.setId(UUID.randomUUID());
     * clonedEvent.setStatus("U");
     * clonedEvent.setRef(event);
     * // clonedEvent.setCreatedDate(LocalDateTime.now());
     * 
     * Event savedEvent = eventRepository.save(clonedEvent);
     * 
     * LocalDate groupKey =
     * savedEvent.getInfo().getRange().getStart().toLocalDate();
     * Long sortKey =
     * savedEvent.getInfo().getRange().getStart().toInstant(ZoneOffset.UTC).
     * toEpochMilli();
     * 
     * eventDto = new EventDTO(savedEvent, groupKey, sortKey);
     * 
     * return Optional.of(eventDto);
     * } else {
     * return Optional.empty();
     * }
     * }
     */

    public Optional<EventDTO> saveEvent(Profile profile, Event pEvent) throws CloneNotSupportedException {
        Optional<Event> eventRes = pEvent.getId() != null ? this.eventRepository.findById(pEvent.getId())
                : Optional.empty();

        if (!eventRes.isPresent()) {
            Event lEvent = (Event) pEvent.clone();
            lEvent.setId(UUID.randomUUID());
            lEvent.setGroup(profile.getGroup());
            lEvent.setCreatedDate(LocalDateTime.now());
            lEvent.setCreatedBy(profile.getId());
            lEvent.setStatus(pEvent.getStatus());
            eventRes = Optional.of(lEvent);
        } else {
            Event dbEvent = eventRes.get();
            Event lEvent = (Event) pEvent.clone();
            lEvent.setId(dbEvent.getId());
            lEvent.setGroup(dbEvent.getGroup());
            lEvent.setCreatedDate(dbEvent.getCreatedDate());
            lEvent.setCreatedBy(dbEvent.getCreatedBy());
            lEvent.setStatus(dbEvent.getStatus());

            // it should be hierarchical, if the event has "List<Event> items", promotion is
            // two level deep
            lEvent.shift();
            lEvent.sync(); // if dbEvent.range.end < dbEvent.items.range.end -> revert back
                           // dbEvent.range.end change to dbEvent.items.range.end
            eventRes = Optional.of(lEvent);
        }

        if (eventRes.isPresent()) {
            Event lEvent = this.eventRepository.save(eventRes.get());
            return converters.convert(lEvent).map(obj -> (EventDTO) obj);
        }
        return Optional.empty();
    }

    public Optional<EventDTO> saveItem(Profile profile, String eventId, Event pEventItem)
            throws CloneNotSupportedException {
        Optional<Event> eventRes = eventId != null ? this.eventRepository.findById(UUID.fromString(eventId))
                : Optional.empty();

        if (eventRes.isPresent()) {
            Event dbEvent = eventRes.get();

            Optional<Event> eventItemRes = dbEvent.getItems().stream()
                    .filter(item -> pEventItem.getId().equals(item.getId()))
                    .findFirst();

            Event lEventItem = (Event) pEventItem.clone();
            UUID eventItemId;
            if (!eventItemRes.isPresent()) {
                eventItemId = UUID.randomUUID();
                lEventItem.setId(eventItemId);
                lEventItem.setCreatedDate(LocalDateTime.now());
                lEventItem.setCreatedBy(profile.getId());
                lEventItem.setStatus(lEventItem.getNumOfMembers() >= lEventItem.getCapacity().getMin() ? "A" : "P");
            } else {
                Event dbEventItem = eventItemRes.get();

                eventItemId = dbEventItem.getId();
                lEventItem.setId(eventItemId);
                lEventItem.setCreatedDate(dbEventItem.getCreatedDate());
                lEventItem.setCreatedBy(dbEventItem.getCreatedBy());

                if (!"D".equals(pEventItem.getStatus())) {
                    lEventItem.setStatus(dbEventItem.getStatus());
                } else {
                    lEventItem.setStatus("D");
                }

                // it should be hierarchical, if the event has "List<Event> items", promotion is
                // two level deep
                dbEvent.sync();

            }
            dbEvent.getItems().add(lEventItem);

            List<Event> eventItems = eventRepository.saveAll(dbEvent.getItems());

            dbEvent.setItems(eventItems);
            dbEvent = eventRepository.save(dbEvent);

            Optional<Event> dbEventItem = dbEvent.getItems().stream()
                    .filter(item -> item.getId().equals(eventItemId))
                    .findFirst();

            if (dbEventItem.isPresent()) {
                Event tEventItem = dbEventItem.get();
                return converters.convert(tEventItem).map(obj -> (EventDTO) obj);
            }
        }

        return Optional.empty();
    }

    public List<EventDTO> getEventItems(PageParam pageParam, UUID eventId) {
        return eventRepository.findItemsByEvent(eventId, pageParam);
    }

    public Optional<EventDTO> inviteMembersForEvent(String eventId, List<String> pProfileUuids, UUID byUuid)
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
                throw new IllegalAccessException();
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

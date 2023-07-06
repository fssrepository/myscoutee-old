package com.raxim.myscoutee.profile.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.profile.converter.Converters;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.EventItem;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Token;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventItemDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.handler.EventParamHandler;
import com.raxim.myscoutee.profile.repository.mongo.EventItemRepository;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.MemberRepository;
import com.raxim.myscoutee.profile.repository.mongo.PromotionRepository;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final EventItemRepository eventItemRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;
    private final Converters converters;

    public EventService(EventRepository eventRepository,
            EventItemRepository eventItemRepository,
            PromotionRepository promotionRepository,
            MemberRepository memberRepository,
            ObjectMapper objectMapper, Converters converters) {
        this.eventRepository = eventRepository;
        this.eventItemRepository = eventItemRepository;
        this.memberRepository = memberRepository;
        this.objectMapper = objectMapper;
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

    // TODO: to be fixed
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

    // TODO: to be fixed
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

    public Optional<EventDTO> save(Profile profile, Event pEvent) throws CloneNotSupportedException {
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

            lEvent.shift();
            lEvent.sync(); // if dbEvent.range.end < dbEvent.items.range.end -> revert back
                           // dbEvent.range.end change to dbEvent.items.range.end
            eventRes = Optional.of(lEvent);
        }

        if (eventRes.isPresent()) {
            Event lEvent = this.eventRepository.save(eventRes.get());
            Optional.of((EventDTO) converters.convert(lEvent));
        }
        return Optional.empty();
    }

    public Optional<EventItemDTO> saveItem(Profile profile, String eventId, EventItem pEventItem)
            throws CloneNotSupportedException {
        Optional<Event> eventRes = eventId != null ? this.eventRepository.findById(UUID.fromString(eventId))
                : Optional.empty();

        if (eventRes.isPresent()) {
            Event dbEvent = eventRes.get();

            Optional<EventItem> eventItemRes = dbEvent.getItems().stream()
                    .filter(item -> pEventItem.getId().equals(item.getId()))
                    .findFirst();

            UUID eventItemId;
            if (!eventItemRes.isPresent()) {
                EventItem lEventItem = (EventItem) pEventItem.clone();
                eventItemId = UUID.randomUUID();
                lEventItem.setId(eventItemId);
                lEventItem.setCreatedDate(LocalDateTime.now());
                lEventItem.setCreatedBy(profile.getId());
                lEventItem.setStatus(lEventItem.getNum() >= lEventItem.getCapacity().getMin() ? "A" : "P");
                dbEvent.getItems().add(lEventItem);
            } else {
                EventItem lEventItem = (EventItem) pEventItem.clone();
                EventItem dbEventItem = eventItemRes.get();

                eventItemId = dbEventItem.getId();
                lEventItem.setId(eventItemId);
                lEventItem.setCreatedDate(dbEventItem.getCreatedDate());
                lEventItem.setCreatedBy(dbEventItem.getCreatedBy());

                if (!"D".equals(pEventItem.getStatus())) {
                    lEventItem.setStatus(dbEventItem.getStatus());
                } else {
                    lEventItem.setStatus("D");
                }

                // event
                dbEvent.sync();
                dbEvent.getItems().add(lEventItem);
            }

            Set<EventItem> eventItems = new HashSet<>(eventItemRepository.saveAll(dbEvent.getItems()));

            dbEvent.setItems(eventItems);
            dbEvent = eventRepository.save(dbEvent);

            Optional<EventItem> dbEventItem = dbEvent.getItems().stream()
                    .filter(item -> item.getId().equals(eventItemId))
                    .findFirst();

            if (dbEventItem.isPresent()) {
                EventItem lEventItem = dbEventItem.get();
                return Optional.of((EventItemDTO) converters.convert(lEventItem));
            }
        }

        return Optional.empty();
    }

    public List<EventItemDTO> getEventItems(UUID eventId, Integer step, Object[] tOffset, UUID profileId) {
        return eventRepository.findItemsByEvent(eventId, 20, step != null ? step : 5, "%Y-%m-%d", profileId, tOffset);
    }
}

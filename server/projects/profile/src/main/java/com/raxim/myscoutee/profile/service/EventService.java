package com.raxim.myscoutee.profile.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.EventItem;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.RangeLocal;
import com.raxim.myscoutee.profile.data.document.mongo.Token;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventItemDTO;
import com.raxim.myscoutee.profile.repository.mongo.EventItemRepository;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.MemberRepository;
import com.raxim.myscoutee.profile.repository.mongo.PromotionRepository;
import com.raxim.myscoutee.profile.util.EventUtil;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final EventItemRepository eventItemRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    public EventService(EventRepository eventRepository,
            EventItemRepository eventItemRepository,
            PromotionRepository promotionRepository,
            MemberRepository memberRepository,
            ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.eventItemRepository = eventItemRepository;
        this.memberRepository = memberRepository;
        this.objectMapper = objectMapper;
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

    public List<EventDTO> getEvents(String step, Integer direction, String[] tOffset,
            UUID profileId, String[] status) {
        List<EventDTO> events = Collections.emptyList();

        if (step == null || step.equals("d")) {
            if (direction == 1) {
                events = eventRepository.findEventDown(profileId, 20, 5, "%Y-%m-%d", status, tOffset, "A");
            } else {
                events = eventRepository.findEventUp(profileId, 20, 5, "%Y-%m-%d", status, tOffset, "A");
            }
        } else {
            if (step.equals("w")) {
                events = eventRepository.findEventDown(profileId, 20, 5, "%Y %U", status, tOffset, "A");
            } else if (step.equals("m")) {
                LocalDateTime from = LocalDate.parse(tOffset[0], DateTimeFormatter.ISO_DATE_TIME)
                        .withDayOfMonth(1).atStartOfDay();
                LocalDateTime until = from.plusMonths(1);

                events = eventRepository.findEventByMonth(profileId, 20, 5, "%Y-%m", status,
                        until.format(DateTimeFormatter.ISO_DATE_TIME), tOffset, "A");

                events = events.stream().map(event -> {
                    String[] dateParts = event.getGroupKey().toString().split("-");
                    event.setGroupKey(dateParts[0] + " " + CommonUtil.months[Integer.parseInt(dateParts[1]) - 1]);

                    RangeLocal range = event.getEvent().getInfo().getRange();
                    if (range != null) {
                        LocalDateTime start = range.getStart();
                        if (start.isBefore(from)) {
                            start = from;
                        }
                        LocalDateTime end = range.getEnd();
                        if (end.isAfter(until)) {
                            end = until.minusSeconds(1);
                        }
                        event.getEvent().getInfo().setRange(new RangeLocal(start, end));
                    }
                    return event;
                }).collect(Collectors.toList());
            }
        }
        return events;
    }

    public Optional<Event> cloneEvent(UUID eventId, Profile profile) {
        Optional<Event> eventRes = eventRepository.findById(eventId);

        if (eventRes.isPresent()) {
            Event clonedEvent = EventUtil.cloneBy(eventRes.get(), profile, null, false, objectMapper);

            memberRepository.saveAll(clonedEvent.getInfo().getMembers());
            eventItemRepository.saveAll(clonedEvent.getItems());
            Event savedEvent = eventRepository.save(clonedEvent);
            return Optional.of(savedEvent);
        } else {
            return Optional.empty();
        }
    }

    public Optional<EventDTO> recommendEvent(UUID eventId) {
        Optional<Event> eventRes = eventRepository.findById(eventId);

        if (eventRes.isPresent()) {
            EventDTO eventDto;

            Event event = eventRes.get();
            List<EventItem> items = event.getItems().stream()
                    .filter(item -> !"pr".equals(item.getType()) && !"D".equals(item.getStatus()))
                    .map(item -> {
                        EventItem eventItem = JsonUtil.clone(item, objectMapper);
                        eventItem.setId(UUID.randomUUID());
                        return eventItem;
                    })
                    .collect(Collectors.toList());
            event.setItems(eventItemRepository.saveAll(items));

            event.getInfo().setMembers(new HashSet<>());

            Event clonedEvent = JsonUtil.clone(event, objectMapper);
            clonedEvent.setId(UUID.randomUUID());
            clonedEvent.setStatus("U");
            clonedEvent.setRef(event);
            // clonedEvent.setCreatedDate(LocalDateTime.now());

            Event savedEvent = eventRepository.save(clonedEvent);

            LocalDate groupKey = savedEvent.getInfo().getRange().getStart().toLocalDate();
            Long sortKey = savedEvent.getInfo().getRange().getStart().toInstant(ZoneOffset.UTC).toEpochMilli();

            eventDto = new EventDTO(savedEvent, groupKey, sortKey);

            return Optional.of(eventDto);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Event> getEvent(EventItem eventItem, Profile profile, String status) {
        return getEvent(eventItem, profile, status, null, false);
    }

    public Optional<Event> getEvent(EventItem eventItem, Profile profile, String status,
            UUID eventId, boolean isUpdate) {
        Optional<Event> eventRes = eventId != null ? eventRepository.findById(eventId) : Optional.empty();

        if (eventRes.isPresent()) {
            Event event = eventRes.get();

            if (isUpdate) {
                if (eventItem.getId() != null) {
                    EventItem item = event.getItems().stream()
                            .filter(i -> i.getId().equals(eventItem.getId()))
                            .findFirst()
                            .orElse(null);
                    if (item != null) {
                        return Optional.of(event);
                    } else {
                        return Optional.empty();
                    }
                } else {
                    return Optional.empty();
                }
            } else {
                return Optional.of(event);
            }
        } else {
            if (!isUpdate) {
                Event event = new Event();
                event.setInfo(eventItem);
                event.setPosition(eventItem.getPosition());
                event.setGroup(profile.getGroup());
                event.setStatus(status);
                event.setCreatedBy(profile.getId());
                // event.setCreatedDate(LocalDateTime.now());
                return Optional.of(event);
            } else {
                return Optional.empty();
            }
        }
    }

    public Optional<Pair<Event, EventItem>> saveEvent(Event pEvent, EventItem pEventItem) {
        Event event = pEvent;
        EventItem eventItem = event.getItems().stream()
                .filter(item -> pEventItem.getId().equals(item.getId()))
                .findFirst()
                .orElse(null);
        if (eventItem == null) {
            event.getItems().add(pEventItem);
        }

        event.getItems().sort(Comparator.comparing(item -> item.getRange().getStart()));
        event.setPositions(event.getItems().stream().map(EventItem::getPosition).collect(Collectors.toList()));

        event = EventUtil.shiftBy(event, pEventItem, objectMapper);

        List<EventItem> eventItems = eventItemRepository.saveAll(event.getItems());

        event = JsonUtil.clone(event, objectMapper);
        event.setItems(eventItems);

        event = eventRepository.save(event);
        eventItem = eventItems.stream()
                .filter(item -> item.getId().equals(pEventItem.getId()))
                .findFirst()
                .orElse(null);

        return Optional.of(Pair.of(event, eventItem));
    }

    public List<EventItemDTO> getEventItems(UUID eventId, Integer step, Object[] tOffset, UUID profileId) {
        return eventRepository.findItemsByEvent(eventId, 20, step != null ? step : 5, "%Y-%m-%d", profileId, tOffset);
    }
}

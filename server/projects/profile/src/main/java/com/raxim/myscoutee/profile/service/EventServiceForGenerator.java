package com.raxim.myscoutee.profile.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.EventItem;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.RangeLocal;
import com.raxim.myscoutee.profile.repository.mongo.EventItemRepository;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.util.AppConstants;

@Service
public class EventServiceForGenerator {
        private final EventRepository eventRepository;
        private final EventItemRepository eventItemRepository;

        public EventServiceForGenerator(EventRepository eventRepository,
                        EventItemRepository eventItemRepository) {
                this.eventRepository = eventRepository;
                this.eventItemRepository = eventItemRepository;
        }

        // random events - generator service
        public void saveEvents(List<Set<Profile>> groups) {
                List<List<Member>> membersByGroup = groups.stream()
                                .map(profiles -> profiles.stream()
                                                .map(profile -> new Member(profile, "A", "U"))
                                                .collect(Collectors.toList()))
                                .collect(Collectors.toList());

                List<EventItem> eventItems = membersByGroup.stream()
                                .map(members -> {
                                        EventItem eventItem = new EventItem();
                                        eventItem.setId(UUID.randomUUID());
                                        eventItem.setType("g");
                                        eventItem.setCategory("l");
                                        eventItem.setName("Generated Event!");
                                        eventItem.setDesc("Generated Event for strangers!");
                                        eventItem.setMembers(new HashSet<>(members));
                                        eventItem.setCreatedBy(AppConstants.UUID_SYSTEM);
                                        eventItem.setStatus("A");

                                        LocalDateTime fromDT = LocalDateTime.now()
                                                        .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                                                        .withHour(21);
                                        LocalDateTime toDT = fromDT.plusHours(3);
                                        RangeLocal range = new RangeLocal(fromDT, toDT);
                                        eventItem.setRange(range);
                                        return eventItem;
                                })
                                .collect(Collectors.toList());

                List<EventItem> eventItemsSaved = eventItemRepository.saveAll(eventItems);

                List<Event> events = eventItemsSaved.stream()
                                .map(eventItemSaved -> {
                                        Event event = new Event();
                                        event.setId(UUID.randomUUID());
                                        event.setInfo(eventItemSaved);
                                        event.setItems(new ArrayList<>(Collections.singletonList(eventItemSaved)));
                                        event.setStatus("A");
                                        return event;
                                })
                                .collect(Collectors.toList());

                eventRepository.saveAll(events);
        }
}

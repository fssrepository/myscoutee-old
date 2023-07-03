package com.raxim.myscoutee.profile.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.RangeLocal;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.util.AppConstants;

@Service
public class EventServiceForGenerator {
        private final EventRepository eventRepository;

        public EventServiceForGenerator(EventRepository eventRepository) {
                this.eventRepository = eventRepository;
        }

        // random events - generator service
        public void saveEvents(List<Set<Profile>> groups) {
                List<List<Member>> membersByGroup = groups.stream()
                                .map(profiles -> profiles.stream()
                                                .map(profile -> new Member(profile, "A", "U"))
                                                .collect(Collectors.toList()))
                                .collect(Collectors.toList());

                List<Event> events = membersByGroup.stream()
                                .map(members -> {
                                        Event event = new Event();
                                        event.setId(UUID.randomUUID());
                                        event.setType("P");
                                        event.setCategory("l");

                                        event.setName("Generated Event!");
                                        event.setDesc("Generated Event for strangers!");
                                        event.setMembers(new HashSet<>(members));
                                        event.setCreatedBy(AppConstants.UUID_SYSTEM);
                                        event.setStatus("A");

                                        event.setGroup(members.get(0).getProfile().getGroup());

                                        LocalDateTime fromDT = LocalDateTime.now()
                                                        .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                                                        .withHour(21);
                                        LocalDateTime toDT = fromDT.plusHours(3);
                                        RangeLocal range = new RangeLocal(fromDT, toDT);
                                        event.setRange(range);
                                        return event;
                                })
                                .collect(Collectors.toList());

                eventRepository.saveAll(events);
        }
}

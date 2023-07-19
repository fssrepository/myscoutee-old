package com.raxim.myscoutee.profile.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.service.iface.IEventGeneratorService;
import com.raxim.myscoutee.profile.util.AppConstants;

@Service
public class EventGeneratorService implements IEventGeneratorService {
    private final EventRepository eventRepository;

    public EventGeneratorService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public List<Event> generate() {
        List<Event> events = this.eventRepository.findEvents();

        List<Event> handledEvents = events.stream().map(event -> {
            event.syncStatus();

            int maxStage = event.getMaxStage();
            if (maxStage > 0 && "A".equals(event.getStatus())) {
                List<Event> dueNextStageEvents = event.getDueNextStageEvents();
                if (!dueNextStageEvents.isEmpty()) {
                    int nextStage = event.getStage() + 1;
                    event.setStage(nextStage);
                }

                int maxCapacity = dueNextStageEvents.stream().mapToInt(item -> item.getCapacity().getMax()).sum();

                List<Event> currStageEvents = event.getItems().stream()
                        .filter(item -> "A".equals(item.getStatus()) && item.getStage() == event.getStage()).toList();

                int firstXWinner = (int) Math.ceil((double) maxCapacity / currStageEvents.size());
                if (event.getRule() != null) {

                    List<Member> winners = currStageEvents.stream().flatMap(cItem -> {
                        Double rateMin = cItem.getRule() != null ? cItem.getRule().getRate() : 0d;

                        List<Member> members = cItem.getMembers().stream()
                                .filter(member -> "A".equals(member.getStatus())
                                        && "U".equals(member.getRole())
                                        && (!cItem.isPriority() || member.getScore() >= rateMin))
                                .sorted(Comparator.comparing(Member::getScore)
                                        .thenComparing(Member::getCreatedDate))
                                .limit(firstXWinner)
                                .toList();

                        return members.stream();
                    }).toList();

                    event.assignToSlots(winners);
                }
            }
            return event;
        }).toList();

        List<Event> eventsToSave = handledEvents.stream().flatMap(event -> event.flatten().stream()).toList();

        List<Event> savedEvents = this.eventRepository.saveAll(eventsToSave);
        return savedEvents;
    }

}

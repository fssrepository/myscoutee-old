package com.raxim.myscoutee.profile.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.algo.dto.FGraph;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.generator.EventGeneratorByPriority;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.service.iface.IEventGeneratorService;
import com.raxim.myscoutee.profile.util.LikesWrapper;

/*
 * who has already met, invited by priority
 */
@Service
public class EventGeneratorByPriorityService implements IEventGeneratorService {
    private final EventRepository eventRepository;
    private final LikeRepository likeRepository;

    public EventGeneratorByPriorityService(EventRepository eventRepository, LikeRepository likeRepository) {
        this.eventRepository = eventRepository;
        this.likeRepository = likeRepository;
    }

    public List<Event> generate(String flags) {
        List<Event> events = this.eventRepository.findEventsWithCandidates();

        LikesWrapper lw = this.likeRepository.findAllByGroup();
        FGraph fGraph = lw.fGraph(Set.of("A", "F"), events);
        Map<String, Profile> profiles = lw.profiles();

        EventGeneratorByPriority eventGeneratorByPriority = new EventGeneratorByPriority(events,
                fGraph, profiles);
        List<Event> eventsToSave = eventGeneratorByPriority.generate(flags);

        List<Event> savedEvents = this.eventRepository.saveAll(eventsToSave);
        return savedEvents;
    }
}

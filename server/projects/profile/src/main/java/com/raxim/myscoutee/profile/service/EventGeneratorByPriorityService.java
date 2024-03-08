package com.raxim.myscoutee.profile.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.algo.dto.ObjGraph;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.EventWithCandidates;
import com.raxim.myscoutee.profile.filter.ProfileObjGraphFilter;
import com.raxim.myscoutee.profile.generator.EventGeneratorByPriority;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.service.iface.IEventGeneratorService;

/*
 * who has already met, invited by priority
 */
@Service
public class EventGeneratorByPriorityService implements IEventGeneratorService {
    private final EventRepository eventRepository;
    private final ProfileObjGraphFilter profileObjGraphFilter;

    public EventGeneratorByPriorityService(EventRepository eventRepository,
            ProfileObjGraphFilter profileObjGraphFilter) {
        this.eventRepository = eventRepository;
        this.profileObjGraphFilter = profileObjGraphFilter;
    }

    public List<Event> generate(ObjGraph filteredEdges, String flags) {
        List<EventWithCandidates> eventWithCandidates = this.eventRepository.findEventsWithCandidates();

        EventGeneratorByPriority eventGeneratorByPriority = new EventGeneratorByPriority(eventWithCandidates,
                profileObjGraphFilter,
                filteredEdges, flags);
        List<Event> eventsToSave = eventGeneratorByPriority.generate();

        List<Event> savedEvents = this.eventRepository.saveAll(eventsToSave);
        return savedEvents;
    }
}

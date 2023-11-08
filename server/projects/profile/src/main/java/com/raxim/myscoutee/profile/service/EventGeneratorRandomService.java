package com.raxim.myscoutee.profile.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.algo.dto.Range;
import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.dto.FilteredEdges;
import com.raxim.myscoutee.profile.generator.EventGeneratorByRandom;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.service.iface.IEventGeneratorService;

/*
 * the members did not met witheach other before
 */
@Service
public class EventGeneratorRandomService implements IEventGeneratorService {

    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;

    public EventGeneratorRandomService(EventRepository eventRepository, ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
    }

    // only profiles with status is 'A'
    public List<Event> generate(FilteredEdges filteredEdges, String flags) {
        Range lFlags = new Range(6, 12);
        if (flags != null) {
            lFlags = JsonUtil.jsonToObject(flags, Range.class, objectMapper);
        }

        EventGeneratorByRandom eventGeneratorByRandom = new EventGeneratorByRandom(filteredEdges, lFlags);
        List<Event> eventsToSave = eventGeneratorByRandom.generate();

        List<Event> savedEvents = this.eventRepository.saveAll(eventsToSave);
        return savedEvents;
    }
}

package com.raxim.myscoutee.profile.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.algo.dto.FGraph;
import com.raxim.myscoutee.algo.dto.Range;
import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.generator.EventGeneratorByRandom;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.service.iface.IEventGeneratorService;
import com.raxim.myscoutee.profile.util.LikesWrapper;

/*
 * the members did not met witheach other before
 */
@Service
public class EventGeneratorRandomService implements IEventGeneratorService {

    private final EventRepository eventRepository;
    private final LikeRepository likeRepository;
    private final ObjectMapper objectMapper;

    public EventGeneratorRandomService(EventRepository eventRepository, LikeRepository likeRepository,
            ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.likeRepository = likeRepository;
        this.objectMapper = objectMapper;
    }

    // only profiles with status is 'A'
    public List<Event> generate(String flags) {
        Range lFlags = new Range(6, 12);
        if (flags != null) {
            lFlags = JsonUtil.jsonToObject(flags, Range.class, objectMapper);
        }

        LikesWrapper lw = this.likeRepository.findAllByGroup();
        FGraph fGraph = lw.fGraph(Set.of("A"));
        Map<String, Profile> profiles = lw.profiles();

        EventGeneratorByRandom eventGeneratorByRandom = new EventGeneratorByRandom(fGraph, profiles);
        List<Event> eventsToSave = eventGeneratorByRandom.generate(lFlags);

        List<Event> savedEvents = this.eventRepository.saveAll(eventsToSave);
        return savedEvents;
    }
}

package com.raxim.myscoutee.profile.service;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.repository.mongo.EventItemRepository;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.repository.mongo.ScheduleRepository;

public class EventPriorityService {
    private final LikeRepository likeRepository;
    private final EventItemRepository eventItemRepository;
    private final ObjectMapper objectMapper;

    public EventPriorityService(ScheduleRepository scheduleRepository,
            LikeRepository likeRepository, EventItemRepository eventItemRepository,
            ObjectMapper objectMapper) {
        this.likeRepository = likeRepository;
        this.eventItemRepository = eventItemRepository;
        this.objectMapper = objectMapper;
    }

    /*public List<Set<Profile>> generate() {
        //find all eventItem where capacity hasn't been filled, status is A, type is 'g' general, and priority is true
        //the data structure will be refactored, but the calculation shouldn't change that much
    }*/
}

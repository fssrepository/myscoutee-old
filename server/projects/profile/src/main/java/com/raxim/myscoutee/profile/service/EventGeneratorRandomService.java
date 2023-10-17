package com.raxim.myscoutee.profile.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.algo.BCTree;
import com.raxim.myscoutee.algo.CTree;
import com.raxim.myscoutee.algo.dto.DGraph;
import com.raxim.myscoutee.algo.dto.Range;
import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.RangeLocal;
import com.raxim.myscoutee.profile.data.document.mongo.Schedule;
import com.raxim.myscoutee.profile.data.dto.FilteredEdges;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.ScheduleRepository;
import com.raxim.myscoutee.profile.service.iface.IEventGeneratorService;
import com.raxim.myscoutee.profile.util.AppConstants;

/*
 * the members did not met witheach other before
 */
@Service
public class EventGeneratorRandomService implements IEventGeneratorService {

    public static final String SCHEDULE_RANDOM_GROUP = "RANDOM_GROUP";

    private final ScheduleRepository scheduleRepository;
    private final LikeService likeService;
    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;

    public EventGeneratorRandomService(ScheduleRepository scheduleRepository,
            LikeService likeService, EventRepository eventRepository,
            ObjectMapper objectMapper) {
        this.scheduleRepository = scheduleRepository;
        this.likeService = likeService;
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
    }

    // only profiles with status is 'A'
    public List<Event> generate() {
        System.out.println("---- generate start -----");

        Optional<Schedule> schedule = scheduleRepository.findByKey(SCHEDULE_RANDOM_GROUP);
        long lastIdx = schedule.map(Schedule::getLastIdx).orElse(0L);
        long batchSize = schedule.map(Schedule::getBatchSize).orElse(1000L);

        Range flags = schedule.map(sch -> JsonUtil.jsonToObject(sch.getFlags(),
                Range.class, objectMapper))
                .orElse(new Range(6, 12));

        FilteredEdges filteredEdges = likeService.getEdges(Set.of("A"));

        DGraph dGraph = new DGraph();
        dGraph.addAll(filteredEdges.getEdges());

        Range range = new Range(flags.getMin(), flags.getMax());
        List<BCTree> bcTrees = dGraph.stream().map(cGraph -> {
            CTree cTree = new CTree(cGraph, List.of(AppConstants.MAN, AppConstants.WOMAN),
                    filteredEdges.getIgnoredEdges());
            return new BCTree(cTree, range);
        }).toList();

        List<List<Member>> membersByGroup = new ArrayList<>();
        bcTrees.forEach(bcTree -> bcTree.forEach(cGroup -> {
            List<Member> profiles = cGroup.stream()
                    .map(node -> new Member(filteredEdges.getNodes().get(node.getId()), "A", "U"))
                    .toList();
            membersByGroup.add(profiles);
        }));

        List<Event> handledEvents = membersByGroup.stream()
                .map(members -> {
                    Event event = new Event();
                    event.setId(UUID.randomUUID());
                    event.setType("P");
                    event.setCategory("na");

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
                    RangeLocal range1 = new RangeLocal(fromDT, toDT);
                    event.setRange(range1);
                    return event;
                })
                .collect(Collectors.toList());

        List<Event> eventsToSave = handledEvents.stream().flatMap(event -> event.flatten().stream()).toList();

        List<Event> savedEvents = this.eventRepository.saveAll(eventsToSave);

        System.out.println("---- generate end -----");

        return savedEvents;
    }
}

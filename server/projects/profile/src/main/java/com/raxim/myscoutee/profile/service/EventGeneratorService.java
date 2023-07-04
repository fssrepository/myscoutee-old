package com.raxim.myscoutee.profile.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.algo.BCTree;
import com.raxim.myscoutee.algo.CTree;
import com.raxim.myscoutee.algo.dto.DGraph;
import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.algo.dto.Range;
import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Like;
import com.raxim.myscoutee.profile.data.document.mongo.LikeGroup;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.RangeLocal;
import com.raxim.myscoutee.profile.data.document.mongo.Schedule;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.repository.mongo.ScheduleRepository;
import com.raxim.myscoutee.profile.util.AppConstants;
import com.raxim.myscoutee.profile.util.EventUtil;

@Service
public class EventGeneratorService implements IEventGeneratorService {

    public static final String SCHEDULE_RANDOM_GROUP = "RANDOM_GROUP";

    private final ScheduleRepository scheduleRepository;
    private final LikeRepository likeRepository;
    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;

    public EventGeneratorService(ScheduleRepository scheduleRepository,
            LikeRepository likeRepository, EventRepository eventRepository,
            ObjectMapper objectMapper) {
        this.scheduleRepository = scheduleRepository;
        this.likeRepository = likeRepository;
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
    }

    // only profiles with status is 'A'
    public List<Event> generate() {
        System.out.println("---- generate start -----");

        Optional<Schedule> schedule = scheduleRepository.findByKey(SCHEDULE_RANDOM_GROUP);
        long lastIdx = schedule.map(Schedule::getLastIdx).orElse(0L);
        long batchSize = schedule.map(Schedule::getBatchSize).orElse(1000L);
        Range flags = schedule.map(sch -> JsonUtil.jsonToObject(sch.getFlags(), Range.class, objectMapper))
                .orElse(new Range(6, 12));

        // batched version of query does exist -> just to simplify for the time being
        List<LikeGroup> likeGroups = likeRepository.findLikeGroups();

        List<Event> events = eventRepository.findAll();

        List<Set<Edge>> ignoredEdgesByMet = events.stream().map(event -> {
            Set<Member> members = event.getMembers()
                    .stream()
                    .filter(member -> Member.MET.contains(member.getStatus()))
                    .collect(Collectors.toSet());

            return EventUtil.permutate(members);
        }).toList();

        // merge likes
        List<Like> likesBoth = likeGroups.stream().map(group -> {
            return group.reduce();
        }).filter(like -> like != null).toList();

        // nodes
        Map<String, Profile> nodes = new HashMap<>();
        likesBoth.forEach(likeBoth -> {
            nodes.put(likeBoth.getFrom().getId().toString(), likeBoth.getFrom());
            nodes.put(likeBoth.getTo().getId().toString(), likeBoth.getTo());
        });

        System.out.println("Num of nodes " + nodes.keySet().size());

        // ignoring edges, where the profile is not with status 'A'
        Set<Edge> ignoredEdgesByStatus = likesBoth.stream()
                .filter(ignoredLike -> !"A".equals(ignoredLike.getFrom().getStatus())
                        || !"A".equals(ignoredLike.getTo().getStatus()))
                .map(likeBoth -> {
                    Node fromNode = new Node(likeBoth.getFrom().getId().toString(), likeBoth.getFrom().getGender());
                    Node toNode = new Node(likeBoth.getTo().getId().toString(), likeBoth.getTo().getGender());
                    double weight = (double) (likeBoth.getRate() * likeBoth.getDistance());
                    return new Edge(fromNode, toNode, weight);
                }).collect(Collectors.toSet());

        List<Set<Edge>> ignoredEdges = new ArrayList<>();
        ignoredEdges.addAll(ignoredEdgesByMet);
        ignoredEdges.add(ignoredEdgesByStatus);

        System.out.println("ignored edges -----" + ignoredEdges);

        // edges
        List<Edge> edges = likesBoth.stream().map(likeBoth -> {
            Node fromNode = new Node(likeBoth.getFrom().getId().toString(), likeBoth.getFrom().getGender());
            Node toNode = new Node(likeBoth.getTo().getId().toString(), likeBoth.getTo().getGender());
            double weight = (double) (likeBoth.getRate() * likeBoth.getDistance());
            return new Edge(fromNode, toNode, weight);
        }).toList();

        System.out.println("Num of edges " + edges.size());

        DGraph dGraph = new DGraph();
        dGraph.addAll(edges);

        Range range = new Range(flags.getMin(), flags.getMax());
        List<BCTree> bcTrees = dGraph.stream().map(cGraph -> {
            CTree cTree = new CTree(cGraph, List.of(AppConstants.MAN, AppConstants.WOMAN), ignoredEdges);
            return new BCTree(cTree, range);
        }).toList();

        List<List<Member>> membersByGroup = new ArrayList<>();
        bcTrees.forEach(bcTree -> bcTree.forEach(cGroup -> {
            List<Member> profiles = cGroup.stream()
                    .map(node -> new Member(nodes.get(node.getId()), "A", "U"))
                    .toList();
            membersByGroup.add(profiles);
        }));

        List<Event> memberEvents = membersByGroup.stream()
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

        this.eventRepository.saveAll(events);

        System.out.println("---- generate end -----");

        return memberEvents;
    }
}

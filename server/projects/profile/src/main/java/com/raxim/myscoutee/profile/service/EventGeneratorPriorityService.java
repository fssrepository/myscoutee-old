package com.raxim.myscoutee.profile.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.raxim.myscoutee.algo.BCTree;
import com.raxim.myscoutee.algo.CTree;
import com.raxim.myscoutee.algo.dto.CGroup;
import com.raxim.myscoutee.algo.dto.DGraph;
import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.algo.dto.Range;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.EventWithCandidates;
import com.raxim.myscoutee.profile.data.document.mongo.Like;
import com.raxim.myscoutee.profile.data.document.mongo.LikeGroup;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Rule;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.util.AppConstants;
import com.raxim.myscoutee.profile.util.EventUtil;

public class EventGeneratorPriorityService implements IEventGeneratorService {
    private final LikeRepository likeRepository;
    private final EventRepository eventRepository;

    public EventGeneratorPriorityService(
            LikeRepository likeRepository,
            EventRepository eventRepository) {
        this.likeRepository = likeRepository;
        this.eventRepository = eventRepository;
    }

    public List<Event> generate() {
        List<LikeGroup> likeGroups = likeRepository.findLikeGroups();

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

        // edges
        List<Edge> edges = likesBoth.stream().map(likeBoth -> {
            Node fromNode = new Node(likeBoth.getFrom().getId().toString(), likeBoth.getFrom().getGender());
            Node toNode = new Node(likeBoth.getTo().getId().toString(), likeBoth.getTo().getGender());
            double weight = (double) likeBoth.getRate();
            return new Edge(fromNode, toNode, weight);
        }).toList();

        // ignoring edges, where the profile is not with status 'A' or F
        Set<Edge> ignoredEdgesByStatus = likesBoth.stream()
                .filter(ignoredLike -> !Profile.ACTIVE.contains(ignoredLike.getFrom().getStatus())
                        || !Profile.ACTIVE.contains(ignoredLike.getTo().getStatus()))
                .map(likeBoth -> {
                    Node fromNode = new Node(likeBoth.getFrom().getId().toString(), likeBoth.getFrom().getGender());
                    Node toNode = new Node(likeBoth.getTo().getId().toString(), likeBoth.getTo().getGender());
                    double weight = (double) (likeBoth.getRate() * likeBoth.getDistance());
                    return new Edge(fromNode, toNode, weight);
                }).collect(Collectors.toSet());

        List<EventWithCandidates> eventWithCandidates = this.eventRepository.findCandidates();

        List<Event> events = eventWithCandidates.stream().map(event -> {

            if ("T".equals(event.getEvent().getStatus())
                    || "A".equals(event.getEvent().getStatus())) {
                // send notification either Timed out, or event Activated
                return event.getEvent();
            }

            Rule rule = event.getEvent().getRule();

            // status "P"
            // update members who deleted their profiles
            LocalDateTime validUntil = LocalDateTime.now()
                    .minus(rule.getMemberGrace(),
                            ChronoUnit.MINUTES);

            Set<Member> members = event.getEvent().getMembers().stream().map(member -> {
                if ("I".equals(member.getStatus())
                        && member.getCreatedDate().isBefore(validUntil)) {
                    member.setStatus("T");
                }

                return member;
            }).collect(Collectors.toSet());
            event.getEvent().setMembers(members);

            Set<Edge> tIgnoredEdges = EventUtil.permutate(event.getEvent().getMembers());

            List<Edge> tIgnoredEdgesByRate = edges.stream()
                    .filter(edge -> rule.getRate() != null &&
                            edge.getWeight() >= rule.getRate())
                    .toList();
            tIgnoredEdges.addAll(tIgnoredEdgesByRate);
            List<Set<Edge>> ignoredEdges = List.of(tIgnoredEdges, ignoredEdgesByStatus);

            Set<Edge> possibleEdges = EventUtil.permutate(event.getCandidates());

            List<Edge> validEdges = edges.stream()
                    .filter(edge -> possibleEdges.contains(edge))
                    .toList();

            DGraph dGraph = new DGraph();
            dGraph.addAll(validEdges);

            Set<Node> activeNodes = event.getEvent().getMembers().stream()
                    .filter(member -> "A".equals(member.getStatus())
                            && "I".equals(member.getStatus()))
                    .map(member -> {
                        Profile profile = nodes.get(member.getProfile().getId().toString());
                        return new Node(profile.getId().toString(), profile.getGender());
                    })
                    .collect(Collectors.toSet());
            
            Range range = new Range(event.getEvent().getCapacity().getMin(),
                    event.getEvent().getCapacity().getMax() - activeNodes.size());

            List<String> types;
            if (Boolean.TRUE.equals(rule.getBalanced())) {
                types = List.of(AppConstants.MAN, AppConstants.WOMAN);
            } else {
                types = List.of();
            }

            List<BCTree> bcTrees = dGraph.stream().map(cGraph -> {
                CTree cTree = new CTree(cGraph, types, ignoredEdges);
                return new BCTree(cTree, range, activeNodes);
            }).toList();

            Iterator<BCTree> itBCTree = bcTrees.iterator();
            if (itBCTree.hasNext()) {
                BCTree bcTree = itBCTree.next();
                Iterator<CGroup> itCGroup = bcTree.iterator();
                if (itCGroup.hasNext()) {
                    CGroup cGroup = itCGroup.next();
                    Set<Member> newMembers = cGroup.stream()
                            .map(node -> new Member(nodes.get(node.getId()), "A", "U"))
                            .collect(Collectors.toSet());
                    // send notification invited
                    event.getEvent().getMembers().addAll(newMembers);
                }
            }
            return event.getEvent();
        }).toList();

        this.eventRepository.saveAll(events);

        return events;
    }
}

package com.raxim.myscoutee.profile.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.algo.BCTree;
import com.raxim.myscoutee.algo.BCTreeIterator;
import com.raxim.myscoutee.algo.CTree;
import com.raxim.myscoutee.algo.dto.CGroup;
import com.raxim.myscoutee.algo.dto.DGraph;
import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.algo.dto.Range;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.EventWithCandidates;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Rule;
import com.raxim.myscoutee.profile.data.dto.FilteredEdges;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.service.iface.IEventGeneratorService;
import com.raxim.myscoutee.profile.util.AppConstants;
import com.raxim.myscoutee.profile.util.EventUtil;

/*
 * who has already met, invited by priority
 */
@Service
public class EventGeneratorByPriorityService implements IEventGeneratorService {
    private final EventRepository eventRepository;

    public EventGeneratorByPriorityService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> generate(FilteredEdges filteredEdges, String flags) {
        List<EventWithCandidates> eventWithCandidates = this.eventRepository.findEventsWithCandidates();

        List<Event> handledEvents = eventWithCandidates.stream().map(event -> {
            event.getEvent().syncStatus();

            if ("T".equals(event.getEvent().getStatus())
                    || "A".equals(event.getEvent().getStatus())) {
                // send notification either Timed out, or event Activated
                return event.getEvent();
            }

            Rule rule = event.getEvent().getRule();

            Set<Edge> sIgnoredEdges = EventUtil.permutate(event.getEvent().getMembers());

            Set<Edge> sIgnoredEdgesByRate = filteredEdges.getEdges().stream()
                    .filter(edge -> rule.getRate() != null &&
                            edge.getWeight() >= rule.getRate())
                    .collect(Collectors.toSet());

            List<Set<Edge>> ignoredEdges = new ArrayList<>();
            ignoredEdges.add(sIgnoredEdges);
            ignoredEdges.add(sIgnoredEdgesByRate);
            ignoredEdges.addAll(filteredEdges.getIgnoredEdges());

            Set<Edge> possibleEdges = EventUtil.permutate(event.getCandidates());

            List<Edge> validEdges = filteredEdges.getEdges().stream()
                    .filter(edge -> possibleEdges.contains(edge))
                    .toList();

            DGraph dGraph = new DGraph();
            dGraph.addAll(validEdges);

            Set<Node> activeNodes = event.getEvent().getMembers().stream()
                    .filter(member -> "A".equals(member.getStatus())
                            || "I".equals(member.getStatus()))
                    .map(member -> {
                        Profile profile = filteredEdges.getNodes().get(member.getProfile().getId().toString());
                        return new Node(profile.getId().toString(), profile.getGender());
                    })
                    .collect(Collectors.toSet());

            Range range = new Range(event.getEvent().getCapacity().getMin() - activeNodes.size(),
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
                BCTreeIterator itCGroup = (BCTreeIterator) bcTree.iterator();
                if (itCGroup.hasAnyNext()) {
                    CGroup cGroup = itCGroup.next();
                    Set<Member> newMembers = cGroup.stream()
                            .map(node -> new Member(filteredEdges.getNodes().get(node.getId()), "I", "U"))
                            .collect(Collectors.toSet());
                    // send notification invited
                    event.getEvent().getMembers().addAll(newMembers);
                }
            }
            return event.getEvent();
        }).toList();

        List<Event> eventsToSave = handledEvents.stream().flatMap(event -> event.flatten().stream()).toList();

        List<Event> savedEvents = this.eventRepository.saveAll(eventsToSave);
        return savedEvents;
    }
}

package com.raxim.myscoutee.profile.generator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.raxim.myscoutee.algo.BCTree;
import com.raxim.myscoutee.algo.BCTreeIterator;
import com.raxim.myscoutee.algo.CTree;
import com.raxim.myscoutee.algo.Fifa;
import com.raxim.myscoutee.algo.dto.CGroup;
import com.raxim.myscoutee.algo.dto.DGraph;
import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.algo.dto.Range;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Rule;
import com.raxim.myscoutee.profile.data.document.mongo.ScoreMatrix;
import com.raxim.myscoutee.profile.data.dto.FilteredEdges;
import com.raxim.myscoutee.profile.util.AppConstants;
import com.raxim.myscoutee.profile.util.EventUtil;

public class EventGeneratorByScore extends GeneratorBase<Event> {

    private final List<Event> events;
    private final Map<String, List<ScoreMatrix>> scoreMatricesByType;

    public EventGeneratorByScore(List<Event> events, FilteredEdges filteredEdges, String flags,
            Map<String, List<ScoreMatrix>> scoreMatricesByType) {
        super(filteredEdges, flags);
        this.events = events;
        this.scoreMatricesByType = scoreMatricesByType;
    }

    @Override
    public List<Event> generate() {
        List<Event> handledEvents = events.stream().map(event -> {
            event.syncStatus();

            int maxStage = event.getMaxStage();
            if (maxStage > 0 && "A".equals(event.getStatus())) {

                List<Event> nextStageEvents;
                List<Event> currStageEvents;

                if (event.isStageFinished()) {
                    currStageEvents = event.getItemsByStage(event.getStage());
                    int nextStage = event.getStage() + 1;
                    event.setStage(nextStage);
                } else {
                    currStageEvents = List.of(event);
                }

                nextStageEvents = event.getItemsByStage(event.getStage());

                Event nextItem = nextStageEvents.get(0);

                int maxCapacity = nextStageEvents.stream().mapToInt(item -> item.getCapacity().getMax()).sum();

                int firstXWinner = (int) Math.ceil((double) maxCapacity / currStageEvents.size());
                if (nextItem.getRule() != null) {
                    Rule nextRule = nextItem.getRule();

                    List<Member> winners = currStageEvents.stream().flatMap(cItem -> {
                        // nextEvent.cItem.getRule
                        List<Member> cMembers = new ArrayList<>();
                        if (Boolean.TRUE.equals(nextItem.getRule().getMutual())) {

                            Set<Edge> sIgnoredEdges = EventUtil.permutate(cItem.getMembers());

                            Set<Edge> sIgnoredEdgesByRate = getFilteredEdges().getEdges().stream()
                                    .filter(edge -> nextRule.getRate() != null &&
                                            edge.getWeight() >= nextRule.getRate())
                                    .collect(Collectors.toSet());
                            List<Set<Edge>> ignoredEdges = List.of(sIgnoredEdges, sIgnoredEdgesByRate);
                            ignoredEdges.addAll(getFilteredEdges().getIgnoredEdges());

                            Set<Edge> possibleEdges = EventUtil.permutate(cItem.getMembers());

                            List<Edge> validEdges = getFilteredEdges().getEdges().stream()
                                    .filter(edge -> possibleEdges.contains(edge))
                                    .toList();

                            DGraph dGraph = new DGraph();
                            dGraph.addAll(validEdges);

                            Set<Node> activeNodes = cItem.getMembers().stream()
                                    .filter(member -> "A".equals(member.getStatus()))
                                    .map(member -> {
                                        Profile profile = getFilteredEdges().getNodes()
                                                .get(member.getProfile().getId().toString());
                                        return new Node(profile.getId().toString(), profile.getGender());
                                    })
                                    .collect(Collectors.toSet());

                            Range range = new Range(firstXWinner, firstXWinner);

                            List<String> types;
                            if (Boolean.TRUE.equals(nextRule.getBalanced())) {
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
                                            .map(node -> new Member(getFilteredEdges().getNodes().get(node.getId()),
                                                    "P",
                                                    "U"))
                                            .collect(Collectors.toSet());
                                    cMembers = new ArrayList<>(newMembers);
                                }
                            }
                        } else {
                            Stream<Member> sMembers = cItem.getMembers().stream()
                                    .filter(member -> "A".equals(member.getStatus())
                                            && "U".equals(member.getRole()));

                            List<ScoreMatrix> scoreMatrices = scoreMatricesByType
                                    .get(nextRule.getRankType());
                            if (AppConstants.RANK_FIFA.equals(nextRule.getRankType())) {
                                Fifa fifa = new Fifa(new ArrayList<>(event.getMatches()), scoreMatrices);
                                cMembers = fifa.getFirstXMembers(firstXWinner, sMembers);
                            } else {
                                cMembers = sMembers.sorted().limit(firstXWinner).toList();
                            }
                        }

                        return cMembers.stream();
                    }).toList();

                    event.assignToSlots(winners);
                }
            }
            return event;
        }).toList();

        List<Event> respEvents = handledEvents.stream().flatMap(event -> event.flatten().stream()).toList();
        return respEvents;
    }

}

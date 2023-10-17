package com.raxim.myscoutee.profile.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

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
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.ScoreMatrixRepository;
import com.raxim.myscoutee.profile.service.iface.IEventGeneratorService;
import com.raxim.myscoutee.profile.util.AppConstants;
import com.raxim.myscoutee.profile.util.EventUtil;

/*
 * who did met before, selecton is by score (the members are scored -> multistage competition also using it to assign the firstX member)
 */
@Service
public class EventGeneratorByScoreService implements IEventGeneratorService {
    private final EventRepository eventRepository;
    private final ScoreMatrixRepository scoreMatrixRepository;

    public EventGeneratorByScoreService(EventRepository eventRepository, ScoreMatrixRepository scoreMatrixRepository) {
        this.eventRepository = eventRepository;
        this.scoreMatrixRepository = scoreMatrixRepository;
    }

    @Override
    public List<Event> generate(FilteredEdges filteredEdges, String flags) {
        // FilteredEdges filteredEdges = likeService.getEdges(Set.of("A", "F"));

        List<Event> events = this.eventRepository.findEvents();

        Map<String, List<ScoreMatrix>> scoreMatricesByType = new HashMap<>();

        List<Event> handledEvents = events.stream().map(event -> {
            event.syncStatus();

            if (!scoreMatricesByType.containsKey(event.getRule().getRankType())) {
                List<ScoreMatrix> scoreMatrices = this.scoreMatrixRepository.findByName(event.getRule().getRankType());
                scoreMatricesByType.put(event.getRule().getRankType(), scoreMatrices);
            }

            List<ScoreMatrix> scoreMatrices = scoreMatricesByType.get(event.getRule().getRankType());

            int maxStage = event.getMaxStage();
            if (maxStage > 0 && "A".equals(event.getStatus())) {
                List<Event> dueNextStageEvents = event.getDueNextStageEvents();
                if (!dueNextStageEvents.isEmpty()) {
                    int nextStage = event.getStage() + 1;
                    event.setStage(nextStage);
                }

                int maxCapacity = dueNextStageEvents.stream().mapToInt(item -> item.getCapacity().getMax()).sum();

                List<Event> currStageEvents = event.getItems().stream()
                        .filter(item -> "A".equals(item.getStatus()) && item.getStage() == event.getStage()).toList();

                int firstXWinner = (int) Math.ceil((double) maxCapacity / currStageEvents.size());
                if (event.getRule() != null) {

                    List<Member> winners = currStageEvents.stream().flatMap(cItem -> {
                        Double rateMin = cItem.getRule() != null ? cItem.getRule().getRate() : 0d;

                        List<Member> cMembers = new ArrayList<>();
                        if (Boolean.TRUE.equals(cItem.getRule().getMutual())) {
                            Rule rule = cItem.getRule();

                            Set<Edge> sIgnoredEdges = EventUtil.permutate(cItem.getMembers());

                            Set<Edge> sIgnoredEdgesByRate = filteredEdges.getEdges().stream()
                                    .filter(edge -> rule.getRate() != null &&
                                            edge.getWeight() >= rule.getRate())
                                    .collect(Collectors.toSet());
                            List<Set<Edge>> ignoredEdges = List.of(sIgnoredEdges, sIgnoredEdgesByRate);
                            ignoredEdges.addAll(filteredEdges.getIgnoredEdges());

                            Set<Edge> possibleEdges = EventUtil.permutate(cItem.getMembers());

                            List<Edge> validEdges = filteredEdges.getEdges().stream()
                                    .filter(edge -> possibleEdges.contains(edge))
                                    .toList();

                            DGraph dGraph = new DGraph();
                            dGraph.addAll(validEdges);

                            Set<Node> activeNodes = cItem.getMembers().stream()
                                    .filter(member -> "A".equals(member.getStatus()))
                                    .map(member -> {
                                        Profile profile = filteredEdges.getNodes()
                                                .get(member.getProfile().getId().toString());
                                        return new Node(profile.getId().toString(), profile.getGender());
                                    })
                                    .collect(Collectors.toSet());

                            Range range = new Range(firstXWinner,
                                    cItem.getCapacity().getMax() - activeNodes.size());

                            List<String> types;
                            if (Boolean.TRUE.equals(event.getRule().getBalanced())) {
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
                                            .map(node -> new Member(filteredEdges.getNodes().get(node.getId()), "P",
                                                    "U"))
                                            .collect(Collectors.toSet());
                                    cMembers = new ArrayList<>(newMembers);
                                }
                            }
                        } else {
                            Stream<Member> sMembers = cItem.getMembers().stream()
                                    .filter(member -> "A".equals(member.getStatus())
                                            && "U".equals(member.getRole())
                                            && (!cItem.isPriority() || member.getScore() >= rateMin));

                            if (AppConstants.RANK_FIFA.equals(event.getRule().getRankType())) {
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

        List<Event> eventsToSave = handledEvents.stream().flatMap(event -> event.flatten().stream()).toList();

        List<Event> savedEvents = this.eventRepository.saveAll(eventsToSave);
        return savedEvents;
    }

}

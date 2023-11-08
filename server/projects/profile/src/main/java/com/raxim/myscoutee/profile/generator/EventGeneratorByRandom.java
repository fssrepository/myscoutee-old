package com.raxim.myscoutee.profile.generator;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.raxim.myscoutee.algo.BCTree;
import com.raxim.myscoutee.algo.CTree;
import com.raxim.myscoutee.algo.dto.DGraph;
import com.raxim.myscoutee.algo.dto.Range;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.RangeLocal;
import com.raxim.myscoutee.profile.data.dto.FilteredEdges;
import com.raxim.myscoutee.profile.util.AppConstants;

public class EventGeneratorByRandom extends GeneratorBase<Event> {

    public EventGeneratorByRandom(FilteredEdges filteredEdges, Object flags) {
        super(filteredEdges, flags);
    }

    @Override
    public List<Event> generate() {

        Range lFlags = (Range) getFlags();

        DGraph dGraph = new DGraph();
        dGraph.addAll(getFilteredEdges().getEdges());

        Range range = new Range(lFlags.getMin(), lFlags.getMax());
        List<BCTree> bcTrees = dGraph.stream().map(cGraph -> {
            CTree cTree = new CTree(cGraph, List.of(AppConstants.MAN, AppConstants.WOMAN),
                    getFilteredEdges().getIgnoredEdges());
            return new BCTree(cTree, range);
        }).toList();

        List<List<Member>> membersByGroup = new ArrayList<>();
        bcTrees.forEach(bcTree -> bcTree.forEach(cGroup -> {
            List<Member> profiles = cGroup.stream()
                    .map(node -> new Member(getFilteredEdges().getNodes().get(node.getId()), "A", "U"))
                    .toList();
            membersByGroup.add(profiles);
        }));

        List<Event> handledEvents = membersByGroup.stream()
                .map(members -> {
                    Event event = new Event();
                    event.setId(UUID.randomUUID());
                    event.setType("E");
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

        List<Event> respEvents = handledEvents.stream().flatMap(event -> event.flatten().stream()).toList();
        return respEvents;
    }

}

package com.raxim.myscoutee.profile.generator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.raxim.myscoutee.algo.Algo;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.algo.dto.ObjGraph;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.EventWithCandidates;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.filter.ProfileObjGraphFilter;

public class EventGeneratorByPriority extends GeneratorBase<Event, Profile> {
    private final List<EventWithCandidates> eventWithCandidates;
    private final ProfileObjGraphFilter profileObjGraphFilter;

    public EventGeneratorByPriority(List<EventWithCandidates> eventWithCandidates,
            ProfileObjGraphFilter profileObjGraphFilter,
            ObjGraph<Profile> objGraph,
            String flags) {
        super(objGraph, flags);
        this.eventWithCandidates = eventWithCandidates;
        this.profileObjGraphFilter = profileObjGraphFilter;
    }

    @Override
    public List<Event> generate() {
        List<Event> handledEvents = eventWithCandidates.stream().map(event -> {
            event.getEvent().syncStatus();

            if ("T".equals(event.getEvent().getStatus())
                    || "A".equals(event.getEvent().getStatus())) {
                // send notification either Timed out, or event Activated
                return event.getEvent();
            }

            ObjGraph<Profile> objGraph = this.profileObjGraphFilter.filter(getObjGraph(), event);

            Algo algo = new Algo();
            Set<Node> candidates = algo.runPriority(objGraph.getfGraph(),
                    event.getEvent().getTypes(),
                    event.getEvent().getCapacity());

            Set<Member> newMembers = candidates.stream()
                    .map(node -> new Member(objGraph.getNodes().get(node.getId()), "I", "U"))
                    .collect(Collectors.toSet());

            event.getEvent().getMembers().addAll(newMembers);

            return event.getEvent();
        }).toList();

        List<Event> respEvents = handledEvents.stream().flatMap(event -> event.flatten().stream()).toList();
        return respEvents;
    }

}

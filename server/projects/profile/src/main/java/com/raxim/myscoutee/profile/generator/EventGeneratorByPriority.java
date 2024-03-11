package com.raxim.myscoutee.profile.generator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.raxim.myscoutee.algo.Algo;
import com.raxim.myscoutee.algo.dto.FGraph;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;

public class EventGeneratorByPriority extends GeneratorBase<Event, Profile> {
    private final List<Event> events;

    public EventGeneratorByPriority(List<Event> events,
            FGraph fGraph, Map<String, Profile> profiles) {
        super(fGraph, profiles);
        this.events = events;
    }

    @Override
    public List<Event> generate(Object flags) {
        List<Event> handledEvents = events.stream().map(event -> {
            event.syncStatus();

            if ("T".equals(event.getStatus())
                    || "A".equals(event.getStatus())) {
                // send notification either Timed out, or event Activated
                return event;
            }

            FGraph fGraph = getfGraph().filter(event);

            Algo algo = new Algo();
            List<Set<Node>> candidates = algo.run(fGraph,
                    event.getTypes(),
                    event.getCapacity(), true);

            Set<Member> newMembers = candidates.get(0).stream()
                    .map(node -> new Member(getProfileById(node.getId()), "I", "U"))
                    .collect(Collectors.toSet());

            event.getMembers().addAll(newMembers);

            return event;
        }).toList();

        List<Event> respEvents = handledEvents.stream().flatMap(event -> event.flatten().stream()).toList();
        return respEvents;
    }

}

package com.raxim.myscoutee.profile.generator;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.raxim.myscoutee.algo.Algo;
import com.raxim.myscoutee.algo.dto.FGraph;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.algo.dto.Range;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.RangeLocal;
import com.raxim.myscoutee.profile.util.AppConstants;

public class EventGeneratorByRandom extends GeneratorBase<Event, Profile> {

    public EventGeneratorByRandom(FGraph fGraph, Map<String, Profile> profiles, Object flags) {
        super(fGraph, profiles, flags);
    }

    @Override
    public List<Event> generate() {

        Range lFlags = (Range) getFlags();
        Range range = new Range(lFlags.getMin(), lFlags.getMax());
        List<String> types = List.of(AppConstants.MAN, AppConstants.WOMAN);

        Algo algo = new Algo();
        List<Set<Node>> nodesByEvent = algo.run(getfGraph(), types, range, false);

        List<Event> handledEvents = nodesByEvent.stream()
                .map(nodes -> {

                    List<Member> members = nodes.stream()
                            .map(n -> new Member(getProfileById(n.getId()), "A", "U"))
                            .toList();

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

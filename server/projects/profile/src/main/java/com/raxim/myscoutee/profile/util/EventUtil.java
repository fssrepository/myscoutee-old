package com.raxim.myscoutee.profile.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.EventItem;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.RangeLocal;
import com.raxim.myscoutee.profile.data.document.mongo.Slot;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;

public class EventUtil {
    public static final String EVENT_TYPE_PRIVATE = "pr";
    public static final String DELETED = "D";

    //TODO: to be fixed 
    /*public static Event shiftBy(Event event, EventItem eventItem, ObjectMapper objectMapper) {
        long minutes;
        RangeLocal evtRange;
        if (eventItem.getId().equals(event.getInfo().getId())) {
            minutes = ChronoUnit.MINUTES.between(event.getInfo().getRange().getStart(),
                    eventItem.getRange().getStart());
            evtRange = new RangeLocal(eventItem.getRange().getStart(), eventItem.getRange().getEnd());

            EventItem clonedEventItem = JsonUtil.clone(eventItem, objectMapper);
            clonedEventItem.setRange(evtRange);
            clonedEventItem.setMembers(event.getInfo().getMembers());
            clonedEventItem.setNum(event.getInfo().getNum());
            event.setInfo(clonedEventItem);
        } else {
            minutes = 0L;
            RangeLocal maxRange = event.getItems().stream()
                    .map(item -> item.getId().equals(eventItem.getId()) ? eventItem.getRange() : item.getRange())
                    .reduce((acc, range) -> {
                        LocalDateTime start = acc.getStart();
                        if (range.getStart().isBefore(start)) {
                            start = range.getStart();
                        }
                        LocalDateTime end = acc.getEnd();
                        if (range.getEnd().isAfter(end)) {
                            end = range.getEnd();
                        }
                        return new RangeLocal(start, end);
                    })
                    .orElse(null);
            evtRange = maxRange;

            EventItem clonedEventItem = JsonUtil.clone(eventItem, objectMapper);
            clonedEventItem.setRange(evtRange);
            event.setInfo(clonedEventItem);
        }

        List<EventItem> updatedItems = event.getItems().stream()
                .map(item -> {
                    EventItem currItem;
                    if (event.getItems().size() == 1 || event.getInfo().getId().equals(item.getId())) {
                        currItem = event.getInfo();
                    } else if (item.getId().equals(eventItem.getId())) {
                        EventItem clonedEventItem = JsonUtil.clone(item, objectMapper);
                        clonedEventItem.setMembers(item.getMembers());
                        clonedEventItem.setNum(item.getNum());
                        currItem = clonedEventItem;
                    } else {
                        currItem = item;
                    }

                    if (minutes > 0) {
                        LocalDateTime start = item.getRange().getStart().plusMinutes(minutes);
                        LocalDateTime end = item.getRange().getEnd().plusMinutes(minutes);
                        RangeLocal range = new RangeLocal(start, end);

                        EventItem clonedEventItem = JsonUtil.clone(item, objectMapper);
                        clonedEventItem.setRange(range);
                        return clonedEventItem;
                    } else {
                        return currItem;
                    }
                })
                .collect(Collectors.toList());

        event.setItems(updatedItems);

        return event;
    }*/

    //TODO: to be fixed 
    /*public static Event cloneBy(Event event, Profile profile, Slot slot, boolean isRef, ObjectMapper objectMapper) {
        Event clonedEvent = JsonUtil.clone(event, objectMapper);
        clonedEvent.setId(UUID.randomUUID());
        if (slot != null) {
            clonedEvent.getInfo().setCapacity(slot.getCapacity());
            clonedEvent.setCnt(slot.getNumOfItems());

            LocalDate slotDate = slot.getRange().getStart().toLocalDate();
            LocalDateTime infoStart = event.getInfo().getRange().getStart();
            LocalDateTime infoEnd = event.getInfo().getRange().getEnd();

            long days = ChronoUnit.DAYS.between(infoStart.toLocalDate(), slotDate);
            RangeLocal evtRange = new RangeLocal(infoStart.plusDays(days), infoEnd.plusDays(days));

            EventItem clonedEventItem = JsonUtil.clone(clonedEvent.getInfo(), objectMapper);
            clonedEventItem.setRange(evtRange);

            clonedEvent = shiftBy(clonedEvent, clonedEventItem, objectMapper);
        }

        List<EventItem> updatedItems = clonedEvent.getItems().stream()
                .filter(item -> !EVENT_TYPE_PRIVATE.equals(item.getType()) && !DELETED.equals(item.getStatus()))
                .map(item -> {
                    EventItem clonedEventItem = JsonUtil.clone(item, objectMapper);
                    clonedEventItem.setId(UUID.randomUUID());
                    return clonedEventItem;
                })
                .collect(Collectors.toList());

        updatedItems.set(0, clonedEvent.getInfo());

        Set<Member> members = clonedEvent.getInfo().getMembers().stream()
                .filter(member -> "P".equals(member.getStatus()))
                .collect(Collectors.toSet());

        String status = clonedEvent.getStatus();
        if ("T".equals(status) || !members.isEmpty()) {
            status = "P";
        } else {
            status = "A";
        }

        String memberStatus;
        if ("T".equals(clonedEvent.getStatus())) {
            memberStatus = "P";
        } else {
            memberStatus = "A";
        }

        String role;
        if ("T".equals(clonedEvent.getStatus())) {
            role = "P";
        } else {
            role = "M";
        }

        Member member = new Member();
        member.setId(profile.getId());
        member.setProfile(profile);
        member.setStatus(memberStatus);
        member.setRole(role);
        member.setEventRef(clonedEvent.getId());
        members.add(member);

        Event ref = isRef ? null : event;

        clonedEvent.getInfo().setMembers(members);
        clonedEvent.getInfo().setNum(clonedEvent.getInfo().getNum() + 1);

        clonedEvent.setStatus(status);
        clonedEvent.setGroup(profile.getGroup());
        clonedEvent.setRef(ref);

        return clonedEvent;
    }*/

    //TODO: to be fixed 
    /*public static EventDTO transform(Event event) {
        LocalDateTime eventStart = event.getInfo().getRange().getStart();
        LocalDate groupKey = eventStart != null ? eventStart.toLocalDate() : null;
        Long sortKey = eventStart != null ? eventStart.toInstant(ZoneOffset.UTC).toEpochMilli() : null;

        return new EventDTO(event, groupKey, sortKey);
    }*/

    public static Set<Edge> permutate(Set<Member> members) {
        List<List<List<Member>>> nodes = CommonUtil.permutation(members);

        Set<Edge> edges = nodes.stream()
                .flatMap(group -> group.stream()
                        .map(pair -> {
                            Profile profile1 = pair.get(0).getProfile();
                            Profile profile2 = pair.get(1).getProfile();

                            Node node1 = new Node(profile1.getId().toString(), profile1.getGender());
                            Node node2 = new Node(profile2.getId().toString(), profile2.getGender());
                            Edge edge = new Edge(node1, node2);
                            return edge;
                        }))
                .collect(Collectors.toSet());
        return edges;
    }
}

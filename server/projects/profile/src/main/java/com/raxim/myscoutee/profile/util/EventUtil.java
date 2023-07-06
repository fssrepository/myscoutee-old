package com.raxim.myscoutee.profile.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;

public class EventUtil {
    public static final String EVENT_TYPE_PRIVATE = "pr";
    public static final String DELETED = "D";

    // TODO: to be fixed
    /*
     * public static Event cloneBy(Event event, Profile profile, Slot slot, boolean
     * isRef, ObjectMapper objectMapper) {
     * Event clonedEvent = JsonUtil.clone(event, objectMapper);
     * clonedEvent.setId(UUID.randomUUID());
     * if (slot != null) {
     * clonedEvent.getInfo().setCapacity(slot.getCapacity());
     * clonedEvent.setCnt(slot.getNumOfItems());
     * 
     * LocalDate slotDate = slot.getRange().getStart().toLocalDate();
     * LocalDateTime infoStart = event.getInfo().getRange().getStart();
     * LocalDateTime infoEnd = event.getInfo().getRange().getEnd();
     * 
     * long days = ChronoUnit.DAYS.between(infoStart.toLocalDate(), slotDate);
     * RangeLocal evtRange = new RangeLocal(infoStart.plusDays(days),
     * infoEnd.plusDays(days));
     * 
     * EventItem clonedEventItem = JsonUtil.clone(clonedEvent.getInfo(),
     * objectMapper);
     * clonedEventItem.setRange(evtRange);
     * 
     * clonedEvent = shiftBy(clonedEvent, clonedEventItem, objectMapper);
     * }
     * 
     * List<EventItem> updatedItems = clonedEvent.getItems().stream()
     * .filter(item -> !EVENT_TYPE_PRIVATE.equals(item.getType()) &&
     * !DELETED.equals(item.getStatus()))
     * .map(item -> {
     * EventItem clonedEventItem = JsonUtil.clone(item, objectMapper);
     * clonedEventItem.setId(UUID.randomUUID());
     * return clonedEventItem;
     * })
     * .collect(Collectors.toList());
     * 
     * updatedItems.set(0, clonedEvent.getInfo());
     * 
     * Set<Member> members = clonedEvent.getInfo().getMembers().stream()
     * .filter(member -> "P".equals(member.getStatus()))
     * .collect(Collectors.toSet());
     * 
     * String status = clonedEvent.getStatus();
     * if ("T".equals(status) || !members.isEmpty()) {
     * status = "P";
     * } else {
     * status = "A";
     * }
     * 
     * String memberStatus;
     * if ("T".equals(clonedEvent.getStatus())) {
     * memberStatus = "P";
     * } else {
     * memberStatus = "A";
     * }
     * 
     * String role;
     * if ("T".equals(clonedEvent.getStatus())) {
     * role = "P";
     * } else {
     * role = "M";
     * }
     * 
     * Member member = new Member();
     * member.setId(profile.getId());
     * member.setProfile(profile);
     * member.setStatus(memberStatus);
     * member.setRole(role);
     * member.setEventRef(clonedEvent.getId());
     * members.add(member);
     * 
     * Event ref = isRef ? null : event;
     * 
     * clonedEvent.getInfo().setMembers(members);
     * clonedEvent.getInfo().setNum(clonedEvent.getInfo().getNum() + 1);
     * 
     * clonedEvent.setStatus(status);
     * clonedEvent.setGroup(profile.getGroup());
     * clonedEvent.setRef(ref);
     * 
     * return clonedEvent;
     * }
     */

    public static Set<Edge> permutate(Set<Member> members) {
        List<List<List<Member>>> nodes = CommonUtil.permutation(new HashSet<>(members));

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

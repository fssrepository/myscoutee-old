package com.raxim.myscoutee.profile.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.profile.repository.mongo.EventItemRepository;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.repository.mongo.ScheduleRepository;

public class EventPriorityService {
    private final LikeRepository likeRepository;
    private final EventItemRepository eventItemRepository;
    private final ObjectMapper objectMapper;

    public EventPriorityService(ScheduleRepository scheduleRepository,
            LikeRepository likeRepository, EventItemRepository eventItemRepository,
            ObjectMapper objectMapper) {
        this.likeRepository = likeRepository;
        this.eventItemRepository = eventItemRepository;
        this.objectMapper = objectMapper;
    }

    //the eventItem <-> event structure will be refactored, but the calculation shouldn't change that much
    //the event will have the Rule and 'g' general eventItem will be moved to Event, (event.info will be removed)
    //you can add eventItem to the event, but not necessarily
    /*public List<Set<Profile>> generate() {
        //eventItem.findCandidates()
        //1) if T, set all the members to T inside the eventItem
        //2) if A, send notification for all the members
        //3) If P
        //  a) check whether some eventItem.members hasn't been accepted invitation on time, hence needs to set the status to time out
        //4) get findLikeGroups (likeGroup.reduce first)
        //   a) filter the likes among members of the union of (candidates and eventItem.members),
        //   b) filter likes, where the rate is less then rule.rate
        //   c) ignore all edges among existing eventItem.members (generate permutation)
        //5) if P, run BCTree (check rule.balanced, eventItem.maxCapacity (CGroup.size))
        // get first CGroup
        //   add the filtered candidates to eventItem and save and send notification for the invitied members


        //findCandidates needs to be extended with mutual and from last event
        //a) from range.start of last event is just a gte from input parameter (?1)
        //b) eventItem lookup query only match, where the currentEventItem.createdBy has status 'A'
        //c) group -> merge lookup.eventItems.commonMembers
        //d) list all the events where $$currentEventItem.members is in the member list also if mutual has been set (rule.mutual)
    }*/
}

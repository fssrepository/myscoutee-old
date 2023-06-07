package com.raxim.myscoutee.profile.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.algo.BGroupSet;
import com.raxim.myscoutee.algo.NodeRepository;
import com.raxim.myscoutee.algo.dto.Bound;
import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.algo.dto.Range;
import com.raxim.myscoutee.profile.data.document.mongo.Like;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Schedule;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.repository.mongo.ScheduleRepository;

@Service
public class EventGeneratorService {

    public static final String RANDOM_GROUP = "RANDOM_GROUP";
    private final ScheduleRepository scheduleRepository;
    private final LikeRepository likeRepository;
    private final Map<String, Profile> nodes;

    public GroupGenerator(ScheduleRepository scheduleRepository, LikeRepository likeRepository) {
        this.scheduleRepository = scheduleRepository;
        this.likeRepository = likeRepository;
        this.nodes = new HashMap<>();
    }

    @Override
    public List<Set<Profile>> generate(Bound flags) {
        Optional<Schedule> schedule = scheduleRepository.findByKey(RANDOM_GROUP);
        Date lastRunningTime = schedule.map(Schedule::getLastRunDate).orElse(new Date());

        // rates should be harmonic mean
        List<Like> likesBoth = likeRepository.findBothAll(lastRunningTime.toString(), 1.5);

        List<Edge> edges = new ArrayList<>();
        for (Like likeBoth : likesBoth) {
            Node fromNode = new Node(likeBoth.getFrom().getId().toString(), likeBoth.getFrom().getGender());
            Node toNode = new Node(likeBoth.getTo().getId().toString(), likeBoth.getTo().getGender());
            long weight = (long) ((1 / (2 / likeBoth.getRate())) * likeBoth.getDistance());
            edges.add(new Edge(fromNode, toNode, weight));
        }

        NodeRepository nodeRepository = new NodeRepository();
        nodeRepository.addAll(edges);

        Range range = new Range(flags.getMinGroupSize(), flags.getMaxGroupSize());
        BGroupSet groupSet = new BGroupSet(nodeRepository, range, List.of("m", "w"));

        Map<String, Profile> nodeMap = new HashMap<>();
        likesBoth.forEach(likeBoth -> {
            nodeMap.put(likeBoth.getFrom().getId().toString(), likeBoth.getFrom());
            nodeMap.put(likeBoth.getTo().getId().toString(), likeBoth.getTo());
        });
        nodes.putAll(nodeMap);

        List<Set<Profile>> profileList = new ArrayList<>();
        for(com.raxim.myscoutee.algo.dto.Group group : groupSet) {
            
            Set<Profile> profilesByGroup = group.getNodes().stream().map(node -> node.stream().map(id -> nodes.get(id.getId())).collect(Collectors.toSet()))
            profileList.add(profilesByGroup);

        }

        return profileList;
    }
}

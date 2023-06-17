package com.raxim.myscoutee.profile.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.algo.BGroupSet;
import com.raxim.myscoutee.algo.NodeRepository;
import com.raxim.myscoutee.algo.dto.Bound;
import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.GroupAlgo;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.algo.dto.Range;
import com.raxim.myscoutee.profile.data.document.mongo.Like;
import com.raxim.myscoutee.profile.data.document.mongo.LikeForGroup;
import com.raxim.myscoutee.profile.data.document.mongo.LikeGroup;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Schedule;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.repository.mongo.ScheduleRepository;
import com.raxim.myscoutee.profile.util.AppConstants;
import com.raxim.myscoutee.profile.util.LikeUtil;

@Service
public class EventGeneratorService {

    public static final String SCHEDULE_RANDOM_GROUP = "RANDOM_GROUP";

    private final ScheduleRepository scheduleRepository;
    private final LikeRepository likeRepository;

    public EventGeneratorService(ScheduleRepository scheduleRepository, LikeRepository likeRepository) {
        this.scheduleRepository = scheduleRepository;
        this.likeRepository = likeRepository;
    }

    public List<Set<Profile>> generate(Bound flags) {
        Optional<Schedule> schedule = scheduleRepository.findByKey(SCHEDULE_RANDOM_GROUP);
        long lastIdx = schedule.map(Schedule::getLastIdx).orElse(0L);
        long batchSize = schedule.map(Schedule::getBatchSize).orElse(1000L);

        List<LikeGroup> likeGroups = likeRepository.findAll(lastIdx, batchSize);

        List<Like> likesBoth = reduceLikeGroups(likeGroups);

        // nodes
        Map<String, Profile> nodes = new HashMap<>();
        likesBoth.forEach(likeBoth -> {
            nodes.put(likeBoth.getFrom().getId().toString(), likeBoth.getFrom());
            nodes.put(likeBoth.getTo().getId().toString(), likeBoth.getTo());
        });

        // edges
        List<Edge> edges = likesBoth.stream().map(likeBoth -> {
            Node fromNode = new Node(likeBoth.getFrom().getId().toString(), likeBoth.getFrom().getGender());
            Node toNode = new Node(likeBoth.getTo().getId().toString(), likeBoth.getTo().getGender());
            long weight = (long) (likeBoth.getRate() * likeBoth.getDistance());
            return new Edge(fromNode, toNode, weight);
        }).toList();

        NodeRepository nodeRepository = new NodeRepository();
        nodeRepository.addAll(edges);

        Range range = new Range(flags.getMinGroupSize(), flags.getMaxGroupSize());
        BGroupSet groupSet = new BGroupSet(nodeRepository, range, List.of(AppConstants.MAN, AppConstants.WOMAN));

        List<Set<Profile>> profileList = new ArrayList<>();
        for (GroupAlgo group : groupSet) {

            Set<Profile> profilesByGroup = group.getNodes().stream()
                    .map(node -> nodes.get(node.getId()))
                    .collect(Collectors.toSet());
            profileList.add(profilesByGroup);

        }

        return profileList;
    }

    private List<Like> reduceLikeGroups(List<LikeGroup> likeGroups) {
        List<Like> likesBoth = likeGroups
                .stream().map(group -> {
                    List<LikeForGroup> likesWithStatusP = group.getLikes().stream()
                            .filter(like -> "P".equals(like.getStatus())).toList();
                    if (!likesWithStatusP.isEmpty()) {
                        Like firstLike = likesWithStatusP.get(0);

                        List<Double> ratesForStatusP = likesWithStatusP
                                .stream()
                                .map(like -> like.getRate())
                                .toList();

                        List<Double> ratesForStatusD = group.getLikes()
                                .stream()
                                .filter(like -> "D".equals(like.getStatus()))
                                .map(like -> like.getRate())
                                .toList();

                        double rate = LikeUtil.calcAdjustedHarmonicMean(ratesForStatusP, ratesForStatusD);
                        firstLike.setRate(rate);
                        return firstLike;
                    } else {
                        return null;
                    }
                }).filter(like -> like != null)
                .toList();
        return likesBoth;
    }
}

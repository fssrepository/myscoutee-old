package com.raxim.myscoutee.profile.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.algo.BCTree;
import com.raxim.myscoutee.algo.CTree;
import com.raxim.myscoutee.algo.dto.DGraph;
import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.algo.dto.Range;
import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Like;
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
    private final ObjectMapper objectMapper;

    public EventGeneratorService(ScheduleRepository scheduleRepository,
            LikeRepository likeRepository,
            ObjectMapper objectMapper) {
        this.scheduleRepository = scheduleRepository;
        this.likeRepository = likeRepository;
        this.objectMapper = objectMapper;
    }

    public List<Set<Profile>> generate() {
        Optional<Schedule> schedule = scheduleRepository.findByKey(SCHEDULE_RANDOM_GROUP);
        long lastIdx = schedule.map(Schedule::getLastIdx).orElse(0L);
        long batchSize = schedule.map(Schedule::getBatchSize).orElse(1000L);
        Range flags = schedule.map(sch -> JsonUtil.jsonToObject(sch.getFlags(), Range.class, objectMapper))
                .orElse(new Range(6, 12));

        List<LikeGroup> likeGroups = likeRepository.findAll(lastIdx, batchSize);
        /*
         * filter out all the edges between profiles being added to the same group, and
         * save it to G
         */
        /*
         * that likes should show that certain people met with each other, can't met
         * again
         */

        List<Like> likesBoth = reduceLikeGroups(likeGroups);

        // edges
        List<Edge> edges = likesBoth.stream().map(likeBoth -> {
            Node fromNode = new Node(likeBoth.getFrom().getId().toString(), likeBoth.getFrom().getGender());
            Node toNode = new Node(likeBoth.getTo().getId().toString(), likeBoth.getTo().getGender());
            double weight = (double) (likeBoth.getRate() * likeBoth.getDistance());
            boolean isIgnored = "G".equals(likeBoth.getStatus()) ? true : false;
            return new Edge(fromNode, toNode, weight, isIgnored);
        }).toList();

        DGraph dGraph = new DGraph();
        dGraph.addAll(edges);

        Range range = new Range(flags.getMin(), flags.getMax());
        List<BCTree> bcTrees = dGraph.stream().map(cGraph -> {
            CTree cTree = new CTree(cGraph, List.of(AppConstants.MAN, AppConstants.WOMAN));
            return new BCTree(cTree, range);
        }).toList();

        // nodes
        Map<String, Profile> nodes = new HashMap<>();
        likesBoth.forEach(likeBoth -> {
            nodes.put(likeBoth.getFrom().getId().toString(), likeBoth.getFrom());
            nodes.put(likeBoth.getTo().getId().toString(), likeBoth.getTo());
        });

        List<Set<Profile>> profileList = new ArrayList<>();
        bcTrees.forEach(bcTree -> bcTree.forEach(cGroup -> {
            System.out.println(cGroup);
            Set<Profile> profiles = cGroup.stream()
                    .map(node -> nodes.get(node.getId()))
                    .collect(Collectors.toSet());
            profileList.add(profiles);
        }));

        return profileList;
    }

    private List<Like> reduceLikeGroups(List<LikeGroup> likeGroups) {
        List<Like> likesBoth = likeGroups
                .stream().map(group -> {
                    List<Like> likesWithStatusP = group.getLikes().stream()
                            .filter(like -> "A".equals(like.getStatus()) || "G".equals(like.getStatus()))
                            .toList();
                    if (likesWithStatusP.size() == 2) {
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

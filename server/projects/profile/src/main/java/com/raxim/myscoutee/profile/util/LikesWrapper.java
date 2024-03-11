package com.raxim.myscoutee.profile.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.FGraph;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Like;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;

public class LikesWrapper {

    private final List<Like> likes;

    public LikesWrapper(List<Like> likes) {
        this.likes = likes;
    }

    public Map<String, Profile> profiles() {
        Map<String, Profile> profiles = new HashMap<>();
        this.likes.forEach(likeBoth -> {
            profiles.put(likeBoth.getFrom().getId().toString(), likeBoth.getFrom());
            profiles.put(likeBoth.getTo().getId().toString(), likeBoth.getTo());
        });
        return profiles;
    }

    public FGraph fGraph(Set<String> ignoredStatuses) {
        return fGraph(ignoredStatuses, null);
    }

    public FGraph fGraph(Set<String> ignoredStatuses, List<Event> events) {

        // nodes
        Map<String, Node> nodes = new HashMap<>();
        this.likes.forEach(likeBoth -> {
            nodes.put(likeBoth.getFrom().getId().toString(),
                    new Node(likeBoth.getFrom().getId().toString(), likeBoth.getFrom().getGender()));
            nodes.put(likeBoth.getTo().getId().toString(),
                    new Node(likeBoth.getTo().getId().toString(), likeBoth.getTo().getGender()));
        });

        // edges
        List<Edge> edges = this.likes.stream().map(likeBoth -> {
            Node fromNode = new Node(likeBoth.getFrom().getId().toString(), likeBoth.getFrom().getGender());
            Node toNode = new Node(likeBoth.getTo().getId().toString(), likeBoth.getTo().getGender());
            double weight = (double) likeBoth.getRate();
            return new Edge(fromNode, toNode, weight);
        }).toList();

        // ignoring edges, where the profile is not with status 'A' or F
        Set<Edge> ignoredEdgesByStatus = this.likes.stream()
                .filter(ignoredLike -> !ignoredStatuses.contains(ignoredLike.getFrom().getStatus())
                        || !ignoredStatuses.contains(ignoredLike.getTo().getStatus()))
                .map(likeBoth -> {
                    Node fromNode = new Node(likeBoth.getFrom().getId().toString(), likeBoth.getFrom().getGender());
                    Node toNode = new Node(likeBoth.getTo().getId().toString(), likeBoth.getTo().getGender());
                    double weight = (double) (likeBoth.getRate() * likeBoth.getDistance());
                    return new Edge(fromNode, toNode, weight);
                }).collect(Collectors.toSet());

        List<Set<Edge>> ignoredEdges = new ArrayList<>();
        if (events != null) {
            events.stream().forEach(event -> {
                Set<Member> members = event.getMembers()
                        .stream()
                        .filter(member -> Member.MET.contains(member.getStatus()))
                        .collect(Collectors.toSet());

                Set<Edge> ignoredEdge = EventUtil.permutate(members);
                ignoredEdges.add(ignoredEdge);
            });
        }

        ignoredEdges.add(ignoredEdgesByStatus);

        FGraph fGraph = new FGraph(nodes, edges, new HashSet<>(), ignoredEdges);
        return fGraph;
    }
}

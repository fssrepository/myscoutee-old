package com.raxim.myscoutee.algo.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Rule;
import com.raxim.myscoutee.profile.util.EventUtil;

public class FGraph extends Graph {
    private Set<Node> ignoredNodes;
    private List<Set<Edge>> ignoredEdges;

    public FGraph() {
        this(null, null);
    }

    public FGraph(Map<String, Node> nodes, List<Edge> edges) {
        this(nodes, edges, null, null);
    }

    public FGraph(Map<String, Node> nodes, List<Edge> edges, Set<Node> ignoredNodes, List<Set<Edge>> ignoredEdges) {
        super(edges, nodes);
        this.ignoredNodes = ignoredNodes;
        this.ignoredEdges = ignoredEdges;
    }

    public List<Set<Edge>> getIgnoredEdges() {
        return ignoredEdges;
    }

    public void setIgnoredEdges(List<Set<Edge>> ignoredEdges) {
        this.ignoredEdges = ignoredEdges;
    }

    public Set<Node> getIgnoredNodes() {
        return ignoredNodes;
    }

    public void setIgnoredNodes(Set<Node> ignoredNodes) {
        this.ignoredNodes = ignoredNodes;
    }

    public FGraph filter(Event evt) {
        FGraph fg = this;

        Set<Member> members = evt.getMembers();
        Set<Member> candidates = evt.getCandidates();
        Rule rule = evt.getRule();

        Set<Edge> sIgnoredEdges = EventUtil.permutate(members);

        Set<Edge> sIgnoredEdgesByRate = fg.getEdges().stream()
                .filter(edge -> rule.getRate() != null &&
                        edge.getWeight() >= rule.getRate())
                .collect(Collectors.toSet());

        List<Set<Edge>> ignoredEdges = new ArrayList<>();
        ignoredEdges.add(sIgnoredEdges);
        ignoredEdges.add(sIgnoredEdgesByRate);
        ignoredEdges.addAll(fg.getIgnoredEdges());

        Set<Edge> possibleEdges = EventUtil.permutate(candidates);

        List<Edge> validEdges = fg.getEdges().stream()
                .filter(edge -> possibleEdges.contains(edge))
                .toList();

        Set<Node> ignoredNodes = members.stream()
                .filter(member -> "A".equals(member.getStatus())
                        || "I".equals(member.getStatus()))
                .map(member -> {
                    return new Node(member.getProfile().getId().toString(),
                            member.getProfile().getGender());
                })
                .collect(Collectors.toSet());

        FGraph fGraph = new FGraph(fg.getNodes(), validEdges, ignoredNodes, ignoredEdges);
        return fGraph;
    }

}

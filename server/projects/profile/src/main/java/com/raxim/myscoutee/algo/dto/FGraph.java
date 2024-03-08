package com.raxim.myscoutee.algo.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

}

package com.raxim.myscoutee.algo.dto;

import java.util.List;
import java.util.Map;

public class Graph {
    private final List<Edge> edges;
    private final Map<String, Node> nodes;
    public Graph(List<Edge> edges, Map<String, Node> nodes) {
        this.edges = edges;
        this.nodes = nodes;
    }
    public List<Edge> getEdges() {
        return edges;
    }
    public Map<String, Node> getNodes() {
        return nodes;
    }
    
}

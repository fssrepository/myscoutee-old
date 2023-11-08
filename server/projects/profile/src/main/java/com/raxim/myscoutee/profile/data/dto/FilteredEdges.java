package com.raxim.myscoutee.profile.data.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;

public class FilteredEdges {
    private final Map<String, Profile> nodes;
    private final List<Edge> edges;
    private final List<Set<Edge>> ignoredEdges;

    public FilteredEdges() {
        this(null, null, null);
    }

    public FilteredEdges(Map<String, Profile> nodes, List<Edge> edges, List<Set<Edge>> ignoredEdges) {
        this.nodes = nodes;
        this.edges = edges;
        this.ignoredEdges = ignoredEdges;
    }

    public Map<String, Profile> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public List<Set<Edge>> getIgnoredEdges() {
        return ignoredEdges;
    }

}

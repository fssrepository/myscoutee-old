package com.raxim.myscoutee.algo.dto;

import java.util.Map;

public class ObjGraph<T> {
    private final Map<String, T> nodes;
    private final FGraph fGraph;

    public ObjGraph() {
        this(null, null);
    }

    public ObjGraph(Map<String, T> nodes, FGraph fGraph) {
        this.nodes = nodes;
        this.fGraph = fGraph;
    }

    public FGraph getfGraph() {
        return fGraph;
    }

    public Map<String, T> getNodes() {
        return nodes;
    }
}

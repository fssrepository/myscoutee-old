package com.raxim.myscoutee.algo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.raxim.myscoutee.algo.dto.CGraph;
import com.raxim.myscoutee.algo.dto.CNode;
import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Node;

public class CTree implements Iterable<Edge> {
    public static final String DEFAULT_TYPE = "d";

    private final ConcurrentMap<String, CNode> nodes;
    private final PriorityQueue<CNode> nodesOrdered;

    private final List<String> types;
    private final List<Set<Edge>> ignoredEdges;
    private final Set<Node> disabledNodes;

    public CTree(CGraph cGraph) {
        this(cGraph, new ArrayList<>());
    }

    public CTree(CGraph cGraph, List<String> types) {
        this(cGraph, types, new ArrayList<>());
    }

    public CTree(CGraph cGraph, List<String> types, List<Set<Edge>> ignoredEdges) {
        this(cGraph, types, ignoredEdges, new HashSet<>());
    }

    public CTree(CGraph cGraph, List<String> types, List<Set<Edge>> ignoredEdges, Set<Node> disabledNodes) {
        if (!types.isEmpty()) {
            this.types = types;
        } else {
            this.types = List.of(DEFAULT_TYPE);
        }
        this.nodes = new ConcurrentHashMap<>();
        this.nodesOrdered = new PriorityQueue<>(
                Comparator.comparing(CNode::getWeight).reversed().thenComparing(CNode::getNode));
        cGraph.forEach(cNode -> {
            nodes.put(cNode.getNode().getId(), cNode);
            nodesOrdered.add(cNode);
        });
        this.ignoredEdges = ignoredEdges;
        this.disabledNodes = disabledNodes;
    }

    @Override
    public Iterator<Edge> iterator() {
        return new CTreeIterator(this, types);
    }

    public CNode getNode(String key) {
        return nodes.get(key);
    }

    public CNode poll() {
        return nodesOrdered.poll();
    }

    public boolean remove(CNode cNode) {
        return nodesOrdered.remove(cNode);
    }

    public List<String> getTypes() {
        return types;
    }

    public Set<Node> getDisabledNodes() {
        return disabledNodes;
    }

    public List<Set<Edge>> getIgnoredEdges() {
        return ignoredEdges;
    }

    public boolean isEmpty() {
        return this.nodesOrdered.isEmpty();
    }

    public boolean isIgnoredEdge(Edge edge) {
        return this.ignoredEdges.stream().anyMatch(edges -> edges.contains(edge));
    }

    public boolean isDisabledNode(Node node) {
        return this.disabledNodes.contains(node);
    }

}

package com.raxim.myscoutee.algo.dto;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.raxim.myscoutee.algo.CGraphIterator;
import com.raxim.myscoutee.common.merge.Mergeable;

/*
 * connected graph
 */
public class CGraph implements Mergeable<CGraph>, Comparable<CGraph>, Iterable<Edge> {
    private final ConcurrentMap<String, CNode> nodes;
    private final PriorityQueue<CNode> nodesOrdered;

    public CGraph() {
        this.nodes = new ConcurrentHashMap<>();
        this.nodesOrdered = new PriorityQueue<>(
                Comparator.comparing(CNode::getWeight).reversed().thenComparing(CNode::getNode));
    }

    public void addAll(Collection<Edge> edges) {
        edges.forEach(edge -> {
            add(edge);

            Edge edgeReversed = new Edge(edge.getTo(), edge.getFrom(), edge.getWeight());
            add(edgeReversed);
        });
    }

    public void add(Edge edge) {
        if (!this.nodes.containsKey(edge.getFrom().getId())) {
            this.nodes.put(edge.getFrom().getId(), new CNode(edge.getFrom()));
        }

        CNode aNode = this.nodes.get(edge.getFrom().getId());
        aNode.add(edge);

        this.nodesOrdered.remove(aNode);
        this.nodesOrdered.add(aNode);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CGraph other = (CGraph) obj;
        if (nodes == null) {
            if (other.nodes != null)
                return false;
        } else if (!nodes.equals(other.nodes)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canMerge(CGraph other) {
        if (nodes != null && other.nodes != null) {
            Set<String> keys1 = new HashSet<String>(nodes.keySet());
            Set<String> keys2 = new HashSet<String>(other.nodes.keySet());
            keys1.retainAll(keys2);
            return !keys1.isEmpty();
        }
        return false;
    }

    @Override
    public void merge(CGraph node) {
        this.nodes.putAll(node.nodes);
        this.nodesOrdered.addAll(node.nodesOrdered);
    }

    @Override
    public int compareTo(CGraph arg0) {
        return Integer.compare(this.hashCode(), arg0.hashCode());
    }

    @Override
    public String toString() {
        return "CGraph [nodesOrdered=" + nodesOrdered + "]";
    }

    @Override
    public Iterator<Edge> iterator() {
        return new CGraphIterator(this);
    }

    public boolean isEmpty() {
        return nodesOrdered.isEmpty();
    }

    public int size() {
        return nodesOrdered.size();
    }

    public ConcurrentMap<String, CNode> getNodes() {
        return nodes;
    }

    public PriorityQueue<CNode> getNodesOrdered() {
        return nodesOrdered;
    }
}

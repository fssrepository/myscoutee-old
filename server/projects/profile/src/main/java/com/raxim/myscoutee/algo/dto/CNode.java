package com.raxim.myscoutee.algo.dto;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.raxim.myscoutee.common.merge.Mergeable;

public class CNode implements Mergeable<CNode>, Comparable<CNode> {

    private final Node node;
    private PriorityQueue<Edge> edgesOrdered;

    public CNode(Node node) {
        this.node = node;
        this.edgesOrdered = new PriorityQueue<>(
                Comparator.comparing(Edge::getWeight).reversed().thenComparing(Edge::getFrom));
    }

    public Node getNode() {
        return node;
    }

    public int getDegree() {
        return edgesOrdered.size();
    }

    public Edge peek() {
        return edgesOrdered.peek();
    }

    public Edge poll() {
        Edge edge = edgesOrdered.poll();
        return edge;
    }

    public void add(Edge edge) {
        edgesOrdered.add(edge);
    }

    public void remove(Edge edge) {
        edgesOrdered.remove(edge);
    }

    public long getWeight() {
        return !this.edgesOrdered.isEmpty() ? this.peek().getWeight() : -1;
    }

    @Override
    public String toString() {
        return "CNode [id=" + node + ", degree=" + this.getDegree() + ", toNodes=" + edgesOrdered + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((node == null) ? 0 : node.hashCode());
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
        CNode other = (CNode) obj;
        if (node == null) {
            if (other.getNode() != null)
                return false;
        } else if (!node.equals(other.getNode()))
            return false;
        return true;
    }

    @Override
    public int compareTo(CNode arg0) {
        return Integer.compare(this.hashCode(), arg0.hashCode());
    }

    @Override
    public boolean canMerge(CNode other) {
        return this.getNode().getId() == other.getNode().getId();
    }

    @Override
    public void merge(CNode node) {
        this.edgesOrdered = Stream.concat(edgesOrdered.stream(), node.edgesOrdered.stream())
                .collect(Collectors.toCollection(() -> new PriorityQueue<>(
                        Comparator.comparing(Edge::getWeight).reversed().thenComparing(Edge::getFrom))));
    }

}

package com.raxim.myscoutee.algo.dto;

import java.util.Comparator;
import java.util.PriorityQueue;

public class CNode {

    private final Node node;
    private final PriorityQueue<Edge> edgesOrdered;

    public CNode(Node node) {
        this.node = node;
        this.edgesOrdered = new PriorityQueue<>(
                Comparator.comparing(Edge::getWeight).reversed());
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
        return edgesOrdered.poll();
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

}

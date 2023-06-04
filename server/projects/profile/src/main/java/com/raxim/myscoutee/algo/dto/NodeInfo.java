package com.raxim.myscoutee.algo.dto;

import java.util.PriorityQueue;

public class NodeInfo {

    private final String id;
    private final PriorityQueue<WeightNode> toNodes;

    public NodeInfo(String id, PriorityQueue<WeightNode> toNodes) {
        this.id = id;
        this.toNodes = toNodes;
    }

    public String getId() {
        return id;
    }

    public int getDegree() {
        return toNodes.size();
    }

    public WeightNode peek() {
        return toNodes.peek();
    }

    public WeightNode poll() {
        return toNodes.poll();
    }

    public void add(WeightNode weightNode) {
        toNodes.add(weightNode);
    }

    public void remove(WeightNode weightNode) {
        toNodes.remove(weightNode);
    }

    public long getWeight() {
        return !this.toNodes.isEmpty() ? this.peek().getWeight() : -1;
    }

    @Override
    public String toString() {
        return "NodeInfo [id=" + id + ", degree=" + this.getDegree() + ", toNodes=" + toNodes + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        NodeInfo other = (NodeInfo) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}

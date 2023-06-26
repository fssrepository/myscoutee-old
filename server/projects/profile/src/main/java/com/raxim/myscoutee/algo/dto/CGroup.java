package com.raxim.myscoutee.algo.dto;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class CGroup {
    private final AtomicLong weight;
    private final List<Node> nodes;
    private int partition;

    public CGroup(List<Node> nodes) {
        this.weight = new AtomicLong(0L);
        this.nodes = nodes;
        this.partition = -1;
    }

    public AtomicLong getWeight() {
        return weight;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public int getPartition() {
        return partition;
    }

    @Override
    public String toString() {
        return "Group [weight=" + weight + ", nodes=" + nodes + ", partition=" + partition + "]";
    }
}

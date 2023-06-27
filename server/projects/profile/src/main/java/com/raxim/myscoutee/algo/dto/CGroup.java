package com.raxim.myscoutee.algo.dto;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class CGroup extends LinkedHashSet<Node> {
    private final AtomicLong weight;

    public CGroup() {
        this(new HashSet<>());
    }

    public CGroup(Set<Node> nodes) {
        super(nodes);
        this.weight = new AtomicLong(0L);
    }

    public void add(Edge edge) {
        this.weight.addAndGet(edge.getWeight());
        if (edge.getFrom() != null) {
            super.add(edge.getFrom());
        }
        if (edge.getTo() != null) {
            super.add(edge.getTo());
        }
    }

    @Override
    public String toString() {
        return "Group [weight=" + weight + ", nodes=" + super.toString() + "]";
    }
}

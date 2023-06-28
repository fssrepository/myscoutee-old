package com.raxim.myscoutee.algo.dto;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.util.concurrent.AtomicDouble;

public class CGroup extends LinkedHashSet<Node> {
    private final AtomicDouble weight;

    private Set<String> types = new HashSet<>();

    public CGroup() {
        this(new HashSet<>());
    }

    public CGroup(Set<Node> nodes) {
        super(nodes);
        this.weight = new AtomicDouble(0D);
    }

    public void add(Edge edge) {
        this.weight.addAndGet(edge.getWeight());
        if (edge.getFrom() != null) {
            super.add(edge.getFrom());
            if (edge.getFrom().getType() != null) {
                types.add(edge.getFrom().getType());
            }
        }
        if (edge.getTo() != null) {
            super.add(edge.getTo());

            if (edge.getTo().getType() != null) {
                types.add(edge.getTo().getType());
            }
        }
    }

    public int balance(List<String> pTypes) {
        if (pTypes.isEmpty() || pTypes.size() < 2) {
            return 0;
        }

        int oldSize = this.size();
        if (pTypes.size() != types.size()) {
            super.clear();
            return oldSize;
        }

        Map<String, List<Node>> nodesByType = this.stream()
                .collect(Collectors.groupingBy(Node::getType));

        int minSize = nodesByType.values().stream()
                .mapToInt(List::size)
                .min()
                .orElse(0); // Minimum size of any type

        Set<Node> nodes = nodesByType.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().limit(minSize))
                .collect(Collectors.toSet());

        super.clear();
        super.addAll(nodes);

        return oldSize - this.size();
    }

    @Override
    public String toString() {
        return "Group [weight=" + weight + ", nodes=" + super.toString() + "]";
    }
}

package com.raxim.myscoutee.algo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.raxim.myscoutee.algo.dto.CGroup;
import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.algo.dto.Range;

public class LCTreeIterator implements Iterator<CGroup> {

    private final Range range;
    private final CTreeIterator cTreeIterator;

    private CGroup cGroup = new CGroup();
    private CGroup cTree = new CGroup();

    public LCTreeIterator(CTreeIterator cTreeIterator,
            final Range range) {

        this.cTreeIterator = cTreeIterator;

        int min = range.getMin() < 2 ? 2 : range.getMin() + (range.getMax() % cTreeIterator.getTypes().size());
        int max = range.getMax() - (range.getMax() % cTreeIterator.getTypes().size());
        // 'max' should be dividable by number of types
        this.range = new Range(min, max);
    }

    @Override
    public boolean hasNext() {

        while (cTreeIterator.hasNext()
                && cGroup.size() < this.range.getMax()) {
            Edge edge = cTreeIterator.next();

            Node from = edge.getFrom();
            if (cTree.contains(edge.getFrom())) {
                from = null;
            }

            Node to = edge.getTo();
            if (cTree.contains(edge.getTo())) {
                to = null;
            }

            edge = new Edge(from, to, edge.getWeight());

            cGroup.add(edge);
            cTree.add(edge);
        }

        cGroup = balanceGroup();

        return cGroup.size() >= this.range.getMin()
                && (cGroup.size() % cTreeIterator.getTypes().size() == 0);
    }

    private CGroup balanceGroup() {
        Map<String, List<Node>> nodesByType = cGroup.stream().collect(Collectors.groupingBy(Node::getType));

        int minSize = nodesByType.values().stream()
                .mapToInt(List::size)
                .min()
                .orElse(0); // Minimum size of any type

        Set<Node> nodes = nodesByType.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().limit(minSize))
                .collect(Collectors.toSet());

        return new CGroup(nodes);
    }

    @Override
    public CGroup next() {
        CGroup result = (CGroup) cGroup.clone();
        cGroup = new CGroup();
        return result;
    }
}

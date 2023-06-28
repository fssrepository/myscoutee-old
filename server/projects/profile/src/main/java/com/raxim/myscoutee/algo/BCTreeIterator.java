package com.raxim.myscoutee.algo;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import com.raxim.myscoutee.algo.dto.CGroup;
import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.algo.dto.Range;

public class BCTreeIterator implements Iterator<CGroup> {

    private final Range range;
    private final CTreeIterator cTreeIterator;

    private CGroup cGroup = new CGroup();
    private CGroup cTree = new CGroup();

    public BCTreeIterator(CTreeIterator cTreeIterator,
            final Range range) {

        this.cTreeIterator = cTreeIterator;

        int min = range.getMin() < 2 ? 2 : range.getMin() + (range.getMin() % cTreeIterator.getTypes().size());
        int max = range.getMax() - (range.getMax() % cTreeIterator.getTypes().size());
        // 'max' should be dividable by number of types
        this.range = new Range(min, max);
    }

    @Override
    public boolean hasNext() {
        Set<Edge> edges = new HashSet<>();

        while (cGroup.size() < this.range.getMax()
                && cTreeIterator.hasNext()) {

            Node from = null;
            Node to = null;
            double weight = 0;

            Edge edge;
            int counter = 0;
            do {
                edge = cTreeIterator.next();
                edges.add(edge);

                if (from == null && !cTree.contains(edge.getFrom())) {
                    from = edge.getFrom();
                }

                if (to == null && !cTree.contains(edge.getTo())) {
                    to = edge.getTo();
                }

                weight += edge.getWeight();
                counter++;
            } while ((edge.getFrom() == null || edge.getTo() == null)
                    && cTreeIterator.hasNext());

            edge = new Edge(from, to, weight / counter, edge.isIgnored());

            cGroup.add(edge);
            cTree.add(edge);
        }
        int oldSize = cGroup.size();
        Set<Node> nodes = cGroup.balance(cTreeIterator.getTypes());
        int newSize = nodes.size();
        System.out.println("---------------" + (oldSize - newSize));

        //edges not used - it might be needed to put it back
        Set<Edge> rEdges = edges.stream().filter(edge -> !nodes.contains(edge.getFrom())
                && !nodes.contains(edge.getTo())).collect(Collectors.toSet());
        System.out.println(rEdges);
        // cTreeIterator.getUsed().addAll(nodes);

        return cGroup.size() >= this.range.getMin()
                && (cGroup.size() % cTreeIterator.getTypes().size() == 0);
    }

    @Override
    public CGroup next() {
        CGroup result = (CGroup) cGroup.clone();
        cGroup = new CGroup();
        return result;
    }
}

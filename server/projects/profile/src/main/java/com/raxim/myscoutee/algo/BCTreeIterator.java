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

    private final BCTree bcTree;
    private final Range range;
    private final CTreeIterator cTreeIterator;

    private CGroup cGroup = new CGroup();
    private CGroup cTree = new CGroup();

    public BCTreeIterator(BCTree bcTree,
            final Range range) {

        this.bcTree = bcTree;
        this.cTreeIterator = (CTreeIterator) bcTree.getcTree().iterator();

        int min = range.getMin() < 2 ? 2 : range.getMin() + (range.getMin() % cTreeIterator.getTypes().size());
        int max = range.getMax() - (range.getMax() % cTreeIterator.getTypes().size());
        // 'max' should be dividable by number of types
        this.range = new Range(min, max);
    }

    public boolean hasAnyNext() {
        handleGroup();
        return hasAnyRemaining();
    }

    @Override
    public boolean hasNext() {
        handleGroup();

        return hasRemaining();
    }

    private void handleGroup() {
        System.out.println("--------- start -------");
        Set<Edge> edges = new HashSet<>();

        while (cGroup.size() < this.range.getMax()
                && cTreeIterator.hasNext()) {

            Node from = null;
            Node to = null;

            Edge edge;
            do {
                edge = cTreeIterator.next();
                edges.add(edge);

                if (from == null && !cTree.contains(edge.getFrom())) {
                    from = edge.getFrom();
                }

                if (to == null && !cTree.contains(edge.getTo())) {
                    to = edge.getTo();
                }

                cGroup.add(edge);

            } while ((edge.getFrom() == null || edge.getTo() == null)
                    && cTreeIterator.hasNext());

            if (!bcTree.getUsedNodes().contains(from)) {
                cGroup.add(from);
            }
            if (!bcTree.getUsedNodes().contains(to)) {
                cGroup.add(to);
            }
            cTree.addAll(cGroup);
        }
        // System.out.println(cGroup);
        int oldSize = cGroup.size();
        Set<Node> nodes = cGroup.balance(cTreeIterator.getTypes());
        int newSize = nodes.size();

        System.out.println(oldSize - newSize);

        // edges not used - it might be needed to put it back
        Set<Edge> rEdges = edges.stream().filter(edge -> !nodes.contains(edge.getFrom())
                && !nodes.contains(edge.getTo())).collect(Collectors.toSet());
        // cTreeIterator.getUsed().addAll(nodes);

        if (!hasRemaining()) {
            System.out.println("--------- end -------");
        }
    }

    private boolean hasRemaining() {
        return cGroup.size() >= this.range.getMin()
                && (cGroup.size() % cTreeIterator.getTypes().size() == 0);
    }

    private boolean hasAnyRemaining() {
        return cGroup.size() > 0
                && (cGroup.size() % cTreeIterator.getTypes().size() == 0);
    }

    @Override
    public CGroup next() {
        System.out.println(cGroup);

        CGroup result = (CGroup) cGroup.clone();
        cGroup = new CGroup();

        System.out.println("--------- end -------");
        return result;
    }
}

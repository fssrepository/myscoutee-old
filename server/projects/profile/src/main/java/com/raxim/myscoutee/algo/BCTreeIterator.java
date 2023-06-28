package com.raxim.myscoutee.algo;

import java.util.Iterator;

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

        while (cGroup.size() < this.range.getMax()
                && cTreeIterator.hasNext()) {
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

        System.out.println(cGroup.balance(cTreeIterator.getTypes()));

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

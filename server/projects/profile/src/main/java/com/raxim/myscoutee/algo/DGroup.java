package com.raxim.myscoutee.algo;

import java.util.Iterator;

import com.raxim.myscoutee.algo.dto.CGraph;
import com.raxim.myscoutee.algo.dto.CGroup;
import com.raxim.myscoutee.algo.dto.Range;

public class DGroup implements Iterable<CGroup> {

    private final CGraph cGraph;
    private final Range range;

    public DGroup(final CGraph cGraph, final Range range) {
        this.cGraph = cGraph;

        int minGroupSize = range.getMin() < 2 ? 2 : range.getMin();
        int maxGroupSize = range.getMax();
        this.range = new Range(minGroupSize, maxGroupSize);
    }

    @Override
    public Iterator<CGroup> iterator() {
        return new DGroupIterator(this);
    }

    public CGraph getCGraph() {
        return cGraph;
    }

    public Range getRange() {
        return range;
    }

    @Override
    public String toString() {
        return this.cGraph.toString();
    }
}

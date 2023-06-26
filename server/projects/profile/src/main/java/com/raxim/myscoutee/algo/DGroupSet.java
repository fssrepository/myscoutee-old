package com.raxim.myscoutee.algo;

import java.util.Iterator;

import com.raxim.myscoutee.algo.dto.CGroup;
import com.raxim.myscoutee.algo.dto.Range;

public class DGroupSet implements Iterable<CGroup> {

    private final NodeRepository nodeRepository;
    private final Range range;

    public DGroupSet(final NodeRepository nodeRepository, final Range range) {
        this.nodeRepository = nodeRepository;

        int minGroupSize = range.getMin() < 2 ? 2 : range.getMin();
        int maxGroupSize = range.getMax();
        this.range = new Range(minGroupSize, maxGroupSize);
    }

    @Override
    public Iterator<CGroup> iterator() {
        return new DGroupSetIterator(this);
    }

    public NodeRepository getNodeRepository() {
        return nodeRepository;
    }

    public Range getRange() {
        return range;
    }

    @Override
    public String toString() {
        return this.nodeRepository.getNodeForest().toString();
    }
}

package com.raxim.myscoutee.algo;

import java.util.Iterator;

import com.raxim.myscoutee.algo.dto.CGroup;
import com.raxim.myscoutee.algo.dto.Range;

public class LCTree implements Iterable<CGroup> {

    private final CTree cTree;
    private final Range range;

    public LCTree(CTree cTree, Range range) {
        this.cTree = cTree;
        this.range = range;
    }

    @Override
    public Iterator<CGroup> iterator() {
        return new LCTreeIterator((CTreeIterator) this.cTree.iterator(), range);
    }
}

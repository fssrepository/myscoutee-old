package com.raxim.myscoutee.algo;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.raxim.myscoutee.algo.dto.CGroup;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.algo.dto.Range;

public class BCTree implements Iterable<CGroup> {

    private final CTree cTree;
    private final Range range;
    private final Set<Node> usedNodes;

    public BCTree(CTree cTree, Range range) {
        this(cTree, range, new HashSet<>());
    }

    public BCTree(CTree cTree, Range range, Set<Node> usedNodes) {
        this(cTree, range, usedNodes, false);

    }

    public BCTree(CTree cTree, Range range, Set<Node> usedNodes, boolean inclNodes) {
        this.cTree = cTree;
        if (inclNodes == true) {
            this.range = range.add(-usedNodes.size());
        } else {
            this.range = range;
        }
        this.usedNodes = usedNodes;
    }

    @Override
    public Iterator<CGroup> iterator() {
        return new BCTreeIterator(this, range);
    }

    public CTree getcTree() {
        return cTree;
    }

    public Set<Node> getUsedNodes() {
        return usedNodes;
    }
}

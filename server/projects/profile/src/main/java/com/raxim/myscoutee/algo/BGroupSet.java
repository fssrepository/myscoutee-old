package com.raxim.myscoutee.algo;

import java.util.Iterator;
import java.util.List;

import com.raxim.myscoutee.algo.dto.CGroup;
import com.raxim.myscoutee.algo.dto.Range;

public class BGroupSet extends GroupSet {
    private final List<String> types;

    public BGroupSet(final NodeRepository nodeRepository, final Range range, final List<String> types) {
        super(nodeRepository, range);
        this.types = types;
    }

    @Override
    public Iterator<CGroup> iterator() {
        return new BGroupSetIterator(this);
    }

    public List<String> getTypes() {
        return types;
    }

}

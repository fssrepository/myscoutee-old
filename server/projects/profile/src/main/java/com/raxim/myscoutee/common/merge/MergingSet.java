package com.raxim.myscoutee.common.merge;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class MergingSet<T extends Mergeable<T>> extends HashSet<T> {

    public MergingSet() {
        super();
    }

    public MergingSet(Collection<T> c) {
        super(c);
    }

    @Override
    public boolean add(T element) {
        for (Iterator<T> iterator = this.iterator(); iterator.hasNext();) {
            T existingElement = iterator.next();
            if (existingElement.canMerge(element)) {
                existingElement.merge(element);
                iterator.remove();
                return add(existingElement);
            }
        }
        return super.add(element);
    }
}

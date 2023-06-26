package com.raxim.myscoutee.common.merge;

import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;

public class MergingSet<T extends Mergeable<T>> extends ConcurrentSkipListSet<T> {
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

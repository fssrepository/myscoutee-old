package com.raxim.myscoutee.common.merge;

public interface Mergeable<T> {
    boolean canMerge(T other);

    void merge(T node);
}

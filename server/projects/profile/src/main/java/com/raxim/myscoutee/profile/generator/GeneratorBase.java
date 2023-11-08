package com.raxim.myscoutee.profile.generator;

import java.util.List;

import com.raxim.myscoutee.profile.data.dto.FilteredEdges;

public abstract class GeneratorBase<T> {
    private final FilteredEdges filteredEdges;
    private final Object flags;

    public GeneratorBase(FilteredEdges filteredEdges, Object flags) {
        this.filteredEdges = filteredEdges;
        this.flags = flags;
    }

    public FilteredEdges getFilteredEdges() {
        return filteredEdges;
    }

    public Object getFlags() {
        return flags;
    }

    public abstract List<T> generate();
}

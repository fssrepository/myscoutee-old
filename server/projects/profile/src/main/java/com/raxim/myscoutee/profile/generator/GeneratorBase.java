package com.raxim.myscoutee.profile.generator;

import java.util.List;

import com.raxim.myscoutee.algo.dto.ObjGraph;

public abstract class GeneratorBase<T, P> {
    private final ObjGraph<P> objGraph;
    private final Object flags;

    public GeneratorBase(ObjGraph<P> nodeRepository, Object flags) {
        this.objGraph = nodeRepository;
        this.flags = flags;
    }

    public ObjGraph<P> getObjGraph() {
        return objGraph;
    }

    public Object getFlags() {
        return flags;
    }

    public abstract List<T> generate();
}

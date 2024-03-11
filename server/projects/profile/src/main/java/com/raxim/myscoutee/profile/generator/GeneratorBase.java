package com.raxim.myscoutee.profile.generator;

import java.util.List;
import java.util.Map;

import com.raxim.myscoutee.algo.dto.FGraph;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;

public abstract class GeneratorBase<T, P> {
    private final FGraph fGraph;
    private final Map<String, Profile> profiles;
    private final Object flags;

    public GeneratorBase(FGraph fGraph, Map<String, Profile> profiles, Object flags) {
        this.fGraph = fGraph;
        this. profiles = profiles;
        this.flags = flags;
    }

    public FGraph getfGraph() {
        return fGraph;
    }

    public Profile getProfileById(String id) {
        return this.profiles.get(id);
    }

    public Object getFlags() {
        return flags;
    }

    public abstract List<T> generate();
}

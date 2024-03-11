package com.raxim.myscoutee.profile.generator;

import java.util.List;
import java.util.Map;

import com.raxim.myscoutee.algo.dto.FGraph;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;

public abstract class GeneratorBase<T, P> {
    private final FGraph fGraph;
    private final Map<String, Profile> profiles;

    public GeneratorBase(FGraph fGraph, Map<String, Profile> profiles) {
        this.fGraph = fGraph;
        this.profiles = profiles;
    }

    public FGraph getfGraph() {
        return fGraph;
    }

    public Profile getProfileById(String id) {
        return this.profiles.get(id);
    }

    public abstract List<T> generate(Object flags);
}

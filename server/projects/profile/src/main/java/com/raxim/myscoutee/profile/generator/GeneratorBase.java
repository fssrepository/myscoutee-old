package com.raxim.myscoutee.profile.generator;

import java.util.List;
import java.util.Map;

import com.raxim.myscoutee.algo.dto.FGraph;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;

public abstract class GeneratorBase<T, P> {
    private final FGraph graph;
    private final Map<String, Profile> profiles;

    public GeneratorBase(FGraph graph, Map<String, Profile> profiles) {
        this.graph = graph;
        this.profiles = profiles;
    }

    public FGraph getGraph() {
        return graph;
    }

    public Profile getProfileById(String id) {
        return this.profiles.get(id);
    }

    public abstract List<T> generate(Object flags);
}

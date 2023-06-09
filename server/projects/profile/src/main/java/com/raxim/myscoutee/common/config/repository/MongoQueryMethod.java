package com.raxim.myscoutee.common.config.repository;

import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;

public class MongoQueryMethod extends org.springframework.data.mongodb.repository.query.MongoQueryMethod {

    private final Map<String, String[]> queryCache;

    public MongoQueryMethod(Method method,
            RepositoryMetadata metadata,
            ProjectionFactory projectionFactory,
            MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext,
            Map<String, String[]> queryCache) {
        super(method, metadata, projectionFactory, mappingContext);
        this.queryCache = queryCache;
    }

    public boolean hasAnnotatedAggregation() {
        boolean hasAnnotatedAggregation = super.hasAnnotatedAggregation();
        if (hasAnnotatedAggregation) {
            String[] pipelines = super.getAnnotatedAggregation();
            if (!pipelines[0].contains("{")) {
                hasAnnotatedAggregation = queryCache.containsKey(pipelines[0]);
            }
        }
        return hasAnnotatedAggregation;
    }

    @Override
    public String[] getAnnotatedAggregation() {
        String[] pipelines = super.getAnnotatedAggregation();
        if (!pipelines[0].contains("{")) {
            pipelines = queryCache.get(pipelines[0]);
        }
        return pipelines;
    }
}

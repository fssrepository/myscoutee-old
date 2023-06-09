package com.raxim.myscoutee.common.config.repository;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.query.PartTreeMongoQuery;
import org.springframework.data.mongodb.repository.query.StringBasedAggregation;
import org.springframework.data.mongodb.repository.query.StringBasedMongoQuery;
import org.springframework.data.mongodb.repository.support.QuerydslMongoPredicateExecutor;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.QuerydslUtils;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition.RepositoryFragments;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class MongoRepositoryFactory extends RepositoryFactorySupport {

    private static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    private final MongoOperations operations;
    private final MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext;
    private final Map<String, String[]> queryCache;

    public MongoRepositoryFactory(MongoOperations mongoOperations, Map<String, String[]> queryCache) {

        Assert.notNull(mongoOperations, "MongoOperations must not be null");

        this.operations = mongoOperations;
        this.mappingContext = mongoOperations.getConverter().getMappingContext();
        this.queryCache = queryCache;
    }

    @Override
    protected ProjectionFactory getProjectionFactory(ClassLoader classLoader, BeanFactory beanFactory) {
        return this.operations.getConverter().getProjectionFactory();
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return SimpleMongoRepository.class;
    }

    @Override
    protected RepositoryFragments getRepositoryFragments(RepositoryMetadata metadata) {
        return getRepositoryFragments(metadata, operations);
    }

    protected RepositoryFragments getRepositoryFragments(RepositoryMetadata metadata, MongoOperations operations) {

        boolean isQueryDslRepository = QuerydslUtils.QUERY_DSL_PRESENT
                && QuerydslPredicateExecutor.class.isAssignableFrom(metadata.getRepositoryInterface());

        if (isQueryDslRepository) {

            if (metadata.isReactiveRepository()) {
                throw new InvalidDataAccessApiUsageException(
                        "Cannot combine Querydsl and reactive repository support in a single interface");
            }

            return RepositoryFragments
                    .just(new QuerydslMongoPredicateExecutor<>(getEntityInformation(metadata.getDomainType()),
                            operations));
        }

        return RepositoryFragments.empty();
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation information) {

        MongoEntityInformation<?, Serializable> entityInformation = getEntityInformation(information.getDomainType(),
                information);
        return getTargetRepositoryViaReflection(information, entityInformation, operations);
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(@Nullable Key key,
            QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return Optional
                .of(new MongoQueryLookupStrategy(operations, evaluationContextProvider, mappingContext, queryCache));
    }

    public <T, ID> MongoEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        return getEntityInformation(domainClass, null);
    }

    private <T, ID> MongoEntityInformation<T, ID> getEntityInformation(Class<T> domainClass,
            @Nullable RepositoryMetadata metadata) {

        MongoPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(domainClass);
        return MongoEntityInformationSupport.<T, ID>entityInformationFor(entity,
                metadata != null ? metadata.getIdType() : null);
    }

    private static class MongoQueryLookupStrategy implements QueryLookupStrategy {

        private final MongoOperations operations;
        private final QueryMethodEvaluationContextProvider evaluationContextProvider;
        private final MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext;
        private final ExpressionParser expressionParser = new CachingExpressionParser(EXPRESSION_PARSER);
        private final Map<String, String[]> queryCache;

        public MongoQueryLookupStrategy(MongoOperations operations,
                QueryMethodEvaluationContextProvider evaluationContextProvider,
                MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext,
                Map<String, String[]> queryCache) {

            this.operations = operations;
            this.evaluationContextProvider = evaluationContextProvider;
            this.mappingContext = mappingContext;
            this.queryCache = queryCache;
        }

        @Override
        public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
                NamedQueries namedQueries) {

            MongoQueryMethod queryMethod = new MongoQueryMethod(method, metadata, factory, mappingContext, queryCache);
            queryMethod.verify();

            String namedQueryName = queryMethod.getNamedQueryName();

            if (namedQueries.hasQuery(namedQueryName)) {
                String namedQuery = namedQueries.getQuery(namedQueryName);
                return new StringBasedMongoQuery(namedQuery, queryMethod, operations, expressionParser,
                        evaluationContextProvider);
            } else if (queryMethod.hasAnnotatedAggregation()) {
                return new StringBasedAggregation(queryMethod, operations, expressionParser, evaluationContextProvider);
            } else if (queryMethod.hasAnnotatedQuery()) {
                return new StringBasedMongoQuery(queryMethod, operations, expressionParser, evaluationContextProvider);
            } else {
                return new PartTreeMongoQuery(queryMethod, operations, expressionParser, evaluationContextProvider);
            }
        }
    }
}
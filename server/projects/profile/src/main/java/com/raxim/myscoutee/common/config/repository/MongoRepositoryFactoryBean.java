package com.raxim.myscoutee.common.config.repository;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bson.Document;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MongoRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
		extends RepositoryFactoryBeanSupport<T, S, ID> {

	private @Nullable MongoOperations operations;
	private boolean createIndexesForQueryMethods = false;
	private boolean mappingContextConfigured = false;

	public MongoRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
	}

	public void setMongoOperations(MongoOperations operations) {
		this.operations = operations;
	}

	public void setCreateIndexesForQueryMethods(boolean createIndexesForQueryMethods) {
		this.createIndexesForQueryMethods = createIndexesForQueryMethods;
	}

	@Override
	public void setMappingContext(MappingContext<?, ?> mappingContext) {

		super.setMappingContext(mappingContext);
		this.mappingContextConfigured = true;
	}

	@Override
	protected RepositoryFactorySupport createRepositoryFactory() {

		Map<String, String[]> queryMap = loadQueries();

		RepositoryFactorySupport factory = getFactoryInstance(operations, queryMap);

		if (createIndexesForQueryMethods) {
			factory.addQueryCreationListener(
					new IndexEnsuringQueryCreationListener(
							(collectionName, javaType) -> operations.indexOps(javaType)));
		}

		return factory;
	}

	protected RepositoryFactorySupport getFactoryInstance(MongoOperations operations, Map<String, String[]> queryMap) {
		return new MongoRepositoryFactory(operations, queryMap);
	}

	@Override
	public void afterPropertiesSet() {

		super.afterPropertiesSet();
		Assert.state(operations != null, "MongoTemplate must not be null");

		if (!mappingContextConfigured) {
			setMappingContext(operations.getConverter().getMappingContext());
		}
	}

	private Map<String, String[]> loadQueries() {
		Map<String, String[]> queryMap = new HashMap<>();
		try {
			String repositoryName = getObjectType().getSimpleName().replaceFirst("Repository", "").toLowerCase();
			URI dir = getClass().getResource("/mongo/" + repositoryName).toURI();
			ObjectMapper objectMapper = new ObjectMapper();

			try (Stream<Path> stream = Files.list(Paths.get(dir))) {
				stream
						.filter(file -> !Files.isDirectory(file))
						.forEach(file -> {
							try {
								try (Stream<String> lines = Files.lines(file, Charset.forName("UTF-8"))) {
									String line = lines.collect(Collectors.joining());

									List<Document> dStages = objectMapper.readValue(line,
											new TypeReference<List<Document>>() {
											});
									String[] stages = dStages.stream().map(pipeline -> pipeline.toJson())
											.toArray(String[]::new);

									String name = file.getFileName().toString();
									String fName = name.substring(0, name.lastIndexOf("."));
									queryMap.putIfAbsent(fName, stages);
								}

							} catch (IOException e) {
								e.printStackTrace();
							}
						});
			}
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
		return queryMap;
	}

}
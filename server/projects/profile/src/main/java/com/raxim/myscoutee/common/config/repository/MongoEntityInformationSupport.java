package com.raxim.myscoutee.common.config.repository;

import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.MappingMongoEntityInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public final class MongoEntityInformationSupport {

	private MongoEntityInformationSupport() {}

	/**
	 * Factory method for creating {@link MongoEntityInformation}.
	 *
	 * @param entity must not be {@literal null}.
	 * @param idType can be {@literal null}.
	 * @return never {@literal null}.
	 */
	@SuppressWarnings("unchecked")
	public static <T, ID> MongoEntityInformation<T, ID> entityInformationFor(MongoPersistentEntity<?> entity,
			@Nullable Class<?> idType) {

		Assert.notNull(entity, "Entity must not be null");

		return new MappingMongoEntityInformation<>((MongoPersistentEntity<T>) entity, (Class<ID>) idType);
	}
}

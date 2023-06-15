package com.raxim.myscoutee.common.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bson.BsonBinary;
import org.bson.BsonBinarySubType;
import org.bson.BsonType;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectionCodecProvider;
import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.IterableCodecProvider;
import org.bson.codecs.MapCodecProvider;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertManyOptions;
import com.raxim.myscoutee.common.util.FileUtil;

public class MongoDataLoaderTestExecutionListener extends AbstractTestExecutionListener {

    private final static BsonTypeClassMap TYPE_MAP = new BsonTypeClassMap(Map.of(BsonType.BINARY, UUID.class));
    private static final Codec<Document> UUID_CODEC = CodecRegistries
            .withUuidRepresentation(CodecRegistries.fromProviders(Arrays.asList(new ValueCodecProvider(),
                    new CollectionCodecProvider(TYPE_MAP), new IterableCodecProvider(TYPE_MAP),
                    new BsonValueCodecProvider(), new DocumentCodecProvider(TYPE_MAP),
                    new MapCodecProvider(TYPE_MAP))),
                    UuidRepresentation.JAVA_LEGACY)
            .get(Document.class);

    private boolean setupRequired = true;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void beforeTestClass(final TestContext testContext) throws Exception {
        if (setupRequired) {
            setupRequired = false;
            ApplicationContext applicationContext = testContext.getApplicationContext();
            applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
        }
    }

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        TestData dataAnnotation = AnnotationUtils.findAnnotation(testContext.getTestClass(), TestData.class);
        if (dataAnnotation != null) {
            String[] jsonFiles = dataAnnotation.value();
            for (String jsonFile : jsonFiles) {
                loadJsonData(testContext, jsonFile);
                // insertOne(testContext, jsonFile);
            }
        }
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        mongoTemplate.getCollectionNames().forEach(collectionName -> {
            mongoTemplate.remove(new Query(), collectionName);
        });
    }

    private void loadJsonData(TestContext testContext, String jsonFile) throws IOException {
        Path filePath = FileUtil.getResourcePath(this, jsonFile);
        String jsonData = new String(Files.readAllBytes(filePath));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Object[] documentArray = objectMapper.readValue(jsonData, Object[].class);

        List<Object> documentObjects = Arrays.asList(documentArray);

        List<Document> documents = new ArrayList<>();
        for (Object docuObject : documentObjects) {
            String docuString = objectMapper.writeValueAsString(docuObject);
            Document document = Document.parse(docuString, UUID_CODEC);
            documents.add(document);
        }

        MongoCollection<Document> collection = mongoTemplate.getCollection(FileUtil.getFileName(filePath));

        Document index = new Document("position", "2dsphere");
        collection.createIndex(index);

        collection.insertMany(documents, new InsertManyOptions().ordered(false));
    }

    public void insertOne(TestContext testContext, String jsonFile) {
        Path filePath = FileUtil.getResourcePath(this, jsonFile);

        MongoCollection<Document> collection = mongoTemplate.getCollection(FileUtil.getFileName(filePath));

        Document index = new Document("position", "2dsphere");
        collection.createIndex(index);

        Document document = new Document();
        document.put("_id", new BsonBinary(BsonBinarySubType.UUID_LEGACY, "9LgfL1AomJ0udg==".getBytes()));
        document.put("name", "dating");
        document.put("type", "d");
        document.put("visibility", "public");
        document.put("system", false);
        document.put("desc", "Dating");
        document.put("position", new Document("type", "Point")
                .append("coordinates", Arrays.asList(47.497912, 19.040235)));

        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        Instant instant = Instant.from(formatter.parse("2023-06-12T12:30:00Z"));
        Date date = Date.from(instant);
        document.put("createdDate", date);
        document.put("createdBy",
                UUID.fromString("2e7b0c6c-57e6-4c0a-98c0-516abed677ab"));

        collection.insertOne(document);
    }
}
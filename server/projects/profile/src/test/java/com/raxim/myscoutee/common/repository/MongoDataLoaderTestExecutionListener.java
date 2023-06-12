package com.raxim.myscoutee.common.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertManyOptions;

public class MongoDataLoaderTestExecutionListener extends AbstractTestExecutionListener {
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
            }
        }
    }

    private void loadJsonData(TestContext testContext, String jsonFile) throws IOException {
        Path filePath = getResourcePath(jsonFile);
        String jsonData = new String(Files.readAllBytes(filePath));

        ObjectMapper objectMapper = new ObjectMapper();

        Document[] documents = objectMapper.readValue(jsonData, Document[].class);

        MongoCollection<Document> collection = mongoTemplate.getCollection(getFileName(filePath));
        
        Document index = new Document("position", "2dsphere");
        collection.createIndex(index);

        collection.insertMany(Arrays.asList(documents), new InsertManyOptions().ordered(false));
    }
    /*
     * 
     * 
     * Document index = new Document("position", "2dsphere");
     * collection.createIndex(index);
     * 
     * Document document = new Document();
     * document.put("_id", UUID.fromString("2e7b0c6c-57e6-4c0a-98c0-516abed677ab"));
     * document.put("name", "dating");
     * document.put("type", "d");
     * document.put("visibility", "public");
     * document.put("system", false);
     * document.put("desc", "Dating");
     * document.put("position", new Document("type", "Point")
     * .append("coordinates", Arrays.asList(47.497912, 19.040235)));
     * 
     * DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
     * Instant instant = Instant.from(formatter.parse("2023-06-12T12:30:00Z"));
     * Date date = Date.from(instant);
     * document.put("createdDate", date);
     * document.put("createdBy",
     * UUID.fromString("2e7b0c6c-57e6-4c0a-98c0-516abed677ab"));
     * 
     * collection.insertOne(document);
     */

    private Path getResourcePath(String jsonFile) {
        String resourcePath = getClass().getClassLoader().getResource(jsonFile).getPath();
        return Paths.get(resourcePath);
    }

    private String getFileName(Path path) {
        String filenameWithExtension = path.getFileName().toString();
        int dotIndex = filenameWithExtension.lastIndexOf(".");
        if (dotIndex != -1) {
            return filenameWithExtension.substring(0, dotIndex);
        } else {
            return filenameWithExtension;
        }
    }
}
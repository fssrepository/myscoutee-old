package com.raxim.myscoutee.common.util;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import org.bson.Document;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.profile.util.AppConstants;

public class JsonUtil {
    public static final String ISO_DATE_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    public static <T> T jsonToObject(String jsonString, Class<T> targetClass, ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(jsonString, targetClass);
        } catch (IOException e) {
            // Handle the exception appropriately
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T clone(T t, ObjectMapper objectMapper) {
        try {
            String jsonString = objectMapper.writeValueAsString(t);
            return objectMapper.readValue(jsonString, new TypeReference<T>() {
            });
        } catch (IOException e) {
            // Handle the exception appropriately
            e.printStackTrace();
            return null;
        }
    }

    public static String toMongoJson(Object t, ObjectMapper objectMapper) {
        try {
            String jsonString = objectMapper.writeValueAsString(t);
            return Document.parse(jsonString, AppConstants.UUID_CODEC).toJson();
        } catch (IOException e) {
            // Handle the exception appropriately
            e.printStackTrace();
            return null;
        }
    }

    public static <T> String toMongoJsonArray(Collection<T> t, ObjectMapper objectMapper) {
        String tJson = t.stream()
                .map(eventItem -> JsonUtil.toMongoJson(eventItem, objectMapper))
                .collect(Collectors.joining(","));
        if (tJson.charAt(tJson.length() - 1) == ',') {
            tJson = tJson.substring(0, tJson.length() - 1);
        }
        return "[" + tJson + "]";
    }

}

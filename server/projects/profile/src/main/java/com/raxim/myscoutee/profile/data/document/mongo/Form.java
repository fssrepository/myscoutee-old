package com.raxim.myscoutee.profile.data.document.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Document(collection = "forms")
public class Form {
    @Id
    @JsonProperty(value = "id")
    private UUID id = UUID.randomUUID();

    @JsonProperty(value = "key")
    private String key;

    @JsonProperty(value = "items")
    private List<FormItem> items;

    // Getter and Setter methods for each field

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<FormItem> getItems() {
        return items;
    }

    public void setItems(List<FormItem> items) {
        this.items = items;
    }
}
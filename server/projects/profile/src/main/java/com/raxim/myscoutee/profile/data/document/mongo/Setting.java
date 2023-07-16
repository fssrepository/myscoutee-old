package com.raxim.myscoutee.profile.data.document.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Document(collection = "settings")
public class Setting implements Cloneable {
    @Id
    @JsonProperty(value = "id")
    private UUID id;

    @JsonProperty(value = "key")
    private String key;

    @JsonIgnore
    private UUID profile;

    @JsonProperty(value = "items")
    private List<FormItem> items;

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

    public UUID getProfile() {
        return profile;
    }

    public void setProfile(UUID profile) {
        this.profile = profile;
    }

    public List<FormItem> getItems() {
        return items;
    }

    public void setItems(List<FormItem> items) {
        this.items = items;
    }

    @Override
    public Object clone()
            throws CloneNotSupportedException {
        return super.clone();
    }
}

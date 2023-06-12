package com.raxim.myscoutee.profile.data.document.mongo;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "scheduleSettings")
public class ScheduleSetting {
    @Id
    @JsonProperty(value = "id")
    private UUID id;

    @JsonProperty(value = "key")
    private String key;

    @JsonProperty(value = "flags")
    private String flags;

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

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }
}

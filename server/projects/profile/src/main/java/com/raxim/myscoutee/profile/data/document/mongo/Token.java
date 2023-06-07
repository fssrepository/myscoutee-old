package com.raxim.myscoutee.profile.data.document.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@QueryEntity
@Document(collection = "tokens")
public class Token {
    @Id
    @JsonIgnore
    private UUID id;

    @JsonIgnore
    @JsonProperty(value = "deviceKey")
    private String deviceKey;

    @JsonIgnore
    private UUID uuid;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }

    public UUID getUuid() {
        return uuid;
    }
}

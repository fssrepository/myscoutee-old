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
    private String deviceKey;

    @JsonIgnore
    private UUID uuid;

    public Token() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @JsonProperty(value = "deviceKey")
    public String getDeviceKey() {
        return deviceKey;
    }

    @JsonProperty(value = "deviceKey")
    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}

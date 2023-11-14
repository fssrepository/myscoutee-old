package com.raxim.myscoutee.profile.data.dto.rest;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.raxim.myscoutee.common.repository.UUIDBase64Deserializer;
import com.raxim.myscoutee.common.repository.UUIDBase64Serializer;

@JsonRootName("message")
public class MessageDTO {
    @JsonSerialize(using = UUIDBase64Serializer.class)
    @JsonDeserialize(using = UUIDBase64Deserializer.class)
    @JsonProperty(value = "key")
    private UUID id;

    @JsonSerialize(using = UUIDBase64Serializer.class)
    @JsonDeserialize(using = UUIDBase64Deserializer.class)
    @JsonProperty(value = "from")
    private UUID from;

    // control, mqtt
    @JsonProperty(value = "type")
    private String type;

    @JsonProperty(value = "value")
    private String value;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getFrom() {
        return from;
    }

    public void setFrom(UUID from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

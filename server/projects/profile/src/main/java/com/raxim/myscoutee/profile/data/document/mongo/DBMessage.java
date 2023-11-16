package com.raxim.myscoutee.profile.data.document.mongo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "messages")
public class DBMessage {
    @Id
    @JsonProperty(value = "key")
    private UUID id;

    @JsonProperty(value = "from")
    private UUID from;

    @JsonIgnore
    private List<UUID> tos;

    @JsonIgnore
    private UUID eventUuid;

    // control, mqtt=if profile.mqtt is true, and the topic is the current page url
    // of the profile then does not send as mosquitto handles
    @JsonProperty(value = "type")
    private String type;

    // received, sent (last received date by user in the query for which
    // message should put the icon on the ui)
    @JsonProperty(value = "value")
    private String value;

    @JsonProperty(value = "createdDate")
    private LocalDateTime createdDate;

    @JsonProperty(value = "ref")
    private UUID ref;

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

    public List<UUID> getTos() {
        return tos;
    }

    public void setTos(List<UUID> tos) {
        this.tos = tos;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public UUID getEventUuid() {
        return eventUuid;
    }

    public void setEventUuid(UUID eventUuid) {
        this.eventUuid = eventUuid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public UUID getRef() {
        return ref;
    }

    public void setRef(UUID ref) {
        this.ref = ref;
    }
}

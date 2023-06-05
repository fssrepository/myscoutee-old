package com.raxim.myscoutee.profile.data.document.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@QueryEntity
@Document(collection = "badges")
public class Badge {
    @Id
    @JsonIgnore
    private UUID id;

    @JsonProperty(value = "root")
    private String root;

    @JsonProperty(value = "value")
    private String value;

    @JsonIgnore
    private UUID profileID;

    @JsonIgnore
    private LocalDateTime dateUpdated;

    @JsonIgnore
    private LocalDateTime dateSeen;

    public Badge() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public UUID getProfileID() {
        return profileID;
    }

    public void setProfileID(UUID profileID) {
        this.profileID = profileID;
    }

    public LocalDateTime getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(LocalDateTime dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public LocalDateTime getDateSeen() {
        return dateSeen;
    }

    public void setDateSeen(LocalDateTime dateSeen) {
        this.dateSeen = dateSeen;
    }
}
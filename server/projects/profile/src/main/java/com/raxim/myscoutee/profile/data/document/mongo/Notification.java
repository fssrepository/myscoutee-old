package com.raxim.myscoutee.profile.data.document.mongo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Document(collection = "notifications")
public class Notification {
    @Id
    @JsonIgnore
    private UUID id;

    private UUID from;

    private List<UUID> toUuids;

    @JsonIgnore
    private LocalDateTime createdDate;

    public Notification() {
    }

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

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public List<UUID> getToUuids() {
        return toUuids;
    }

    public void setToUuids(List<UUID> toUuids) {
        this.toUuids = toUuids;
    }
}

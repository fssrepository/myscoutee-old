package com.raxim.myscoutee.profile.data.document.mongo;

import java.util.Date;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "schedules")
public class Schedule {
    @Id
    @JsonProperty(value = "id")
    private UUID id;

    @JsonProperty(value = "key")
    private String key;

    @JsonIgnore
    private Date createdDate;

    @JsonProperty(value = "lastRunDate")
    private Date lastRunDate;

    @JsonProperty(value = "lastIdx")
    private long lastIdx;

    @JsonProperty(value = "batchSize")
    private long batchSize;

    @JsonProperty(value = "flags")
    private String flags;

    public Schedule(long lastIdx, long batchSize, String flags) {
        this.lastIdx = lastIdx;
        this.batchSize = batchSize;
        this.flags = flags;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public Schedule() {
    }

    public long getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(long batchSize) {
        this.batchSize = batchSize;
    }

    public long getLastIdx() {
        return lastIdx;
    }

    public void setLastIdx(long lastIdx) {
        this.lastIdx = lastIdx;
    }

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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastRunDate() {
        return lastRunDate;
    }

    public void setLastRunDate(Date lastRunDate) {
        this.lastRunDate = lastRunDate;
    }
}

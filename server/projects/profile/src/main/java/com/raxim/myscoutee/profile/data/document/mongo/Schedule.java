package com.raxim.myscoutee.profile.data.document.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.UUID;

@QueryEntity
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
    
    public Schedule() {
        this.id = UUID.randomUUID();
        this.createdDate = new Date();
        this.lastRunDate = new Date();
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
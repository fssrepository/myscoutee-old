package com.raxim.myscoutee.profile.data.document.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Document(collection = "links")
public class Link {

    @Id
    @JsonIgnore
    private UUID id;

    @JsonProperty(value = "key")
    private UUID key;

    @JsonIgnore
    private UUID refId;

    @JsonIgnore
    private String type;

    @JsonIgnore
    private Set<String> usedBys;

    @JsonIgnore
    private Date createdDate;

    @JsonIgnore
    private UUID createdBy;

    public Link() {
        this.usedBys = new HashSet<>();
        this.createdDate = new Date();
    }

    // Getter and Setter methods for each field

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public UUID getRefId() {
        return refId;
    }

    public void setRefId(UUID refId) {
        this.refId = refId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<String> getUsedBys() {
        return usedBys;
    }

    public void setUsedBys(Set<String> usedBys) {
        this.usedBys = usedBys;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }
}

package com.raxim.myscoutee.profile.data.document.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;
import java.util.UUID;

/*
 * 1) in queries there are more rows, and it will be the average, taking care of the "double" flag also
 *    20*(20-abs(rate1-rate2)) + profile difference
 */

@Document(collection = "likes")
public class Like {

    @Id
    @JsonProperty(value = "id")
    private UUID id;

    @JsonProperty(value = "double")
    private Boolean isDouble;

    @DBRef
    @JsonProperty(value = "from")
    private Profile from;

    @DBRef
    @JsonProperty(value = "to")
    private Profile to;

    @JsonProperty(value = "rate")
    private Integer rate;

    @DBRef
    @JsonProperty(value = "createdBy")
    private Profile createdBy;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonProperty(value = "createdDate")
    private Date createdDate;

    @JsonProperty(value = "distance")
    private Long distance;

    @JsonIgnore
    private UUID ref;

    // Getter and Setter methods for each field

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Boolean isDouble() {
        return isDouble;
    }

    public void setDouble(Boolean isDouble) {
        this.isDouble = isDouble;
    }

    public Profile getFrom() {
        return from;
    }

    public void setFrom(Profile from) {
        this.from = from;
    }

    public Profile getTo() {
        return to;
    }

    public void setTo(Profile to) {
        this.to = to;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public Profile getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Profile createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Long getDistance() {
        return distance;
    }

    public void setDistance(Long distance) {
        this.distance = distance;
    }

    public UUID getRef() {
        return ref;
    }

    public void setRef(UUID ref) {
        this.ref = ref;
    }
}

package com.raxim.myscoutee.profile.data.document.mongo;

import java.util.Date;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 1) panel coloring depends on rate.
 */
@Document(collection = "feedbacks")
public class Feedback {

    @Id
    @JsonProperty(value = "key")
    private UUID id = UUID.randomUUID();

    @JsonProperty(value = "rate")
    private Integer rate;

    @JsonProperty(value = "desc")
    private String desc;

    @JsonIgnore
    private Date createdDate = new Date();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}

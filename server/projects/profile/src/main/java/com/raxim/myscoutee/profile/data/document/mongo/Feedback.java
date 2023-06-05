package com.raxim.myscoutee.profile.data.document.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.UUID;

/*
    1)  panel background coloring depends on rate
*/

@QueryEntity
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

    // Getter and Setter methods for each field

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
package com.raxim.myscoutee.profile.data.document.mongo;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Neo4jLike {
    @DBRef
    private Profile from;
    
    @DBRef
    private Profile to;
    
    private Long distance;
    private Double rate;
    
    public Neo4jLike() {
    }

    public Neo4jLike(Profile from, Profile to, Long distance, Double rate) {
        this.from = from;
        this.to = to;
        this.distance = distance;
        this.rate = rate;
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

    public Long getDistance() {
        return distance;
    }

    public void setDistance(Long distance) {
        this.distance = distance;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }
}

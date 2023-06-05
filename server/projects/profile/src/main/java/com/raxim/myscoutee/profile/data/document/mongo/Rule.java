package com.raxim.myscoutee.profile.data.document.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryEntity;
import org.springframework.data.mongodb.core.mapping.Document;

@QueryEntity
@Document(collection = "rules")
public class Rule {
    @JsonProperty(value = "met")
    private Boolean met;
    
    @JsonProperty(value = "rate")
    private Integer rate;
    
    private RangeLocal range;
    
    @JsonProperty(value = "balanced")
    private String balanced;
    
    public Rule() {
        this.range = new RangeLocal();
    }
    
    public Boolean getMet() {
        return met;
    }
    
    public void setMet(Boolean met) {
        this.met = met;
    }
    
    public Integer getRate() {
        return rate;
    }
    
    public void setRate(Integer rate) {
        this.rate = rate;
    }
    
    public RangeLocal getRange() {
        return range;
    }
    
    public void setRange(RangeLocal range) {
        this.range = range;
    }
    
    public String getBalanced() {
        return balanced;
    }
    
    public void setBalanced(String balanced) {
        this.balanced = balanced;
    }
}

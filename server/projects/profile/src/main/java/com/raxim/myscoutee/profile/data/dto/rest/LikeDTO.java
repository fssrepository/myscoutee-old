package com.raxim.myscoutee.profile.data.dto.rest;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("like")
public class LikeDTO {
    @JsonProperty(value = "from")
    private UUID from;

    @JsonProperty(value = "to")
    private UUID to;

    @JsonProperty(value = "rate")
    private Double rate;

    @JsonProperty(value = "ref")
    private UUID ref;

    @JsonProperty(value = "type")
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UUID getFrom() {
        return from;
    }

    public void setFrom(UUID from) {
        this.from = from;
    }

    public UUID getTo() {
        return to;
    }

    public void setTo(UUID to) {
        this.to = to;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public UUID getRef() {
        return ref;
    }

    public void setRef(UUID ref) {
        this.ref = ref;
    }
}

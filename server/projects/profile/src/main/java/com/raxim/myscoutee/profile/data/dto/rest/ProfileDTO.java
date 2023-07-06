package com.raxim.myscoutee.profile.data.dto.rest;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;

@JsonRootName("profile")
public class ProfileDTO extends PageItemDTO {
    @JsonProperty(value = "profile")
    private Profile profile;

    @JsonProperty(value = "groupKey")
    private Object groupKey;

    @JsonProperty(value = "rate")
    private Double rate;

    @JsonProperty(value = "ref")
    private UUID ref;

    @JsonProperty(value = "role")
    private String role;

    @JsonProperty(value = "met")
    private Boolean met;

    public ProfileDTO() {
    }

    public ProfileDTO(Profile profile) {
        this.profile = profile;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Object getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(Object groupKey) {
        this.groupKey = groupKey;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getMet() {
        return met;
    }

    public void setMet(Boolean met) {
        this.met = met;
    }

    @Override
    public String toString() {
        return "ProfileDTO [profile=" + profile + ", groupKey=" + groupKey + ", rate=" + rate + ", ref=" + ref
                + ", offset=" + getOffset() + ", role=" + role + "]";
    }
}

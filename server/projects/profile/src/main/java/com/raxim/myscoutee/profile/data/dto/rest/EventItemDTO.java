package com.raxim.myscoutee.profile.data.dto.rest;

import java.util.List;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.raxim.myscoutee.common.repository.GeoJsonPointDeserializer;
import com.raxim.myscoutee.profile.data.document.mongo.EventItem;

@JsonRootName("eventItem")
public class EventItemDTO {

    @JsonProperty(value = "item")
    private com.raxim.myscoutee.profile.data.document.mongo.EventItem item;

    @JsonProperty(value = "groupKey")
    private Object groupKey;

    @JsonProperty(value = "sortKey")
    private Object sortKey;

    @JsonIgnore
    private List<Object> offset;

    @JsonProperty(value = "main")
    private Boolean main;

    @JsonProperty(value = "category")
    private String category;

    @JsonDeserialize(using = GeoJsonPointDeserializer.class)
    @JsonProperty(value = "positions")
    private List<GeoJsonPoint> positions;

    @JsonProperty(value = "isPromotion")
    private Boolean isPromotion;

    @JsonProperty(value = "isMember")
    private Boolean isMember;

    @JsonProperty(value = "role")
    private String role;

    public EventItemDTO(EventItem item, Object groupKey) {
        this.item = item;
        this.groupKey = groupKey;
    }

    public EventItemDTO() {
    }

    public com.raxim.myscoutee.profile.data.document.mongo.EventItem getItem() {
        return item;
    }

    public void setItem(com.raxim.myscoutee.profile.data.document.mongo.EventItem item) {
        this.item = item;
    }

    public Object getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(Object groupKey) {
        this.groupKey = groupKey;
    }

    public Object getSortKey() {
        return sortKey;
    }

    public void setSortKey(Object sortKey) {
        this.sortKey = sortKey;
    }

    public List<Object> getOffset() {
        return offset;
    }

    public void setOffset(List<Object> offset) {
        this.offset = offset;
    }

    public Boolean getMain() {
        return main;
    }

    public void setMain(Boolean main) {
        this.main = main;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<GeoJsonPoint> getPositions() {
        return positions;
    }

    public void setPositions(List<GeoJsonPoint> positions) {
        this.positions = positions;
    }

    public Boolean getPromotion() {
        return isPromotion;
    }

    public void setPromotion(Boolean promotion) {
        isPromotion = promotion;
    }

    public Boolean getMember() {
        return isMember;
    }

    public void setMember(Boolean member) {
        isMember = member;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

package com.raxim.myscoutee.profile.data.dto.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.raxim.myscoutee.profile.converter.Convertable;
import com.raxim.myscoutee.profile.data.document.mongo.Event;

@JsonRootName("event")
public class EventDTO extends PageItemDTO implements Convertable<EventDTO> {

    @JsonProperty(value = "item")
    private Event item;

    @JsonProperty(value = "groupKey")
    private Object groupKey;

    @JsonProperty(value = "sortKey")
    private Object sortKey;

    @JsonProperty(value = "rate")
    private Integer rate;

    @JsonProperty(value = "role")
    private String role;

    public EventDTO(Event event) {
        this.item = event;
    }

    public EventDTO() {
    }

    public EventDTO(Event event, Object groupKey) {
        this(event, groupKey, null);
    }

    public EventDTO(Event event, Object groupKey, Object sortKey) {
        this.item = event;
        this.groupKey = groupKey;
        this.sortKey = sortKey;
    }

    public Event getItem() {
        return item;
    }

    public void setItem(Event event) {
        this.item = event;
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

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

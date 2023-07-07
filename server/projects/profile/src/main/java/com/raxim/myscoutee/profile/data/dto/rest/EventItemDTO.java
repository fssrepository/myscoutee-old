package com.raxim.myscoutee.profile.data.dto.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.raxim.myscoutee.profile.converter.Convertable;
import com.raxim.myscoutee.profile.data.document.mongo.EventItem;

@JsonRootName("eventItem")
public class EventItemDTO extends PageItemDTO implements Convertable {

    @JsonProperty(value = "item")
    private EventItem item;

    @JsonProperty(value = "groupKey")
    private Object groupKey;

    @JsonProperty(value = "sortKey")
    private Object sortKey;

    @JsonProperty(value = "role")
    private String role;

    public EventItemDTO() {
    }

    public EventItemDTO(EventItem item) {
        this(item, null);
    }

    public EventItemDTO(EventItem item, Object groupKey) {
        this.item = item;
        this.groupKey = groupKey;
    }

    public EventItem getItem() {
        return item;
    }

    public void setItem(EventItem item) {
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

package com.raxim.myscoutee.profile.data.document.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@QueryEntity
@Document(collection = "promotions")
public class Promotion {
    @Id
    @JsonProperty(value = "key")
    private UUID id;

    @JsonProperty(value = "name")
    private String name;

    // job, idea, event
    @JsonProperty(value = "type")
    private String type;

    // advertise date from, to
    @JsonProperty(value = "range")
    private RangeLocal range;

    // select template
    //@DBRef
    @JsonProperty(value = "item")
    private Event item;

    @JsonIgnore
    private int cnt;

    @DBRef
    @JsonIgnore
    private List<Event> events;

    // accordion item by slot
    @JsonProperty(value = "slots")
    private Set<Slot> slots;

    @JsonProperty(value = "groupType")
    private String groupType;

    @DBRef
    @JsonProperty(value = "group")
    private Group group;

    @JsonIgnore
    private Date createdDate;

    @JsonProperty(value = "createdBy")
    private UUID createdBy;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RangeLocal getRange() {
        return range;
    }

    public void setRange(RangeLocal range) {
        this.range = range;
    }

    public Event getItem() {
        return item;
    }

    public void setItem(Event item) {
        this.item = item;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public Set<Slot> getSlots() {
        return slots;
    }

    public void setSlots(Set<Slot> slots) {
        this.slots = slots;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }
}

package com.raxim.myscoutee.profile.data.document.mongo;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.raxim.myscoutee.common.repository.GeoJsonPointDeserializer;

@Document(collection = "events")
public class Event {
    @Id
    @JsonProperty(value = "key")
    private UUID id;

    // the first event in the list
    //it contains the url of signal also
    //if first event is not added yet, it shows the form in the popup instead of the list
    //open dialog in list feature, if there is no element
    //routing hasFirst feature - if isFirst - than the popup *ngIf shows other elements - like signal url field

    //if you change dateFrom of general than every other item's date will slip
    @JsonIgnore
    private EventItem info;

    @DBRef
    @JsonIgnore
    private List<EventItem> items;

    @DBRef
    @JsonIgnore
    private List<Feedback> feedbacks;

    @JsonIgnore
    private int cnt;

    // should be filtered by group
    @JsonIgnore
    private UUID group;

    @JsonIgnore
    private GeoJsonPoint position;

    // cloned from
    @DBRef
    @JsonIgnore
    private Event ref;

    // Accepted (A) (by Organizer), Published/Promotion/Pending (P),
    // Inactive (I), Template (T), Under Review (U), Reviewed/Recommended (R), Rejected/Deleted (D)
    // Cancelled (C)
    // auto publish when general has been added
    // inactive means, just edited locally, before being published
    // when accepted by organizer, create chat room
    // accept form needs to have chat url
    @JsonProperty(value = "status")
    private String status;

    //idea, job, event for template
    @JsonIgnore
    private String type;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonProperty(value = "createdDate")
    private Date createdDate;

    @JsonProperty(value = "createdBy")
    private UUID createdBy;

    @JsonDeserialize(using = GeoJsonPointDeserializer.class)
    @JsonIgnore
    @JsonProperty(value = "positions")
    private List<GeoJsonPoint> positions;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public EventItem getInfo() {
        return info;
    }

    public void setInfo(EventItem info) {
        this.info = info;
    }

    public List<EventItem> getItems() {
        return items;
    }

    public void setItems(List<EventItem> items) {
        this.items = items;
    }

    public List<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public UUID getGroup() {
        return group;
    }

    public void setGroup(UUID group) {
        this.group = group;
    }

    public GeoJsonPoint getPosition() {
        return position;
    }

    public void setPosition(GeoJsonPoint position) {
        this.position = position;
    }

    public Event getRef() {
        return ref;
    }

    public void setRef(Event ref) {
        this.ref = ref;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public List<GeoJsonPoint> getPositions() {
        return positions;
    }

    public void setPositions(List<GeoJsonPoint> positions) {
        this.positions = positions;
    }    
    
}

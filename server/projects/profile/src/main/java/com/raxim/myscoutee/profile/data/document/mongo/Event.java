package com.raxim.myscoutee.profile.data.document.mongo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "events")
public class Event extends EventBase {
    @Id
    @JsonProperty(value = "key")
    private UUID id;

    // should be filtered by group
    @JsonIgnore
    private UUID group;

    @JsonProperty(value = "rule")
    private Rule rule;

    // qr code generation
    @JsonProperty(value = "ticket")
    private Boolean ticket;

    // type = person, idea, job, event for template

    /* local, global */
    /*
     * google map is complicated to get the coordinates to search for,
     * but to differentiate local and global based on that is ridiculous
     * the category will be reused to identify the real category (sport, reading
     * etc.)
     */
    @JsonProperty(value = "category")
    private String category;

    // status
    // Accepted (A) (by Organizer), Published/Promotion/Pending (P),
    // Inactive (I), Template (T), Under Review (U), Reviewed/Recommended (R),
    // Rejected/Deleted (D)
    // Cancelled (C)
    // auto publish when general has been added
    // inactive means, just edited locally, before being published
    // when accepted by organizer, create chat room

    // ref counter
    @JsonIgnore
    private int cnt;

    // cloned from
    @DBRef
    @JsonIgnore
    private Event ref;

    // ??, lehet, hogy az eventItem-nek kell az event.id tartalmaznia es ez ide nem
    // kell
    // lekerdezesek vizsgalata kell-e ez az optimalizacio
    @DBRef
    @JsonIgnore
    private List<EventItem> items;

    // a Feedback-nel van event.id, es nem kell ide, lekerdezeseket checkkolni,
    // kell-e ez az optimalizacio
    @DBRef
    @JsonIgnore
    private List<Feedback> feedbacks;

    @JsonIgnore
    private GeoJsonPoint position;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public Boolean getTicket() {
        return ticket;
    }

    public void setTicket(Boolean ticket) {
        this.ticket = ticket;
    }
}

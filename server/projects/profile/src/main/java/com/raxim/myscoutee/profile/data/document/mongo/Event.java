package com.raxim.myscoutee.profile.data.document.mongo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    // type = person, idea, job, template

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
    private Set<EventItem> items;

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

    public Set<EventItem> getItems() {
        return items;
    }

    public void setItems(Set<EventItem> items) {
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

    // eventItem range should be within event
    public void sync() {
        Optional<Integer> optMaxCapacity = this.items.stream().filter(item -> item.getCapacity() != null)
                .map(item -> item.getCapacity().getMax())
                .max((cap1, cap2) -> cap1.compareTo(cap2));
        if (optMaxCapacity.isPresent()) {
            Integer maxCapacity = optMaxCapacity.get();
            if (maxCapacity.intValue() > this.getCapacity().getMax()) {
                this.getCapacity().setMax(maxCapacity.intValue());
            }
        }

        Optional<LocalDateTime> optStart = this.items.stream().filter(item -> item.getRange() != null)
                .map(item -> item.getRange().getStart())
                .min((cap1, cap2) -> cap1.isBefore(cap2) ? -1 : 1);

        if (optStart.isPresent()) {
            LocalDateTime start = optStart.get();
            if (start.isBefore(this.getRange().getStart())) {
                this.getRange().setStart(start);
            }
        }

        Optional<LocalDateTime> optEnd = this.items.stream().filter(item -> item.getRange() != null)
                .map(item -> item.getRange().getEnd())
                .max((cap1, cap2) -> cap1.isAfter(cap2) ? -1 : 1);

        if (optEnd.isPresent()) {
            LocalDateTime end = optEnd.get();
            if (end.isAfter(this.getRange().getEnd())) {
                this.getRange().setEnd(end);
            }
        }
    }

    public void shift() {
        Optional<LocalDateTime> optStart = this.items.stream().filter(item -> item.getRange() != null)
                .map(item -> item.getRange().getStart())
                .min((cap1, cap2) -> cap1.isBefore(cap2) ? -1 : 1);

        if (optStart.isPresent() && this.getRange() != null) {
            LocalDateTime start = optStart.get();
            Duration duration = Duration.between(start, this.getRange().getStart());
            long diffInMillis = duration.get(ChronoUnit.SECONDS);
            if (diffInMillis > 0) {
                this.items = this.items.stream()
                        .map(item -> {
                            LocalDateTime lEventItemStart = item.getRange().getStart().plusSeconds(diffInMillis);
                            LocalDateTime lEventItemEnd = item.getRange().getEnd().plusSeconds(diffInMillis);
                            item.setRange(RangeLocal.of(lEventItemStart, lEventItemEnd));
                            return item;
                        })
                        .collect(Collectors.toSet());

                Optional<LocalDateTime> optEnd = this.items.stream().filter(item -> item.getRange() != null)
                        .map(item -> item.getRange().getEnd())
                        .max((cap1, cap2) -> cap1.isAfter(cap2) ? -1 : 1);

                if (optEnd.isPresent()) {
                    LocalDateTime end = optEnd.get();
                    if (end.isBefore(this.getRange().getEnd())) {
                        this.getRange().setEnd(end);
                    }
                }
            }
        }
    }

    @Override
    public Object clone()
            throws CloneNotSupportedException {
        return super.clone();
    }
}

package com.raxim.myscoutee.profile.data.document.mongo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.raxim.myscoutee.profile.converter.Convertable;

@Document(collection = "events")
public class Event extends EventBase implements Convertable {
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

    // eventItem range should be within event
    public void sync() {
        if (getItems() != null) {
            // max capacity can be changed only from the event
            List<EventItem> items = getItems().stream().map(item -> {
                if (getCapacity() != null && item.getCapacity() != null) {

                    if (item.getCapacity().getMax() > getCapacity().getMax()) {
                        item.getCapacity().setMax(getCapacity().getMax());
                    }

                    if (item.getCapacity().getMin() > getCapacity().getMax()) {
                        item.getCapacity().setMin(getCapacity().getMax());
                    }
                }
                return item;
            }).toList();
            setItems(items);

            Optional<LocalDateTime> optStart = getItems().stream().filter(item -> item.getRange() != null)
                    .map(item -> item.getRange().getStart())
                    .min((cap1, cap2) -> cap1.isBefore(cap2) ? -1 : 1);

            if (optStart.isPresent()) {
                LocalDateTime start = optStart.get();
                if (getRange() != null
                        && start.isBefore(getRange().getStart())) {
                    getRange().setStart(start);
                }
            }

            Optional<LocalDateTime> optEnd = getItems().stream().filter(item -> item.getRange() != null)
                    .map(item -> item.getRange().getEnd())
                    .max((cap1, cap2) -> cap1.isAfter(cap2) ? -1 : 1);

            if (optEnd.isPresent()) {
                LocalDateTime end = optEnd.get();
                if (getRange() != null
                        && end.isAfter(getRange().getEnd())) {
                    getRange().setEnd(end);
                }
            }
        }

        LocalDateTime graceTime = getRule() != null ? getRange().getStart()
                .minus(getRule().getEventGrace(), ChronoUnit.MINUTES)
                : getRange().getStart();

        if (getMembers() != null) {
            int cnt = (int) getMembers().stream().filter(member -> "A".equals(member.getStatus())).count();
            setNum(cnt);

            getMembers().stream().map(member -> {
                if ("L".equals(member.getStatus())
                        && member.getUpdatedDate().isAfter(graceTime)) {
                    member.setStatus("LL");
                }
                return member;
            });

            // admin or promoter
            if (getRef() != null) {
                int promoterCnt = (int) getMembers().stream()
                        .filter(member -> "P".equals(member.getRole()) && "A".equals(member.getStatus())).count();

                if (promoterCnt == 0) {
                    setStatus("C");
                }
            } else {
                int adminCnt = (int) getMembers().stream()
                        .filter(member -> "A".equals(member.getRole()) && "A".equals(member.getStatus())).count();

                if (adminCnt == 0) {
                    Optional<Member> optMemberMin = getMembers().stream().filter(member -> "U".equals(member.getRole()))
                            .min((cap1, cap2) -> cap1.getCreatedDate().isBefore(cap2.getCreatedDate()) ? -1 : 1);

                    if (optMemberMin.isPresent()) {
                        Member memberMin = optMemberMin.get();
                        memberMin.setRole("A");
                        getMembers().add(memberMin);
                    }
                }
            }

            if (getCapacity() != null) {

                // if invitation has been accepted, but the screen was not uptodate -> accept
                // should have been wait
                int diffWithJ = cnt - getCapacity().getMax();

                if (diffWithJ > 0) {
                    getMembers().stream()
                            .filter(member -> "A".equals(member.getStatus()))
                            .sorted((m1, m2) -> m2.getUpdatedDate().compareTo(m1.getUpdatedDate()))
                            .limit(diffWithJ).map(member -> {
                                member.setStatus("W");
                                return member;
                            })
                            .collect(Collectors.toList());
                }

                if (getNum() >= getCapacity().getMin()) {
                    if (LocalDateTime.now().isAfter(graceTime) && getNum() >= getCapacity().getMin()) {
                        setStatus("A");
                    } else {
                        setStatus("C");
                    }
                } else {
                    setStatus("P");
                }
            }
        }

        if (getNum() == 0) {
            setStatus("C");
        }
    }

    public void shift() {
        if (getItems() != null) {
            Optional<LocalDateTime> optStart = getItems().stream().filter(item -> item.getRange() != null)
                    .map(item -> item.getRange().getStart())
                    .min((cap1, cap2) -> cap1.isBefore(cap2) ? -1 : 1);

            if (optStart.isPresent() && getRange() != null) {
                LocalDateTime start = optStart.get();
                Duration duration = Duration.between(start, getRange().getStart());
                long diffInMillis = duration.get(ChronoUnit.SECONDS);
                if (diffInMillis > 0) {
                    List<EventItem> lItems = getItems().stream()
                            .map(item -> {
                                LocalDateTime lEventItemStart = item.getRange().getStart().plusSeconds(diffInMillis);
                                LocalDateTime lEventItemEnd = item.getRange().getEnd().plusSeconds(diffInMillis);
                                item.setRange(RangeLocal.of(lEventItemStart, lEventItemEnd));
                                return item;
                            })
                            .toList();
                    setItems(lItems);

                    Optional<LocalDateTime> optEnd = getItems().stream().filter(item -> item.getRange() != null)
                            .map(item -> item.getRange().getEnd())
                            .max((cap1, cap2) -> cap1.isAfter(cap2) ? -1 : 1);

                    if (optEnd.isPresent()) {
                        LocalDateTime end = optEnd.get();
                        if (getRange() != null
                                && end.isBefore(getRange().getEnd())) {
                            getRange().setEnd(end);
                        }
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

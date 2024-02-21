package com.raxim.myscoutee.profile.data.document.mongo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.raxim.myscoutee.common.repository.GeoJsonPointDeserializer;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.converter.Convertable;
import com.raxim.myscoutee.profile.data.document.mongo.iface.EventBase;
import com.raxim.myscoutee.profile.data.document.mongo.iface.Tree;

@Document(collection = "events")
public class Event extends EventBase implements Convertable<Event>, Tree<Event> {
    @Id
    @JsonProperty(value = "key")
    private UUID id;

    @JsonIgnore
    private UUID parentId;

    // urlRef -> //car, accomodation etc.

    // urlRef is the template

    // status
    // Accepted (A) (by Organizer), Published/Promotion/Pending (P) ?? -> not sure
    // published/promotion is needed, it's only pending,
    // Inactive (I), Template (T), Under Review (U) ??, Reviewed/Recommended (R),
    // Rejected/Deleted (D), Finished (F)
    // Cancelled (C)
    // auto publish when general has been added
    // inactive means, just edited locally, before being published
    // when accepted by organizer, create chat room

    // lock event (status A) -> no new members can be added or removed (event can be
    // unlocked (status P, based on graceTime, it can be A)
    // also, priority algorithm also filtering it out)
    // it's important for promotional event, when you need to notify the promoter
    // activity/promotion/{id} -> promotional event list can be filtered for events
    // with status A (locked)
    // any event which is locked, any late invitation accept also does not work,
    // will go to "W" (wait list) immadiately

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    // friends only (F) vs all members inside the group (A) vs P (private)
    @JsonProperty(value = "access")
    private String access;

    // if access "A", admin can switch to invite, if access "F", creator can switch
    // to invite, in any other case the value is true
    // if access "A" or "F" this kind of event is going directly to the invitation
    // tab of eny member of
    // the group
    @JsonProperty(value = "autoInvite")
    private Boolean autoInvite;

    @JsonProperty(value = "multislot")
    private Boolean multislot;

    //click on ... button on event item (no slot number on event edit screen), "clone as slot" menu item => enter slot number

    // based on created date make a slotIdx (sync method)
    // leave it 0, if there is only one item in the slot
    @JsonIgnore
    private int slotIdx;

    // in the main event => current Stage idx, in the child event it's and index of the stage => "assign to stage" menu item
    // pre selection from main event -> someone is interested
    // it's going to the parentEvent.members, and later on organized to a stageIdx = 0, slotIdx = 0
    @JsonProperty(value = "stage")
    private int stageIdx = -1;

    // ref counter - how many clones are there, hence we can show on the view
    @JsonIgnore
    private int refCnt;

    // cloned from
    // can be from the recommendation screen or inside a multislot event
    @JsonIgnore
    private UUID ref;

    @JsonProperty(value = "rule")
    private Rule rule = new Rule();

    //people don't see each other
    @JsonProperty(value = "discreet")
    private Boolean discreet;

    // qr code generation
    @JsonProperty(value = "ticket")
    private Boolean ticket;

    // ??, lehet, hogy az eventItem-nek kell az event.id tartalmaznia es ez ide nem
    // kell
    // lekerdezesek vizsgalata kell-e ez az optimalizacio

    @JsonIgnore
    private int numOfEvents;

    @DBRef
    @JsonIgnore
    private List<Event> items = new ArrayList<>();

    // a Feedback-nel van event.id, es nem kell ide, lekerdezeseket checkkolni,
    // kell-e ez az optimalizacio
    @DBRef
    @JsonIgnore
    private List<Feedback> feedbacks = new ArrayList<>();

    // it is used to calculate absolute score of the members
    @DBRef
    @JsonIgnore
    private Set<Match> matches;

    @GeoSpatialIndexed(name = "position", type = GeoSpatialIndexType.GEO_2DSPHERE)
    @JsonDeserialize(using = GeoJsonPointDeserializer.class)
    @JsonProperty(value = "position")
    private GeoJsonPoint position;

    /* local, global */
    /*
     * google map is complicated to get the coordinates to search for,
     * but to differentiate local and global based on that is ridiculous
     * the category will be reused to identify the real category (sport, reading
     * etc.)
     */
    // mabe sub category also
    @JsonProperty(value = "category")
    private String category;

    // should be filtered by group
    @JsonIgnore
    private UUID group;

    // rate average from feedback, maybe harmonic mean
    @JsonProperty(value = "rate")
    public Integer rate;

    public Event() {
    }

    public Event(UUID id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public UUID getGroup() {
        return group;
    }

    public void setGroup(UUID group) {
        this.group = group;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<Event> getItems() {
        return items;
    }

    public void setItems(List<Event> items) {
        this.items = items;
    }

    public List<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public int getRefCnt() {
        return refCnt;
    }

    public void setRefCnt(int cnt) {
        this.refCnt = cnt;
    }

    public GeoJsonPoint getPosition() {
        return position;
    }

    public void setPosition(GeoJsonPoint position) {
        this.position = position;
    }

    public UUID getRef() {
        return ref;
    }

    public void setRef(UUID ref) {
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

    public boolean isPriority() {
        return getRule() != null && "p".equals(getRule().getType());
    }

    public boolean isAutoApprove() {
        return getRule() != null && Boolean.TRUE.equals(getRule().getAutoApprove());
    }

    public boolean isBalanced() {
        return getRule() != null && Boolean.TRUE.equals(getRule().getBalanced());
    }

    public boolean isMultislot() {
        return Boolean.TRUE.equals(getMultislot());
    }

    public int getMaxStage() {
        Optional<Event> event = getItems().stream().max(Comparator.comparing(Event::getStageIdx));

        return event.isPresent() ? event.get().getStageIdx() : 0;
    }

    public boolean isFinished() {
        return getRange() != null && LocalDateTime.now().isAfter(getRange().getEnd());
    }

    public boolean isStageFinished() {
        return getItemsByStage(getStageIdx()).stream().allMatch(item -> item.isFinished());
    }

    public void syncStatus() {
        if (isFinished()) {
            setStatus("F");
            return;
        }

        if (getItems() != null && isMultislot()) {
            int min = getItems().stream().filter(item -> item.getStageIdx() == getStageIdx())
                    .mapToInt(item -> item.getCapacity().getMin())
                    .min().orElse(0);

            int max = getItems().stream().filter(item -> item.getStageIdx() == getStageIdx())
                    .mapToInt(item -> item.getCapacity().getMax())
                    .sum();
            setCapacity(RangeInt.of(min, max));
        }

        if (getMembers() != null) {

            List<Member> activeMembers = getMembers().stream()
                    .filter(member -> "A".equals(member.getStatus())).toList();

            if (activeMembers.size() == 0 && isStarted()) {
                setStatus("T");
                return;
            }

            if ("P".equals(getStatus())) {

                // access is A or F
                if (!"P".equals(getAccess())
                        && isStarted()) {
                    getMembers().stream()
                            .filter(member -> "J".equals(member.getStatus())
                                    && "U".equals(member.getRole()))
                            .forEach(member -> {
                                member.setStatus("A");
                            });
                }

                Double rateMin = getRule() != null ? getRule().getRate() != null ? getRule().getRate() : 0d : 0d;

                List<Member> members = getMembers().stream()
                        .filter(member -> "A".equals(member.getStatus())
                                && "U".equals(member.getRole()) && (!isPriority() || member.getScore() >= rateMin))
                        .sorted()
                        .toList();

                setNumOfMembers(members.size());

                if (isBalanced()) {
                    Map<String, Long> genderCounts = members.stream()
                            .collect(Collectors.groupingBy(member -> member.getProfile().getGender(),
                                    Collectors.counting()));

                    long minCount = genderCounts.values().stream().min(Long::compareTo).orElse(0L);

                    members = members.stream()
                            .filter(member -> genderCounts.get(member.getProfile().getGender()) >= minCount)
                            .collect(Collectors.toList());
                }

                if (isStarted()) {
                    if (getRule() != null) {

                        if (getCapacity().getMin() > getNumOfMembers()) {
                            timeOut();
                        } else {
                            setStatus("A");
                        }

                    }
                } else {
                    if ("P".equals(getAccess())
                            && getCapacity().getMax() == getNumOfMembers()) {
                        setStatus("A");
                    }
                }
            }
        }
    }

    public List<Event> getItemsByStage(int i) {
        return getItems().stream()
                .filter(item -> item.getStageIdx() == i)
                .toList();
    }

    public boolean isStarted() {
        LocalDateTime startDate = getRule() != null
                ? getRange().getStart()
                        .minus(getRule().getEventGrace(), ChronoUnit.MINUTES)
                : getRange().getStart();
        return LocalDateTime.now().isAfter(startDate);
    }

    public void assignToSlots(List<Member> members) {
        try {
            if (Boolean.TRUE.equals(getMultislot())) {
                int memberIdx = 0;
                int counter = getNumOfMembers();
                for (Event item : getItems()) {

                    if (item.getStageIdx() == getStageIdx() && "P".equals(item.getStatus())) {
                        int availableCapacity = item.getCapacity().getMax() - item.getNumOfMembers();
                        if (counter < item.getCapacity().getMin()) {
                            item.setStatus("T");
                        } else {
                            // item status is pending, until there is not enough approval
                            int limit = Math.min(members.size(), memberIdx + availableCapacity);
                            for (int i = memberIdx; i < limit; i++) {

                                Member currMemberForSub = (Member) members.get(i).clone();
                                currMemberForSub.setStatus("I");
                                item.getMembers().remove(currMemberForSub);
                                item.getMembers().add(currMemberForSub);

                                Member currMemberForMain = (Member) members.get(i);
                                currMemberForMain.setStatus("P"); // ???
                                getMembers().remove(currMemberForMain);
                                getMembers().add(currMemberForMain);
                            }
                            memberIdx += availableCapacity;
                        }
                        counter -= availableCapacity;
                    }
                    item.syncStatus();
                }
            }

            boolean allItemsTimedOut = getItems().stream()
                    .allMatch(item -> item.getStageIdx() == getStageIdx() && "T".equals(item.getStatus()));
            if (allItemsTimedOut) {
                setStatus("T");
            }

            boolean anyActiveItem = getItems().stream()
                    .anyMatch(item -> item.getStageIdx() == getStageIdx() && "A".equals(item.getStatus()));
            boolean anyNotFinished = getItems().stream()
                    .anyMatch(item -> item.getStageIdx() == getStageIdx() && "P".equals(item.getStatus()));
            if (!anyNotFinished && anyActiveItem) {
                setStatus("A");
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace(); // add logger
        }
    }

    // eventItem range should be within event
    public void sync() {

        // timed out/active/finished
        if ("T".equals(getStatus()) || "F".equals(getStatus())) {
            return;
        }

        if (getItems() != null && !"A".equals(getStatus())) {

            if (Boolean.TRUE.equals(getMultislot())) {
                List<Event> items = getItems().stream()
                        .collect(Collectors.groupingBy(item -> item.getRange())) // Group items by range
                        .values().stream()
                        .flatMap(group -> CommonUtil.mapIndexed(
                                group.stream().sorted(Comparator.comparing(Event::getCreatedDate)).toList(),
                                (index, item) -> {
                                    item.slotIdx = ++index;
                                    return item;
                                }))
                        .toList();
                setItems(items);
            }

            if (!Boolean.TRUE.equals(getMultislot())) {
                // max capacity can be changed only from the event
                List<Event> items = getItems().stream().map(item -> {
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
            }

            Optional<LocalDateTime> optStart = getItems().stream()
                    .filter(item -> item.getRange() != null && item.getStageIdx() == getStageIdx())
                    .map(item -> item.getRange().getStart())
                    .min((cap1, cap2) -> cap1.isBefore(cap2) ? -1 : 1);

            if (optStart.isPresent()) {
                LocalDateTime start = optStart.get();
                if (getRange() != null
                        && start.isBefore(getRange().getStart())) {
                    getRange().setStart(start);
                }
            }

            Optional<LocalDateTime> optEnd = getItems().stream()
                    .filter(item -> item.getRange() != null && item.getStageIdx() == getStageIdx())
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

        if (getMembers() != null) {

            LocalDateTime graceTime = getRule() != null ? getRange().getStart()
                    .minus(getRule().getEventGrace(), ChronoUnit.MINUTES)
                    : getRange().getStart();

            List<Member> members = getMembers().stream()
                    .filter(member -> "A".equals(member.getStatus())
                            && "U".equals(member.getRole()))
                    .toList();
            setNumOfMembers(members.size());

            getMembers().stream().map(member -> {
                if ("L".equals(member.getStatus())
                        && member.getUpdatedDate().isAfter(graceTime)) {
                    member.setStatus("LL");
                }
                return member;
            });

            // admin or promoter
            if (getRef() != null) {

                // promotion needs to cancel all the subEvents
                int promoterCnt = (int) getMembers().stream()
                        .filter(member -> "P".equals(member.getRole()) && "A".equals(member.getStatus()))
                        .count();

                if (promoterCnt == 0) {
                    List<Event> events = getItems().stream().map(item -> {
                        item.setStatus("T");
                        return item;
                    }).toList();
                    setItems(events);
                    setStatus("T");
                }
            } else {
                int adminCnt = (int) getMembers().stream()
                        .filter(member -> "A".equals(member.getRole()) && "A".equals(member.getStatus()))
                        .count();

                if (adminCnt == 0) {
                    Optional<Member> optMemberMin = getMembers().stream()
                            .filter(member -> "U".equals(member.getRole()))
                            .min((cap1, cap2) -> cap1.getCreatedDate().isBefore(cap2.getCreatedDate()) ? -1
                                    : 1);

                    if (optMemberMin.isPresent()) {
                        Member memberMin = optMemberMin.get();
                        memberMin.setRole("A");
                        getMembers().add(memberMin);
                    }
                }
            }
        }

        // algorithm will calculate whether new member needs to be added, when someone
        // leaves
        if (getCapacity() != null && !"A".equals(getStatus())) {
            int diff = getCapacity().getMax() - getNumOfMembers();

            if (!Boolean.TRUE.equals(getMultislot())) {

                if (getRule() != null
                        && Boolean.TRUE.equals(getRule().getBalanced())) {
                    balanceByGender("W", "J", diff);
                    balanceByGender("J", "W", null);
                    balanceByGender("A", "W", null);
                } else {
                    balance("W", "J", diff);
                    balance("J", "W", null);
                    balance("A", "W", null);
                }
            }
        }

        if (!"A".equals(getStatus())) {
            syncStatus();
        }

    }

    private void balance(String fromStatus, String toStatus, Integer pDiff) {
        int lDiff = 0;
        if (pDiff != null) {
            lDiff = pDiff.intValue();
        } else {
            int cntWithJ = (int) getMembers().stream()
                    .filter(member -> fromStatus.equals(member.getStatus())).count();
            lDiff = cntWithJ - getCapacity().getMax();
        }

        if (lDiff > 0) {
            getMembers().stream()
                    .filter(member -> fromStatus.equals(member.getStatus()))
                    .sorted((m1, m2) -> m2.getUpdatedDate().compareTo(m1.getUpdatedDate()))
                    .limit(lDiff).map(member -> {
                        member.setStatus(toStatus);
                        return member;
                    }).toList();
        }
    }

    private void balanceByGender(String fromStatus, String toStatus, Integer pDiff) {
        int lDiff = 0;
        if (pDiff != null) {
            lDiff = pDiff.intValue();
        } else {
            int cntWithJ = (int) getMembers().stream()
                    .filter(member -> fromStatus.equals(member.getStatus())).count();
            lDiff = cntWithJ - getCapacity().getMax();
        }

        int halfDiff = (int) ((float) lDiff / 2);

        if (lDiff > 0) {
            List<Member> menW = getMembers().stream()
                    .filter(member -> fromStatus.equals(member.getStatus())
                            && "m".equals(member.getProfile().getGender()))
                    .sorted((m1, m2) -> m1.getUpdatedDate().compareTo(m2.getUpdatedDate())).limit(halfDiff)
                    .map(member -> {
                        member.setStatus(toStatus);
                        return member;
                    }).toList();

            List<Member> womenW = getMembers().stream()
                    .filter(member -> fromStatus.equals(member.getStatus())
                            && "w".equals(member.getProfile().getGender()))
                    .sorted((m1, m2) -> m1.getUpdatedDate().compareTo(m2.getUpdatedDate())).limit(halfDiff)
                    .map(member -> {
                        member.setStatus(toStatus);
                        return member;
                    }).toList();

            int diffGW = menW.size() - womenW.size();
            if (diffGW < 0) {
                menW.stream().map(man -> {
                    man.setStatus(toStatus);
                    return man;
                }).toList();
            } else if (diffGW > 0) {
                womenW.stream().map(woman -> {
                    woman.setStatus(toStatus);
                    return woman;
                }).toList();
            }
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
                    List<Event> lItems = getItems().stream()
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

    public String getAccess() {
        return access;
    }

    public void setAccess(String accessLevel) {
        this.access = accessLevel;
    }

    public Boolean getAutoInvite() {
        return autoInvite;
    }

    public void setAutoInvite(Boolean autoInvite) {
        this.autoInvite = autoInvite;
    }

    public int getStageIdx() {
        return stageIdx;
    }

    public void setStageIdx(int stageIdx) {
        this.stageIdx = stageIdx;
    }

    public Boolean getDiscreet() {
        return discreet;
    }

    public void setDiscreet(Boolean discreet) {
        this.discreet = discreet;
    }

    public Boolean getMultislot() {
        return multislot;
    }

    public void setMultislot(Boolean multislot) {
        this.multislot = multislot;
    }

    public int getNumOfEvents() {
        return numOfEvents;
    }

    public void setNumOfEvents(int numOfEvents) {
        this.numOfEvents = numOfEvents;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public Set<Match> getMatches() {
        return matches;
    }

    public void setMatches(Set<Match> matches) {
        this.matches = matches;
    }

    public int getSlotIdx() {
        return slotIdx;
    }

    public void setSlotIdx(int slotIdx) {
        this.slotIdx = slotIdx;
    }

    @Override
    public Event clone(Profile profile) throws CloneNotSupportedException {
        Event clonedEvent = (Event) super.clone();
        clonedEvent.setId(UUID.randomUUID());

        clonedEvent.setCreatedDate(LocalDateTime.now());
        clonedEvent.setCreatedBy(profile.getId());
        clonedEvent.setUpdatedDate(LocalDateTime.now());
        clonedEvent.setUpdatedBy(profile.getId());

        if (clonedEvent.getItems() != null) {
            List<Event> items = clonedEvent.getItems().stream().map(item -> {
                try {
                    Event sItem = (Event) item.clone(profile);
                    // nullify ref in childs
                    item.setRefCnt(item.getRefCnt() - 1);
                    sItem.setRef(null);
                    return sItem;
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace(); // logger needs to be added
                }

                return null;
            }).filter(item -> item != null).toList();
            clonedEvent.setItems(items);
        }

        this.setRefCnt(clonedEvent.getRefCnt() + 1);
        clonedEvent.setRef(this.getId());

        if ("T".equals(this.getType())) {
            clonedEvent.setType("E");
        }

        return clonedEvent;
    }

    @Override
    public List<Event> flatten() {
        List<Event> events = new ArrayList<>();
        events.add(this);
        if (getItems() != null) {
            List<Event> items = getItems()
                    .stream().flatMap(item -> item.flatten().stream()).toList();
            events.addAll(items);
        }
        return events;
    }

    public void timeOut() {
        setStatus("T");

        if (getItems() != null) {
            for (Event item : getItems()) {
                item.timeOut();
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Event other = (Event) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}

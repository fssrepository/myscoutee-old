package com.raxim.myscoutee.profile.data.document.mongo;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonProperty;

//utolso x alkalommal akikkel talalkoztam szurje ki -> az esemeny ideje, es neve jelenjen meg felul (fieldset)
//random esemeny utan az egyezo nemuekre is lehet szavazni -> az esemeny koedukalt parameteru

//lista kepernyo ezekkel a szurokkel, hogy lehessen latni milyen sorrendben hivja meg az embereket
//az elso x ember a kapacitas alapjan mas szinu

//contact kepernyorol szures (sorba van rendezve kolcsonos rate-k alapjan)

@Document(collection = "rules")
public class Rule {
    // join first vs priority (rate based)
    @JsonProperty(value = "priority")
    private Boolean priority;

    // only when it's join first and simple event
    @JsonProperty(value = "autoApprove")
    private Boolean autoApprove;

    // the capacity should be fulfilled before the event starts with the grace
    // period
    @JsonProperty(value = "eventGrace")
    private int eventGrace;

    // invited, but didn't respond within the grace period, new candidates are
    // invited, but that ones are not timed out
    @JsonProperty(value = "memberGrace")
    private int memberGrace;

    // don't invite less than 2-3 rated people for the event
    @JsonProperty(value = "rate")
    private Integer rate;

    // fifa ranking (leaderboard) etc., none is a value also, rate (rate for
    // each other mutually)
    @JsonProperty(value = "rankType")
    private String rankType;

    /*
     * kesobb boviteni lehet
     * met field has been removed we might need to look at one level down,
     * (ismeros ismerosei)
     */
    /*
     * in the case of competition it has a different meaning
     * -> defines the most liked person inside an event can go to the next stage
     * A and F does not make sense on first stage, just after when you roll to the next stage on a competition
     */
    @JsonProperty(value = "mutual")
    private Boolean mutual;

    // ehelyett utolso x esemeny - ido bar-on kivalasztva
    // nem regebbi mint 'from' esemeny
    @JsonProperty(value = "from")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    // Balanced Female/Male Ratio
    @JsonProperty(value = "balanced")
    private Boolean balanced;

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    // Balanced Female/Male Ratio
    public Boolean getBalanced() {
        return balanced;
    }

    public void setBalanced(Boolean balanced) {
        this.balanced = balanced;
    }

    public Boolean getMutual() {
        return mutual;
    }

    public void setMutual(Boolean mutual) {
        this.mutual = mutual;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public void setFrom(LocalDateTime from) {
        this.from = from;
    }

    public Boolean getPriority() {
        return priority;
    }

    public void setPriority(Boolean priority) {
        this.priority = priority;
    }

    public int getEventGrace() {
        return eventGrace;
    }

    public void setEventGrace(int eventGrace) {
        this.eventGrace = eventGrace;
    }

    public int getMemberGrace() {
        return memberGrace;
    }

    public void setMemberGrace(int memberGrace) {
        this.memberGrace = memberGrace;
    }

    public String getRankType() {
        return rankType;
    }

    public void setRankType(String rankType) {
        this.rankType = rankType;
    }

    public Boolean getAutoApprove() {
        return autoApprove;
    }

    public void setAutoApprove(Boolean autoApprove) {
        this.autoApprove = autoApprove;
    }
}

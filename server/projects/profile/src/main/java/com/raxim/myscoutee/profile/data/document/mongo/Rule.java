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
    @JsonProperty(value = "priority")
    private Boolean priority;

    // the capacity should be fulfilled before the event starts with the grace
    // period
    @JsonProperty(value = "gracePeriod")
    private int gracePeriod;

    /*
     * kesobb boviteni lehet
     * met field has been removed we might need to look at one level down,
     * (ismeros ismerosei)
     */
    @JsonProperty(value = "mutual")
    private Boolean mutual;

    // don't invite less than 2-3 rated people for the event
    @JsonProperty(value = "rate")
    private Integer rate;

    // ehelyett utolso x esemeny - ido bar-on kivalasztva
    // nem regebbi mint 'from' esemeny
    @JsonProperty(value = "from")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    // Balanced Female/Male Ratio
    @JsonProperty(value = "balanced")
    private String balanced;

    // don't invite less than 2-3 rated people for the event
    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    // Balanced Female/Male Ratio
    public String getBalanced() {
        return balanced;
    }

    public void setBalanced(String balanced) {
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

    public int getGracePeriod() {
        return gracePeriod;
    }

    public void setGracePeriod(int gracePeriod) {
        this.gracePeriod = gracePeriod;
    }
}

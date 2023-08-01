package com.raxim.myscoutee.profile.data.document.mongo.iface;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.raxim.myscoutee.profile.data.document.mongo.Amount;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.RangeInt;
import com.raxim.myscoutee.profile.data.document.mongo.RangeLocal;

public abstract class EventBase {
    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "desc")
    private String desc;

    @JsonProperty(value = "range")
    private RangeLocal range = new RangeLocal();

    // Active (A), Deleted (D), Timed Out (T), Pending (P)
    @JsonProperty(value = "status")
    private String status = "P";

    @JsonIgnore
    private LocalDateTime createdDate = LocalDateTime.now();

    @JsonIgnore
    private LocalDateTime updatedDate = LocalDateTime.now();

    @JsonIgnore
    private UUID createdBy; // can be system uuid also, not only a valid profile

    @JsonIgnore
    private UUID updatedBy; // can be system uuid also, not only a valid profile

    @JsonProperty(value = "type")
    private String type;

    // price
    @JsonProperty(value = "amount")
    private Amount amount;

    @JsonProperty(value = "capacity")
    private RangeInt capacity;

    @JsonProperty(value = "urlRef")
    private String urlRef;

    // number of members, nem tudom, hogy kell-e...
    @JsonProperty(value = "num")
    private int numOfMembers;

    @JsonIgnore
    @JsonProperty(value = "members")
    private Set<Member> members = new HashSet<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RangeInt getCapacity() {
        return capacity;
    }

    public void setCapacity(RangeInt capacity) {
        this.capacity = capacity;
    }

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    public String getUrlRef() {
        return urlRef;
    }

    public void setUrlRef(String urlRef) {
        this.urlRef = urlRef;
    }

    public Set<Member> getMembers() {
        return members;
    }

    public void setMembers(Set<Member> members) {
        this.members = members;
    }

    public int getNumOfMembers() {
        return numOfMembers;
    }

    public void setNumOfMembers(int num) {
        this.numOfMembers = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public RangeLocal getRange() {
        return range;
    }

    public void setRange(RangeLocal range) {
        this.range = range;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }
}

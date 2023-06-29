package com.raxim.myscoutee.profile.data.document.mongo;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.raxim.myscoutee.common.repository.GeoJsonPointDeserializer;

@Document(collection = "items")
public class EventItem {
    @Id
    @JsonProperty(value = "key")
    private UUID id;

    // aki jelentkezett kulon chat ablak -> pl. fuvar
    @JsonProperty(value = "optional")
    private Boolean optional; // not mandatory event - not all participant needs to attend

    // random, main -> only one (there is no random and main -> if all the places
    // has been invited no calculation), general, car, accommodation, private (like
    // phone number)
    // event is published if capacity has been set - auto capacity
    @JsonProperty(value = "type")
    private String type;

    /* local, global */
    /*
     * google map is complicated to get the coordinates to search for,
     * but to differentiate local and global based on that is ridiculous
     */
    @JsonProperty(value = "category")
    private String category;

    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "desc")
    private String desc;

    @JsonProperty(value = "range")
    private RangeLocal range;

    @JsonProperty(value = "capacity")
    private RangeInt capacity;

    // price
    @JsonProperty(value = "amount")
    private Amount amount;

    // qr code generation
    @JsonProperty(value = "ticket")
    private Boolean ticket;

    // google, booking.com etc., az alkalmazas belso url-je is,
    // vagyis ilyenkor egy popup ablak jelenik meg pl. a kocsi infoival
    @JsonProperty(value = "urlRef")
    private String urlRef;

    @GeoSpatialIndexed(name = "position", type = GeoSpatialIndexType.GEO_2DSPHERE)
    @JsonDeserialize(using = GeoJsonPointDeserializer.class)
    @JsonProperty(value = "position")
    private GeoJsonPoint position;

    // Active (A), Deleted (D)
    @JsonProperty(value = "status")
    private String status = "A";

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonProperty(value = "createdDate")
    private LocalDateTime createdDate = LocalDateTime.now();

    @JsonProperty(value = "priority")
    private Boolean priority;

    @JsonProperty(value = "rule")
    private Rule rule;

    @JsonIgnore
    private UUID createdBy; // can be system uuid also, not only a valid profile

    // ?? - no idea what is it
    @JsonProperty(value = "memberRange")
    private RangeLocal memberRange;

    // number of members, nem tudom, hogy kell-e...
    @JsonProperty(value = "num")
    private int num;

    // opcionalis esemenyek - car is egy esemeny, amihez lehet csatlakozni
    // memberek telefonszama nincs itt mentve, egyelore
    @JsonIgnore
    @JsonProperty(value = "members")
    private Set<Member> members;

    // -------------nem kell--------------

    // ez nem lesz,sajat google notification chat, kesobb web socket
    // signal url
    @JsonProperty(value = "chatKey")
    private String chatKey;

    // nem kell, chat-ben megy a kommunikacio
    @JsonProperty(value = "telNum")
    private String telNum;

    // nem kell urlRef lefedi
    @JsonProperty(value = "car")
    private Car car;

    // Add getters and setters for all fields

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getUrlRef() {
        return urlRef;
    }

    public void setUrlRef(String urlRef) {
        this.urlRef = urlRef;
    }

    public String getChatKey() {
        return chatKey;
    }

    public void setChatKey(String chatKey) {
        this.chatKey = chatKey;
    }

    public RangeLocal getRange() {
        return range;
    }

    public void setRange(RangeLocal range) {
        this.range = range;
    }

    public RangeInt getCapacity() {
        return capacity;
    }

    public void setCapacity(RangeInt capacity) {
        this.capacity = capacity;
    }

    public String getTelNum() {
        return telNum;
    }

    public void setTelNum(String telNum) {
        this.telNum = telNum;
    }

    public Boolean getOptional() {
        return optional;
    }

    public void setOptional(Boolean optional) {
        this.optional = optional;
    }

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    public Boolean getTicket() {
        return ticket;
    }

    public void setTicket(Boolean ticket) {
        this.ticket = ticket;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public RangeLocal getMemberRange() {
        return memberRange;
    }

    public void setMemberRange(RangeLocal memberRange) {
        this.memberRange = memberRange;
    }

    public GeoJsonPoint getPosition() {
        return position;
    }

    public void setPosition(GeoJsonPoint position) {
        this.position = position;
    }

    public Set<Member> getMembers() {
        return members;
    }

    public void setMembers(Set<Member> members) {
        this.members = members;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
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

    public Boolean getPriority() {
        return priority;
    }

    public void setPriority(Boolean priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}

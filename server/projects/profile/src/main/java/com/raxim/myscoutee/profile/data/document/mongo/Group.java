package com.raxim.myscoutee.profile.data.document.mongo;

import java.time.LocalDateTime;
import java.util.List;
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

@Document(collection = "groups")
public class Group implements Cloneable {
    @Id
    @JsonProperty(value = "key")
    private UUID id;

    //T = Team, G = Group
    //inside project list out Teams, show Unassigned Team as base etc.
    @JsonProperty(value = "type")
    private String type;

    /*
     * values: dating
     */
    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "category")
    private String category;

    // public
    @JsonProperty(value = "access")
    private String access;

    @JsonProperty(value = "images")
    private List<Image> images;

    @JsonProperty(value = "system")
    private Boolean system = false;

    @JsonProperty(value = "desc")
    private String desc;

    @GeoSpatialIndexed(name = "position", type = GeoSpatialIndexType.GEO_2DSPHERE)
    @JsonDeserialize(using = GeoJsonPointDeserializer.class)
    @JsonProperty(value = "position")
    private GeoJsonPoint position;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonProperty(value = "createdDate")
    private LocalDateTime createdDate = LocalDateTime.now();

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonProperty(value = "updatedDate")
    private LocalDateTime updatedDate = LocalDateTime.now();

    // profileId
    @JsonIgnore
    private UUID createdBy;

    @JsonIgnore
    private UUID updatedBy;

    @JsonProperty(value = "status")
    private String status = "P";

    //isDescreet / discreet level (school is visible for others???)
    @JsonProperty(value = "privacy")
    private Privacy privacy;

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

    public String getAccess() {
        return access;
    }

    public void setAccess(String visibility) {
        this.access = visibility;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public Boolean getSystem() {
        return system;
    }

    public void setSystem(Boolean system) {
        this.system = system;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public GeoJsonPoint getPosition() {
        return position;
    }

    public void setPosition(GeoJsonPoint position) {
        this.position = position;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public Object clone()
            throws CloneNotSupportedException {
        return super.clone();
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Privacy getPrivacy() {
        return privacy;
    }

    public void setPrivacy(Privacy discreet) {
        this.privacy = discreet;
    }
}

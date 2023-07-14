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

    /*
     * values: dating
     */
    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "category")
    private String category;

    // public
    @JsonProperty(value = "visibility")
    private String visibility;

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

    // profileId
    @JsonIgnore
    private UUID createdBy;

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

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
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
}

package com.raxim.myscoutee.profile.data.document.mongo;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.raxim.myscoutee.common.repository.GeoJsonPointDeserializer;

@Document(collection = "profiles")
public class Profile {
    @Id
    @JsonProperty(value = "key")
    private UUID id;
    
    @JsonProperty(value = "firstName")
    private String firstName;
    
    @JsonProperty(value = "gender")
    private String gender;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonProperty(value = "birthday")
    private Date birthday;
    
    @JsonProperty(value = "smoker")
    private Boolean smoker;
    
    @JsonProperty(value = "marital")
    private String marital;
    
    @JsonProperty(value = "hasChild")
    private Boolean hasChild;
    
    @JsonProperty(value = "height")
    private Integer height;
    
    @JsonProperty(value = "physique")
    private String physique;
    
    @JsonProperty(value = "languages")
    private List<String> languages;
    
    @JsonProperty(value = "religion")
    private String religion;
    
    @JsonProperty(value = "profession")
    private String profession;
    
    @JsonProperty(value = "images")
    private List<Image> images;
    
    @JsonProperty(value = "voice")
    private String voice;
    
    @JsonProperty(value = "desc")
    private String desc;
    
    @JsonProperty(value = "website")
    private String website;
    
    @DBRef
    @JsonIgnore
    private List<Car> cars;
    
    @DBRef
    @JsonIgnore
    private List<School> schools;
    
    @GeoSpatialIndexed(name = "position", type = GeoSpatialIndexType.GEO_2DSPHERE)
    @JsonDeserialize(using = GeoJsonPointDeserializer.class)
    @JsonIgnore
    private GeoJsonPoint position;
    
    @JsonProperty(value = "status")
    private String status = "I"; //P (pending), F (friends only), I (invisible), A (All), S (suspended), D (deleted - group leave)
    
    @JsonIgnore
    private Date createdDate;
    
    @JsonIgnore
    private UUID group;
    
    @JsonIgnore
    private int score;
    
    @JsonIgnore
    private LocalDateTime lastLogin;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Boolean getSmoker() {
        return smoker;
    }

    public void setSmoker(Boolean smoker) {
        this.smoker = smoker;
    }

    public String getMarital() {
        return marital;
    }

    public void setMarital(String marital) {
        this.marital = marital;
    }

    public Boolean getHasChild() {
        return hasChild;
    }

    public void setHasChild(Boolean hasChild) {
        this.hasChild = hasChild;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getPhysique() {
        return physique;
    }

    public void setPhysique(String physique) {
        this.physique = physique;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public List<Car> getCars() {
        return cars;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }

    public List<School> getSchools() {
        return schools;
    }

    public void setSchools(List<School> schools) {
        this.schools = schools;
    }

    public GeoJsonPoint getPosition() {
        return position;
    }

    public void setPosition(GeoJsonPoint position) {
        this.position = position;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public UUID getGroup() {
        return group;
    }

    public void setGroup(UUID group) {
        this.group = group;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
}

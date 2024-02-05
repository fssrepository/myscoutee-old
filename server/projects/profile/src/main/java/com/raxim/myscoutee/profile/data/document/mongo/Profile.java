package com.raxim.myscoutee.profile.data.document.mongo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
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
public class Profile implements Cloneable {

    public static final Set<String> ACTIVE = Set.of("A", "F");

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

    /*
     * Single
     * Married
     * Divorced
     * Widowed
     * Separated
     */
    @JsonProperty(value = "marital")
    private String marital;

    @JsonProperty(value = "hasChild")
    private Boolean hasChild;

    @JsonProperty(value = "height")
    private Integer height;

    /*
     * Slim
     * Athletic
     * Average
     * Curvy
     * Muscular
     */
    @JsonProperty(value = "physique")
    private String physique;

    @JsonProperty(value = "languages")
    private List<String> languages;

    /*
     * Christianity - Over 2.4 billion followers
     * Islam - Over 1.9 billion followers
     * Hinduism - Over 1.2 billion followers
     * Buddhism - Over 500 million followers
     * Sikhism - Over 30 million followers
     * Judaism - Over 14 million followers
     * Bahá'í Faith - Over 7 million followers
     * Jainism - Over 4.5 million followers
     * Shintoism - Over 4 million followers
     * Taoism - Over 12 million followers
     * Zoroastrianism - Around 200,000 followers
     * Atheism
     * Other
     */
    @JsonProperty(value = "religion")
    private String religion;

    /*
     * Engineer
     * Teacher
     * Doctor
     * Artist
     * Scientist
     * Lawyer
     * Chef
     * Accountant
     * Musician
     * Writer
     * Salesperson
     * Athlete
     * Architect
     * Nurse
     * Entrepreneur
     * Logistics Coordinator
     */
    // when user types, it shows suggestions, hence he can select, but can add a new
    // value
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
    private List<Car> cars = new ArrayList<>();

    @DBRef
    @JsonIgnore
    private List<School> schools = new ArrayList<>();

    @GeoSpatialIndexed(name = "position", type = GeoSpatialIndexType.GEO_2DSPHERE)
    @JsonDeserialize(using = GeoJsonPointDeserializer.class)
    private GeoJsonPoint position;

    /*
     * P (pending),
     * F (friends only),
     * I (invisible),
     * A (All),
     * S (suspended),
     * L (group leave)
     */
    @JsonProperty(value = "status")
    private String status = "P";

    // group's role
    @JsonProperty(value = "role")
    private String role;

    @JsonIgnore
    private LocalDateTime createdDate;

    @JsonIgnore
    private UUID createdBy;

    @JsonIgnore
    private LocalDateTime updatedDate;

    @JsonIgnore
    private UUID updatedBy;

    @JsonIgnore
    private UUID group;

    @JsonIgnore
    private int score;

    @JsonIgnore
    private LocalDateTime lastActive;

    // is mqtt connection active?
    @JsonIgnore
    private Boolean mqtt;

    // which page the profile in?
    @JsonIgnore
    private String currentPage;

    public Profile() {
    }

    public Profile(UUID id) {
        this.id = id;
    }

    @Override
    public Profile clone() {
        try {
            Profile clonedProfile = (Profile) super.clone();
            clonedProfile.createdDate = LocalDateTime.now();
            clonedProfile.lastActive = LocalDateTime.now();
            return clonedProfile;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Cloning not supported for Profile");
        }
    }

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

    @JsonIgnore
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

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
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

    public LocalDateTime getLastActive() {
        return lastActive;
    }

    public void setLastActive(LocalDateTime lastLogin) {
        this.lastActive = lastLogin;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
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
        Profile other = (Profile) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    public static Set<String> getActive() {
        return ACTIVE;
    }

    public Boolean getMqtt() {
        return mqtt;
    }

    public void setMqtt(Boolean mqtt) {
        this.mqtt = mqtt;
    }

    public String getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
    }
}

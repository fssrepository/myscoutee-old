package com.raxim.myscoutee.profile.data.document.mongo;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

// role for event
@Document(collection = "members")
public class Member {

    public static final Set<String> MET = Set.of("A", "L", "LL", "V", "LD");

    @DBRef
    @JsonProperty(value = "profile")
    private Profile profile;

    // A (Accepted), R (rejected), I (invited), K (kicked)?? - maybe by more members
    // than one, L (left) V (Verified)
    // T (Timed-Out), M (manager), P (promoter), LL (late leave), W (on waiting
    // list), LD (left deleted), D (deleted)
    @JsonProperty(value = "status")
    private String status;

    // User (U), Admin (A), Promoter (P)
    @JsonProperty(value = "role")
    private String role;

    // JsonIgnore for field - authorization who can read it
    @JsonIgnore
    private String code;

    @JsonIgnore
    private LocalDateTime createdDate;

    // event item ref - members will be removed from event item
    @JsonIgnore
    private UUID eventRef;

    public Member(Profile profile) {
        this(profile, "A", "U");
    }

    public Member(Profile profile, String status, String role) {
        this.profile = profile;
        this.status = status;
        this.role = role;
    }

    public Member() {
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public UUID getEventRef() {
        return eventRef;
    }

    public void setEventRef(UUID eventRef) {
        this.eventRef = eventRef;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((profile == null) ? 0 : profile.hashCode());
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
        Member other = (Member) obj;
        if (profile == null) {
            if (other.profile != null)
                return false;
        } else if (!profile.equals(other.profile))
            return false;
        return true;
    }
}

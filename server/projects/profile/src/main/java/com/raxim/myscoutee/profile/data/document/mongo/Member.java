package com.raxim.myscoutee.profile.data.document.mongo;

import java.util.Date;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

// role for event
@Document(collection = "members")
public class Member {
    // profile id - only for the query?? it might not be needed, we can get
    // profile.id
    @Id
    @JsonProperty(value = "key")
    private UUID id;

    @DBRef
    @JsonProperty(value = "profile")
    private Profile profile;

    // A (Accepted), R (rejected), I (invited), K (kicked)?? - maybe by more members
    // than one, L (left) V (Verified)
    // T (Timed-Out), M (manager), P (promoter), LL (late leave), W (on waiting
    // list)
    @JsonProperty(value = "status")
    private String status;

    // User (U), Admin (A), Promoter (P)
    @JsonProperty(value = "role")
    private String role;

    // JsonIgnore for field - authorization who can read it
    @JsonIgnore
    private String code;

    @JsonIgnore
    private Date createdDate;

    // event item ref - members will be removed from event item
    @JsonIgnore
    private UUID eventRef;

    public Member(Profile profile, String status, String role) {
        this.id = this.profile.getId(); //it might not be needed
        this.profile = profile;
        this.status = status;
        this.role = role;
    }

    public Member() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
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
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (profile == null) {
            if (other.profile != null)
                return false;
        } else if (!profile.equals(other.profile))
            return false;
        return true;
    }
}

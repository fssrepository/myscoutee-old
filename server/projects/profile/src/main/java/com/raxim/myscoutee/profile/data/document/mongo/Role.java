package com.raxim.myscoutee.profile.data.document.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.UUID;

@Document(collection = "roles")
public class Role {
    @Id
    private UUID id;
    private UUID profileId;
    private String role;
    private String status = "A";
    private UUID refId;
    
    public Role() {
        this.id = UUID.randomUUID();
    }
    
    public Role(UUID profileId, String role) {
        this.id = UUID.randomUUID();
        this.profileId = profileId;
        this.role = role;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getProfileId() {
        return profileId;
    }
    
    public void setProfileId(UUID profileId) {
        this.profileId = profileId;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public UUID getRefId() {
        return refId;
    }
    
    public void setRefId(UUID refId) {
        this.refId = refId;
    }
}

package com.raxim.myscoutee.profile.data.dto.rest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.raxim.myscoutee.profile.data.document.mongo.Badge;
import com.raxim.myscoutee.profile.data.document.mongo.User;

@JsonRootName("user")
public class UserDTO extends PageItemDTO {
    @JsonProperty(value = "user")
    private User user;

    @JsonProperty(value = "likes")
    private List<Badge> likes;

    public UserDTO(User user, List<Badge> likes) {
        this.user = user;
        this.likes = likes;
    }

    public UserDTO() {
        // Default constructor
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Badge> getLikes() {
        return likes;
    }

    public void setLikes(List<Badge> likes) {
        this.likes = likes;
    }
}

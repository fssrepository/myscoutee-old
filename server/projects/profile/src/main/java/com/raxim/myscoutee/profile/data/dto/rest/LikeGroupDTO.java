package com.raxim.myscoutee.profile.data.dto.rest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.raxim.myscoutee.profile.data.document.mongo.Like;

@JsonRootName("likes")
public class LikeGroupDTO {

    @JsonProperty(value = "cnt")
    private long _id;

    @JsonProperty(value = "likes")
    private List<Like> likes;

    public LikeGroupDTO() {
    }

    public LikeGroupDTO(long _id, List<Like> likes) {
        this._id = _id;
        this.likes = likes;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }
}

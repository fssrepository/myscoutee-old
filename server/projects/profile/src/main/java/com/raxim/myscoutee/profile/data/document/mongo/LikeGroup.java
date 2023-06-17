package com.raxim.myscoutee.profile.data.document.mongo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("likeGroups")
public class LikeGroup {

    @JsonProperty(value = "cnt")
    private long _id;

    @JsonProperty(value = "likes")
    private List<LikeForGroup> likes;

    public LikeGroup() {
    }

    public LikeGroup(long _id, List<LikeForGroup> likes) {
        this._id = _id;
        this.likes = likes;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public List<LikeForGroup> getLikes() {
        return likes;
    }

    public void setLikes(List<LikeForGroup> likes) {
        this.likes = likes;
    }
}

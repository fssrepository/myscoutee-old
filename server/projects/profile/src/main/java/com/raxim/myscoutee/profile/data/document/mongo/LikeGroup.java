package com.raxim.myscoutee.profile.data.document.mongo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.raxim.myscoutee.profile.util.LikeUtil;

@JsonRootName("likeGroups")
public class LikeGroup {

    @JsonProperty(value = "cnt")
    private long _id;

    @JsonProperty(value = "likes")
    private List<Like> likes;

    public LikeGroup() {
    }

    public LikeGroup(long _id, List<Like> likes) {
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

    public Like reduce() {
        List<Like> likesWithStatusP = likes.stream()
                .filter(like -> "A".equals(like.getStatus()))
                .toList();
        if (likesWithStatusP.size() == 2) {
            Like firstLike = likesWithStatusP.get(0);

            List<Double> ratesForStatusP = likesWithStatusP
                    .stream()
                    .map(like -> like.getRate())
                    .toList();

            List<Double> ratesForStatusD = likes
                    .stream()
                    .filter(like -> "D".equals(like.getStatus()))
                    .map(like -> like.getRate())
                    .toList();

            double rate = LikeUtil.calcAdjustedHarmonicMean(ratesForStatusP, ratesForStatusD);
            firstLike.setRate(rate);
            return firstLike;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "LikeGroup [_id=" + _id + ", likes=" + likes + "]";
    }
}

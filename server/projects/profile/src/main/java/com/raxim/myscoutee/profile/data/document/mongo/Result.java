package com.raxim.myscoutee.profile.data.document.mongo;

import java.util.Random;
import java.util.UUID;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "results")
public class Result implements Comparable<Result> {
    private UUID id;

    private UUID profileId;

    private Integer mp; // match played
    private Integer w; // wins
    private Integer d; // draws
    private Integer l; // losses
    private Integer gf; // goals for
    private Integer ga; // goals against
    private Integer gd; // goal difference
    private Integer pts; // points 3,1,0
    private Integer fpts; // fair playpoints

    public Result() {
    }

    public Result(UUID id) {
        this.id = id;
    }

    public Integer getMp() {
        return mp;
    }

    public void setMp(Integer mp) {
        this.mp = mp;
    }

    public Integer getW() {
        return w;
    }

    public void setW(Integer w) {
        this.w = w;
    }

    public Integer getD() {
        return d;
    }

    public void setD(Integer d) {
        this.d = d;
    }

    public Integer getL() {
        return l;
    }

    public void setL(Integer l) {
        this.l = l;
    }

    public Integer getGf() {
        return gf;
    }

    public void setGf(Integer gf) {
        this.gf = gf;
    }

    public Integer getGa() {
        return ga;
    }

    public void setGa(Integer ga) {
        this.ga = ga;
    }

    public Integer getGd() {
        return gd;
    }

    public void setGd(Integer gd) {
        this.gd = gd;
    }

    public Integer getPts() {
        return pts;
    }

    public void setPts(Integer pts) {
        this.pts = pts;
    }

    public Integer getFpts() {
        return fpts;
    }

    public void setFpts(Integer fpts) {
        this.fpts = fpts;
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

    @Override
    public int compareTo(Result other) {
        // First, compare based on the total points (pts) in descending order
        int pointsComparison = other.pts.compareTo(this.pts);
        if (pointsComparison != 0) {
            return pointsComparison;
        }

        // If points are equal, compare based on goal difference (gd) in descending
        // order
        int goalDifferenceComparison = other.gd.compareTo(this.gd);
        if (goalDifferenceComparison != 0) {
            return goalDifferenceComparison;
        }

        // If goal difference is also equal, compare based on goals for (gf) in
        // descending order
        int goalsForComparison = other.gf.compareTo(this.gf);
        if (goalsForComparison != 0) {
            return goalsForComparison;
        }

        // If goals for are also equal, compare based on the fair play points (fpts) in
        // ascending order
        int fairPlayComparison = this.fpts.compareTo(other.fpts);
        if (fairPlayComparison != 0) {
            return fairPlayComparison;
        }

        // If fair play points are also equal, use random tie-breaker
        Random random = new Random();
        return random.nextInt(3) - 1; // Generates a random value between -1 and 1 (inclusive)
    }
}

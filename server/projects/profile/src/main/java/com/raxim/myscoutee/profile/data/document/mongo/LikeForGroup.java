package com.raxim.myscoutee.profile.data.document.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LikeForGroup extends Like {
    @JsonProperty("cnt")
    public void setCnt(Long cnt) {
        super.setCnt(cnt);
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        super.setStatus(status);
    }

    public Profile getFrom() {
        return super.getFrom();
    }

    public void setFrom(ProfileForGroup from) {
        super.setFrom(from);
    }

    public Profile getTo() {
        return super.getTo();
    }

    public void setTo(ProfileForGroup to) {
        super.setTo(to);
    }
}

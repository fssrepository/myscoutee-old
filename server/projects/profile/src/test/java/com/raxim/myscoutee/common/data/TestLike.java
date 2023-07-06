package com.raxim.myscoutee.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.raxim.myscoutee.profile.data.document.mongo.Like;

public class TestLike extends Like {
    @JsonProperty("cnt")
    public void setCnt(Long cnt) {
        super.setCnt(cnt);
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        super.setStatus(status);
    }
}

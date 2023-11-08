package com.raxim.myscoutee.profile.data.dto.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.raxim.myscoutee.profile.data.document.mongo.Match;

@JsonRootName("match")
public class MatchDTO extends PageItemDTO {
    @JsonProperty(value = "item")
    private Match item;

    public MatchDTO() {
    }

    public MatchDTO(Match item) {
        this.item = item;
    }

    public Match getItem() {
        return item;
    }

    public void setItem(Match item) {
        this.item = item;
    }
}

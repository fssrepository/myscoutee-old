package com.raxim.myscoutee.profile.data.document.mongo;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "privacies")
public class Privacy {
    // members with User role do not see each other
    // it does mean separate chat window
    @JsonProperty(value = "discreet")
    private Boolean discreet;

    @JsonProperty(value = "level")
    private String level;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Boolean getDiscreet() {
        return discreet;
    }

    public void setDiscreet(Boolean discreet) {
        this.discreet = discreet;
    }
}

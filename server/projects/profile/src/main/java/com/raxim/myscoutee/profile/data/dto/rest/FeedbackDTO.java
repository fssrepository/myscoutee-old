package com.raxim.myscoutee.profile.data.dto.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("feedback")
public class FeedbackDTO {

    @JsonProperty(value = "feedback")
    private com.raxim.myscoutee.profile.data.document.mongo.Feedback feedback;

    @JsonIgnore
    private List<Object> offset;

    public com.raxim.myscoutee.profile.data.document.mongo.Feedback getFeedback() {
        return feedback;
    }

    public void setFeedback(com.raxim.myscoutee.profile.data.document.mongo.Feedback feedback) {
        this.feedback = feedback;
    }

    public List<Object> getOffset() {
        return offset;
    }

    public void setOffset(List<Object> offset) {
        this.offset = offset;
    }
}

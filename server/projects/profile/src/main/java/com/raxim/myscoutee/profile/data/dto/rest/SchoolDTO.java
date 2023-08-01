package com.raxim.myscoutee.profile.data.dto.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.raxim.myscoutee.profile.data.document.mongo.School;

@JsonRootName("school")
public class SchoolDTO extends PageItemDTO {
    @JsonProperty(value = "school")
    private School school;

    @JsonProperty(value = "groupKey")
    private Object groupKey;

    public SchoolDTO() {
    }

    public SchoolDTO(School school) {
        this.school = school;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public Object getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(Object groupKey) {
        this.groupKey = groupKey;
    }
}

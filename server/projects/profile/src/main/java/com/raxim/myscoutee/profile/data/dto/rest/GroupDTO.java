package com.raxim.myscoutee.profile.data.dto.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("group")
public class GroupDTO {

    @JsonProperty(value = "group")
    private com.raxim.myscoutee.profile.data.document.mongo.Group group;

    @JsonProperty(value = "groupKey")
    private Object groupKey;

    @JsonProperty(value = "role")
    private Object role;

    @JsonIgnore
    private List<Object> offset;

    public com.raxim.myscoutee.profile.data.document.mongo.Group getGroup() {
        return group;
    }

    public void setGroup(com.raxim.myscoutee.profile.data.document.mongo.Group group) {
        this.group = group;
    }

    public Object getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(Object groupKey) {
        this.groupKey = groupKey;
    }

    public Object getRole() {
        return role;
    }

    public void setRole(Object role) {
        this.role = role;
    }

    public List<Object> getOffset() {
        return offset;
    }

    public void setOffset(List<Object> offset) {
        this.offset = offset;
    }
}

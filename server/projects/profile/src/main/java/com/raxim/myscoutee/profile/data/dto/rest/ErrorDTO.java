package com.raxim.myscoutee.profile.data.dto.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("error")
public class ErrorDTO {

    @JsonProperty(value = "key")
    private Integer key;

    @JsonProperty(value = "value")
    private String value;

    public ErrorDTO(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public ErrorDTO() {
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

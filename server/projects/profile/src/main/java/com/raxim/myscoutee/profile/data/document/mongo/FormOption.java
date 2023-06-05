package com.raxim.myscoutee.profile.data.document.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FormOption {
    @JsonProperty(value = "value")
    private String value;

    @JsonProperty(value = "viewValue")
    private String viewValue;

    // Getter and Setter methods for each field

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getViewValue() {
        return viewValue;
    }

    public void setViewValue(String viewValue) {
        this.viewValue = viewValue;
    }
}
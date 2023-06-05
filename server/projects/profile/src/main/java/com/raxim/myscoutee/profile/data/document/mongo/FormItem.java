package com.raxim.myscoutee.profile.data.document.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/*
    1)  it can be array, object etc. -> dynamic form
*/

public class FormItem {
    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "type")
    private String type;

    @JsonProperty(value = "data")
    private Object data;

    @JsonProperty(value = "options")
    private List<FormOption> options;

    // Getter and Setter methods for each field

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public List<FormOption> getOptions() {
        return options;
    }

    public void setOptions(List<FormOption> options) {
        this.options = options;
    }
}
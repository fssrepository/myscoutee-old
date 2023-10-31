package com.raxim.myscoutee.profile.data.document.mongo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FormItem {
    @JsonProperty(value = "name")
    private String name;

    // date steps = ds
    @JsonProperty(value = "type")
    private String type;

    // it can be array, object etc. -> dynamic form
    @JsonProperty(value = "data")
    private Object data;

    @JsonProperty(value = "options")
    private List<FormOption> options;

    @JsonProperty(value = "range")
    private Object range;

    public Object getRange() {
        return range;
    }

    public void setRange(Object range) {
        this.range = range;
    }

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

package com.raxim.myscoutee.profile.data.dto.rest;

public class ISODateRange {
    private String min;
    private String max;
    
    public ISODateRange(String min, String max) {
        this.min = min;
        this.max = max;
    }
    public ISODateRange() {
    }
    public String getMin() {
        return min;
    }
    public void setMin(String min) {
        this.min = min;
    }
    public String getMax() {
        return max;
    }
    public void setMax(String max) {
        this.max = max;
    }
}

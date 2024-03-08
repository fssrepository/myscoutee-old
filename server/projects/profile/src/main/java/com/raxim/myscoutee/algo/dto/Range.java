package com.raxim.myscoutee.algo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Range {
    @JsonProperty(value = "min")
    private int min;

    @JsonProperty(value = "max")
    private int max;

    public static Range of(int min, int max) {
        Range rangeInt = new Range();
        rangeInt.setMin(min);
        rangeInt.setMax(max);
        return rangeInt;
    }

    public Range add(int size) {
        Range rangeInt = new Range();
        rangeInt.setMin(this.getMin() + size);
        rangeInt.setMax(this.getMax() + size);
        return rangeInt;
    }

    public Range() {
    }

    public Range(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setMax(int max) {
        this.max = max;
    }
}

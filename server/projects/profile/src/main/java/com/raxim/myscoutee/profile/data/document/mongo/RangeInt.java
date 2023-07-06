package com.raxim.myscoutee.profile.data.document.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RangeInt {
    @JsonProperty(value = "min")
    private int min;

    @JsonProperty(value = "max")
    private int max;

    public static RangeInt of(int min, int max) {
        RangeInt rangeInt = new RangeInt();
        rangeInt.setMin(min);
        rangeInt.setMax(max);
        return rangeInt;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }
}

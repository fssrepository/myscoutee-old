package com.raxim.myscoutee.profile.data.document.mongo;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.raxim.myscoutee.algo.dto.Range;

public class Slot {
    @JsonProperty(value = "range")
    private RangeLocal range;

    @JsonProperty(value = "numOfItems")
    private int numOfItems;

    @JsonProperty(value = "capacity")
    private Range capacity;

    public RangeLocal getRange() {
        return range;
    }

    public void setRange(RangeLocal range) {
        this.range = range;
    }

    public int getNumOfItems() {
        return numOfItems;
    }

    public void setNumOfItems(int numOfItems) {
        this.numOfItems = numOfItems;
    }

    public Range getCapacity() {
        return capacity;
    }

    public void setCapacity(Range capacity) {
        this.capacity = capacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Slot slot = (Slot) o;
        return Objects.equals(range, slot.range);
    }

    @Override
    public int hashCode() {
        return Objects.hash(range);
    }
}

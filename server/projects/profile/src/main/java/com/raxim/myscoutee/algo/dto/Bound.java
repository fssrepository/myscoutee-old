package com.raxim.myscoutee.algo.dto;

public class Bound {
    private int minGroupSize;
    private int maxGroupSize;

    public Bound() {
        // Default constructor
    }

    public int getMinGroupSize() {
        return minGroupSize;
    }

    public void setMinGroupSize(int minGroupSize) {
        this.minGroupSize = minGroupSize;
    }

    public int getMaxGroupSize() {
        return maxGroupSize;
    }

    public void setMaxGroupSize(int maxGroupSize) {
        this.maxGroupSize = maxGroupSize;
    }
}

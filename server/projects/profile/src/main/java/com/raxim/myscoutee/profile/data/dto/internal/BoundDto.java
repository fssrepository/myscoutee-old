package com.raxim.myscoutee.profile.data.dto.internal;

public class BoundDto {
    private final int minGroupSize;
    private final int maxGroupSize;

    public BoundDto(int minGroupSize, int maxGroupSize) {
        this.minGroupSize = minGroupSize;
        this.maxGroupSize = maxGroupSize;
    }

    public int getMinGroupSize() {
        return minGroupSize;
    }

    public int getMaxGroupSize() {
        return maxGroupSize;
    }
}

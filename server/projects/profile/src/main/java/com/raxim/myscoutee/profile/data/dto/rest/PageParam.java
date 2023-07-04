package com.raxim.myscoutee.profile.data.dto.rest;

public class PageParam {
    private String[] offset;

    private int direction = 1;
    private String step;

    public PageParam() {
    }

    public PageParam(String[] offset, int direction, String step) {
        this.offset = offset;
        this.direction = direction;
        this.step = step;
    }

    public String[] getOffset() {
        return offset;
    }

    public void setOffset(String[] offset) {
        this.offset = offset;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }
}

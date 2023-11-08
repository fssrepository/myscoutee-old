package com.raxim.myscoutee.profile.data.dto.rest;

import java.util.UUID;

public class PageParam implements Cloneable {
    private Object[] offset;
    private String type;
    private int direction;

    // basket
    private String selectedId;

    private int limit = 20;
    private int step = 5;

    private String groupKey;

    private UUID id;

    public PageParam() {
    }

    public PageParam(Object[] offset) {
        this(offset, null);
    }

    public PageParam(Object[] offset, String type) {
        this(offset, type, 0);
    }

    public PageParam(Object[] offset, String type, int direction) {
        this.offset = offset;
        this.direction = direction;
        this.type = type;
    }

    public Object[] getOffset() {
        return offset;
    }

    public void setOffset(Object[] offset) {
        this.offset = offset;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public String getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(String selectedId) {
        this.selectedId = selectedId;
    }

    @Override
    public Object clone()
            throws CloneNotSupportedException {
        return super.clone();
    }

}

package com.raxim.myscoutee.profile.data.document.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Matrix {
    @JsonProperty(value = "scale")
    private Double scale;

    @JsonProperty(value = "angle")
    private Double angle;

    @JsonProperty(value = "pos")
    private Position pos;

    public Matrix() {
        this.scale = 1.0;
        this.angle = 0.0;
        this.pos = new Position();
    }

    public Double getScale() {
        return scale;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

    public Double getAngle() {
        return angle;
    }

    public void setAngle(Double angle) {
        this.angle = angle;
    }

    public Position getPos() {
        return pos;
    }

    public void setPos(Position pos) {
        this.pos = pos;
    }
}
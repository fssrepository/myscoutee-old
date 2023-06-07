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
    }

    public Matrix(Double scale, Double angle, Position pos) {
        this.scale = scale;
        this.angle = angle;
        this.pos = pos;
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

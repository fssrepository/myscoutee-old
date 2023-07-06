package com.raxim.myscoutee.profile.data.document.mongo;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RangeLocal {
    @JsonProperty(value = "start")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime start;

    @JsonProperty(value = "end")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime end;

    public static RangeLocal of(LocalDateTime start, LocalDateTime end) {
        RangeLocal rangeLocal = new RangeLocal();
        rangeLocal.setStart(start);
        rangeLocal.setEnd(end);
        return rangeLocal;
    }

    public RangeLocal() {
    }

    public RangeLocal(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }
}

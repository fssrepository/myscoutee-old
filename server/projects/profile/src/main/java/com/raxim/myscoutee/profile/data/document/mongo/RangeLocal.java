package com.raxim.myscoutee.profile.data.document.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

public class RangeLocal {
    @JsonProperty(value = "start")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime start;
    
    @JsonProperty(value = "end")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime end;
    
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

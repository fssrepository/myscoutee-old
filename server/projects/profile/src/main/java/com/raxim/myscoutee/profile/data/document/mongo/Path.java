package com.raxim.myscoutee.profile.data.document.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "paths")
public class Path {
    @Id
    @JsonProperty(value = "id")
    private UUID id;

    @JsonProperty(value = "from")
    private UUID from;

    @JsonProperty(value = "to")
    private UUID to;

    @JsonIgnore
    private UUID ref;

    @JsonProperty(value = "distance")
    private Long distance;

    public Path() {
        this.id = UUID.randomUUID();
    }

    public Path(UUID from, UUID to, UUID ref, Long distance) {
        this.id = UUID.randomUUID();
        this.from = from;
        this.to = to;
        this.ref = ref;
        this.distance = distance;
    }

    public UUID getId() {
        return id;
    }

    public UUID getFrom() {
        return from;
    }

    public void setFrom(UUID from) {
        this.from = from;
    }

    public UUID getTo() {
        return to;
    }

    public void setTo(UUID to) {
        this.to = to;
    }

    public UUID getRef() {
        return ref;
    }

    public void setRef(UUID ref) {
        this.ref = ref;
    }

    public Long getDistance() {
        return distance;
    }

    public void setDistance(Long distance) {
        this.distance = distance;
    }
}

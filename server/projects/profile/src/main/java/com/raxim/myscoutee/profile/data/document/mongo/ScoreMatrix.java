package com.raxim.myscoutee.profile.data.document.mongo;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "score_matrix")
public class ScoreMatrix {
    @Id
    @JsonProperty(value = "key")
    private UUID id;

    @JsonProperty(value = "name") // fifa
    private String name;

    public String getName() {
        return name;
    }

    // (W)Win, (D)Draw, YD (Yellow Card),
    // IR (Indirect Red card - second yellow card), DR (Red card),
    // YDR (yellow card with direct red card)
    // GF (goal for), GA (goal against)
    @JsonProperty(value = "type")
    private String type;

    @JsonProperty(value = "score")
    private Double score = 0d;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}

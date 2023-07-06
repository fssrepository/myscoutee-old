package com.raxim.myscoutee.common.data;

import java.util.UUID;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.raxim.myscoutee.common.repository.GeoJsonPointDeserializer;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;

public class TestProfile extends Profile {
    @JsonDeserialize(using = GeoJsonPointDeserializer.class)
    public void setPosition(GeoJsonPoint position) {
        super.setPosition(position);
    }

    @Override
    @JsonProperty("group")
    public void setGroup(UUID group) {
        super.setGroup(group);
    }
}

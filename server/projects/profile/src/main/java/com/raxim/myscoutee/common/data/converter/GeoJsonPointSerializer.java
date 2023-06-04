package com.raxim.myscoutee.common.data.converter;

import java.io.IOException;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class GeoJsonPointSerializer extends JsonSerializer<GeoJsonPoint> {

    @Override
    public void serialize(GeoJsonPoint value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("type", value.getType());
        gen.writeArrayFieldStart("coordinates");
        gen.writeObject(value.getCoordinates());
        gen.writeEndArray();
        gen.writeEndObject();
    }
}
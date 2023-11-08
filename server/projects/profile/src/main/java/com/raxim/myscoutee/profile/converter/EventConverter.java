package com.raxim.myscoutee.profile.converter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;

@Component
public class EventConverter extends BaseConverter<Event, EventDTO> {

    @Override
    public EventDTO convert(Event event) {
        if (!"I".equals(event.getType())) {
            LocalDateTime eventStart = event.getRange().getStart();
            LocalDate groupKey = eventStart != null ? eventStart.toLocalDate() : null;
            Long sortKey = eventStart != null ? eventStart.toInstant(ZoneOffset.UTC).toEpochMilli() : null;
            return new EventDTO(event, groupKey, sortKey);
        } else {
            String groupKey = event.getType();
            return new EventDTO(event, groupKey, null);
        }
    }

}

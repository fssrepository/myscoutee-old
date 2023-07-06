package com.raxim.myscoutee.profile.converter;

import org.springframework.stereotype.Component;

import com.raxim.myscoutee.profile.data.document.mongo.EventItem;
import com.raxim.myscoutee.profile.data.dto.rest.EventItemDTO;

@Component
public class EventItemConverter extends BaseConverter<EventItem, EventItemDTO> {

    @Override
    public EventItemDTO convert(EventItem source) {
        return new EventItemDTO(source, source.getType());
    }

}

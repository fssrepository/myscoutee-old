package com.raxim.myscoutee.profile.util;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.EventItem;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventItemDTO;
import com.raxim.myscoutee.profile.service.EventService;

public class EventItemUtil {
    //TODO: to be fixed
    /*public static ResponseEntity<EventDTO> save(EventService eventService, EventItem eventItem, Profile profile,
            boolean isTemplate) {
        String status = isTemplate ? "T" : "A";

        Optional<Event> event = eventService.getEvent(eventItem, profile, status);
        if (event.isPresent()) {
            Optional<Pair<Event, EventItem>> savedEvent = eventService.saveEvent(event.get(), eventItem);
            if (savedEvent.isPresent()) {
                EventDTO eventDto = EventUtil.transform(savedEvent.get().getFirst());
                return new ResponseEntity<>(eventDto, HttpStatus.CREATED);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }*/

    //TODO: to be fixed
    /*public static ResponseEntity<?> update(EventService eventService, EventItem eventItem, String id, Profile profile) {
        Optional<Event> event = eventService.getEvent(eventItem, profile, "A", CommonUtil.parseUUID(id), true);
        if (event.isPresent()) {
            Optional<Pair<Event, EventItem>> savedEvent = eventService.saveEvent(event.get(), eventItem);
            if (savedEvent.isPresent()) {
                EventDTO eventDto = EventUtil.transform(savedEvent.get().getFirst());
                return new ResponseEntity<>(eventDto, HttpStatus.CREATED);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }*/

    //TODO: to be fixed
    /*public static ResponseEntity<EventItemDTO> update(EventService eventService, EventItem eventItem, String id,
            String itemId,
            Profile profile) {
        Optional<Event> event = eventService.getEvent(eventItem, profile, "A", CommonUtil.parseUUID(id), true);
        if (event.isPresent()) {
            Optional<Pair<Event, EventItem>> savedEvent = eventService.saveEvent(event.get(), eventItem);
            if (savedEvent.isPresent()) {
                EventItemDTO eventItemDto = transform(savedEvent.get().getSecond());
                return new ResponseEntity<>(eventItemDto, HttpStatus.CREATED);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }*/

    //TODO: to be fixed
    /*public static ResponseEntity<EventItemDTO> save(EventService eventService, EventItem eventItem, String id,
            Profile profile) {
        Optional<Event> event = eventService.getEvent(eventItem, profile, "A", CommonUtil.parseUUID(id), false);
        if (event.isPresent()) {
            Optional<Pair<Event, EventItem>> savedEvent = eventService.saveEvent(event.get(), eventItem);
            if (savedEvent.isPresent()) {
                EventItemDTO eventItemDto = transform(savedEvent.get().getSecond());
                return new ResponseEntity<>(eventItemDto, HttpStatus.CREATED);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.badRequest().build();
        }
    }*/
}

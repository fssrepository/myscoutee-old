package com.raxim.myscoutee.data.mongo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.EventItem;
import com.raxim.myscoutee.profile.data.document.mongo.RangeInt;
import com.raxim.myscoutee.profile.data.document.mongo.RangeLocal;

public class EventTest {

    @Test
    public void shouldSync() {
        Event event = new Event();
        event.setCapacity(RangeInt.of(2, 6));

        LocalDateTime eventStart = LocalDateTime.of(2020, 2, 1, 0, 0, 0);
        LocalDateTime eventEnd = LocalDateTime.of(2020, 2, 1, 0, 0, 0);
        event.setRange(RangeLocal.of(eventStart, eventEnd));

        Set<EventItem> items = new HashSet<>();
        EventItem eventItem = new EventItem();
        eventItem.setCapacity(RangeInt.of(3, 8));

        LocalDateTime itemStart = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
        LocalDateTime itemEnd = LocalDateTime.of(2021, 1, 1, 0, 0, 0);
        eventItem.setRange(RangeLocal.of(itemStart, itemEnd));
        items.add(eventItem);
        event.setItems(items);

        event.sync();

        assertEquals(8, event.getCapacity().getMax());
        assertEquals(itemStart, event.getRange().getStart());
        assertEquals(itemEnd, event.getRange().getEnd());
    }

    @Test
    public void shouldShiftAndSync() {
        Event event = new Event();
        event.setCapacity(RangeInt.of(2, 6));

        LocalDateTime eventStart = LocalDateTime.of(2020, 2, 1, 0, 0, 0);
        LocalDateTime eventEnd = LocalDateTime.of(2020, 3, 1, 0, 0, 0);
        event.setRange(RangeLocal.of(eventStart, eventEnd));

        Set<EventItem> items = new HashSet<>();
        EventItem eventItem = new EventItem();
        eventItem.setCapacity(RangeInt.of(3, 8));

        LocalDateTime itemStart = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
        LocalDateTime itemEnd = LocalDateTime.of(2021, 1, 1, 0, 0, 0);
        eventItem.setRange(RangeLocal.of(itemStart, itemEnd));
        items.add(eventItem);
        event.setItems(items);

        event.shift();
        event.sync();

        assertEquals(8, event.getCapacity().getMax());
        assertEquals(eventStart, event.getRange().getStart());

        LocalDateTime expectedEventEnd = LocalDateTime.of(2021, 2, 1, 0, 0, 0);
        assertEquals(expectedEventEnd, event.getRange().getEnd());
    }
}

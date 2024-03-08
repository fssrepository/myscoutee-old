package com.raxim.myscoutee.data.mongo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.raxim.myscoutee.algo.dto.Range;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.RangeLocal;

public class EventTest {

    @Test
    public void shouldSync() {
        Event event = new Event();
        event.setCapacity(Range.of(2, 6));

        LocalDateTime eventStart = LocalDateTime.of(2020, 2, 1, 0, 0, 0);
        LocalDateTime eventEnd = LocalDateTime.of(2020, 2, 1, 0, 0, 0);
        event.setRange(RangeLocal.of(eventStart, eventEnd));

        List<Event> items = new ArrayList<>();
        Event eventItem = new Event();
        eventItem.setCapacity(Range.of(3, 8));

        LocalDateTime itemStart = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
        LocalDateTime itemEnd = LocalDateTime.of(2021, 1, 1, 0, 0, 0);
        eventItem.setRange(RangeLocal.of(itemStart, itemEnd));
        items.add(eventItem);
        event.setItems(items);

        event.sync();

        assertEquals(6, event.getCapacity().getMax());
        assertEquals(6, event.getItems().get(0).getCapacity().getMax());
        assertEquals(itemStart, event.getRange().getStart());
        assertEquals(itemEnd, event.getRange().getEnd());
    }

    @Test
    public void shouldShiftAndSync() {
        Event event = new Event();
        event.setCapacity(Range.of(2, 6));

        LocalDateTime eventStart = LocalDateTime.of(2020, 2, 1, 0, 0, 0);
        LocalDateTime eventEnd = LocalDateTime.of(2020, 3, 1, 0, 0, 0);
        event.setRange(RangeLocal.of(eventStart, eventEnd));

        List<Event> items = new ArrayList<>();
        Event eventItem = new Event();
        eventItem.setCapacity(Range.of(3, 8));

        LocalDateTime itemStart = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
        LocalDateTime itemEnd = LocalDateTime.of(2021, 1, 1, 0, 0, 0);
        eventItem.setRange(RangeLocal.of(itemStart, itemEnd));
        items.add(eventItem);
        event.setItems(items);

        event.shift();
        event.sync();

        assertEquals(6, event.getCapacity().getMax());
        assertEquals(6, event.getItems().get(0).getCapacity().getMax());
        assertEquals(eventStart, event.getRange().getStart());

        LocalDateTime expectedEventEnd = LocalDateTime.of(2021, 2, 1, 0, 0, 0);
        assertEquals(expectedEventEnd, event.getRange().getEnd());
    }

    @Test
    public void shouldSyncSlotIdx() {
        Event event = new Event();
        event.setMultislot(true);

        List<Event> items = new ArrayList<>();

        Event eventItem = new Event();
        LocalDateTime itemStart = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
        LocalDateTime itemEnd = LocalDateTime.of(2021, 1, 1, 0, 0, 0);
        eventItem.setRange(RangeLocal.of(itemStart, itemEnd));
        items.add(eventItem);

        eventItem = new Event();
        itemStart = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
        itemEnd = LocalDateTime.of(2021, 1, 1, 0, 0, 0);
        eventItem.setRange(RangeLocal.of(itemStart, itemEnd));
        items.add(eventItem);

        event.setItems(items);

        event.sync();

        assertEquals(1, event.getItems().get(0).getSlotIdx());
        assertEquals(2, event.getItems().get(1).getSlotIdx());
    }
}

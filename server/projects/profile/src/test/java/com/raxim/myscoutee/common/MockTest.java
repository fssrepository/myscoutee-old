package com.raxim.myscoutee.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.raxim.myscoutee.profile.converter.Converters;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;

@ExtendWith(MockitoExtension.class)
public class MockTest {

    @Mock
    private Converters converters;

    @Test
    public void shouldGetConverters() {
        Event event = new Event();
        event.setStatus("Z");

        when(converters.convert(any())).thenReturn(Optional.of(new EventDTO(event)));

        Optional<EventDTO> eventDTO = converters.convert(event).map(ev -> (EventDTO)ev);
        assertEquals("Z", eventDTO.get().getEvent().getStatus());
    }
}

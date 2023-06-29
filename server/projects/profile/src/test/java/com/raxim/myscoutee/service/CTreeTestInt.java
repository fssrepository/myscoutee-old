package com.raxim.myscoutee.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.common.config.JsonConfig;
import com.raxim.myscoutee.data.mongo.TestEventItem;
import com.raxim.myscoutee.profile.data.document.mongo.EventItem;
import com.raxim.myscoutee.util.TestJsonUtil;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(classes = JsonConfig.class)
public class CTreeTestInt {
        @Autowired
    private ObjectMapper objectMapper;

    // pass already existing group and filter -> algo/eventItems.json
    // objectMapper.addMixIn(EventItem.class, TestEventItem.class);
    @Test
    public void shouldFilteredGroup() throws IOException {
        objectMapper.addMixIn(EventItem.class, TestEventItem.class);

        EventItem[] eventItemArray = TestJsonUtil.loadJson(this, "rest/eventItems.json",
                EventItem[].class,
                objectMapper);
        List<EventItem> eventItems = Arrays.asList(eventItemArray);
        
    }
}

package com.raxim.myscoutee.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import com.raxim.myscoutee.profile.service.EventScheduler;

@Disabled
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
public class EventSchedulerTest {

    @Autowired
    private EventScheduler eventScheduler;

    @Test
    public void generateEvent() throws Exception {
        this.eventScheduler.generateRandomEvents();
    }
}

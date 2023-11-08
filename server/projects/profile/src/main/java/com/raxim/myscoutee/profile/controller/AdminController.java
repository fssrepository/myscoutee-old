package com.raxim.myscoutee.profile.controller;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raxim.myscoutee.profile.service.EventScheduler;

@RestController
@RequestMapping("admin")
public class AdminController {

    private final EventScheduler eventScheduler;

    public AdminController(EventScheduler eventScheduler) {
        this.eventScheduler = eventScheduler;
    }

    @GetMapping("random")
    @Transactional
    public void getEvents() {
        this.eventScheduler.autoGenerateRooms();
    }
}

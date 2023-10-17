package com.raxim.myscoutee.profile.controller;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;
import com.raxim.myscoutee.profile.service.EventScheduler;

@RepositoryRestController
@RequestMapping("calc")
public class CalcRestController {

    private final EventScheduler eventScheduler;
    private final UserRepository userRepository;

    public CalcRestController(EventScheduler eventScheduler, UserRepository userRepository) {
        this.eventScheduler = eventScheduler;
        this.userRepository = userRepository;
    }

    @GetMapping("/priority_none/{lastTime}")
    public ResponseEntity<Void> calcPriorityNone(@PathVariable String lastTime) {
        try {
            eventScheduler.generateRandomEvents();
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/messaging/{lastTime}")
    public ResponseEntity<Void> registerMessaging(@PathVariable String lastTime) {
        try {
            eventScheduler.topicRegistration(lastTime);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().build();
    }
}

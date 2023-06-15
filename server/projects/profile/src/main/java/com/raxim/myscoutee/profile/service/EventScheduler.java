package com.raxim.myscoutee.profile.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Notification;
import com.raxim.myscoutee.common.config.firebase.FirebaseAuthenticationToken;
import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.ScheduleSetting;
import com.raxim.myscoutee.profile.exception.InvalidScheduleSettingsException;
import com.raxim.myscoutee.algo.dto.Bound;
import com.raxim.myscoutee.profile.repository.mongo.ScheduleSettingRepository;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class EventScheduler {

    private final UserRepository userRepository;
    private final EventGeneratorService eventGeneratorService;
    private final ScheduleSettingRepository scheduleSettingRepository;
    private final EventService eventService;
    private final ObjectMapper objectMapper;

    public EventScheduler(
            UserRepository userRepository,
            EventGeneratorService eventGeneratorService,
            ScheduleSettingRepository scheduleSettingRepository,
            EventService eventService,
            ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.eventGeneratorService = eventGeneratorService;
        this.scheduleSettingRepository = scheduleSettingRepository;
        this.eventService = eventService;
        this.objectMapper = objectMapper;
    }

    @Scheduled(cron = "0 0 3 * * MON")
    public void autoGenerateRooms() {
        try {
            generateEvents();
        } catch (FirebaseMessagingException | InvalidScheduleSettingsException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 2 * * MON")
    public void autoTopicRegistration() {
        try {
            topicRegistration("1900-01-01");
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    // greater than profileUpdated field!
    public void topicRegistration(String lastTime) throws FirebaseMessagingException {
        Authentication auth = new FirebaseAuthenticationToken("scheduler", "");
        SecurityContextHolder.getContext().setAuthentication(auth);

        userRepository.findDeviceWithProfileStatusAll(lastTime, new String[]{"A"})
                .forEach(topic -> {
                    String topicName = topic.getName().replace(" ", "_");
                    try {
                        FirebaseMessaging.getInstance().subscribeToTopic(topic.getTokens(), topicName);
                    } catch (FirebaseMessagingException e) {
                        e.printStackTrace();
                    }
                    System.out.println(topic.getTokens().size() + " tokens were subscribed successfully from " + topic.getName());
                });

        userRepository.findDeviceWithProfileStatusAll(lastTime, new String[]{"I", "F", "S", "P"})
                .forEach(topic -> {
                    String topicName = topic.getName().replace(" ", "_");
                    try {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic.getTokens(), topicName);
                    } catch (FirebaseMessagingException e) {
                        e.printStackTrace();
                    }
                    System.out.println(topic.getTokens().size() + " tokens were unsubscribed successfully from " + topic.getName());
                });

        // only test!!!
        com.google.firebase.messaging.Message message = com.google.firebase.messaging.Message.builder()
                .setNotification(
                        Notification.builder()
                                .setTitle("Meet with Strangers - Notification has been activated!")
                                .build()
                )
                .setTopic("Dating")
                .build();

        FirebaseMessaging.getInstance().send(message);
        System.out.println("Successfully sent message");

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    public void generateEvents() throws InvalidScheduleSettingsException, FirebaseMessagingException {
        Authentication auth = new FirebaseAuthenticationToken("scheduler", "");
        SecurityContextHolder.getContext().setAuthentication(auth);

        ScheduleSetting scheduleSetting = scheduleSettingRepository.findByKey(EventGeneratorService.RANDOM_GROUP)
                .orElseThrow(InvalidScheduleSettingsException::new);

        Bound bound = JsonUtil.jsonToObject(scheduleSetting.getFlags(), Bound.class, objectMapper);

        List<Set<Profile>> profilesByGroup = eventGeneratorService.generate(bound);
        eventService.saveEvents(profilesByGroup);

        com.google.firebase.messaging.Message message = com.google.firebase.messaging.Message.builder()
                .setNotification(
                        Notification.builder()
                                .setTitle("Rooms based on your priority has been created!")
                                .setBody("Rooms based on your priority has been created!")
                                .build()
                )
                .setTopic("Dating")
                .build();

        FirebaseMessaging.getInstance().send(message);
        System.out.println("Successfully sent message");

        SecurityContextHolder.getContext().setAuthentication(null);
    }
}

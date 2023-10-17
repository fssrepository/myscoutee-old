package com.raxim.myscoutee.profile.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Notification;
import com.raxim.myscoutee.common.config.firebase.FirebaseAuthenticationToken;
import com.raxim.myscoutee.profile.data.document.mongo.Schedule;
import com.raxim.myscoutee.profile.data.dto.FilteredEdges;
import com.raxim.myscoutee.profile.repository.mongo.ScheduleRepository;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;
import com.raxim.myscoutee.profile.util.AppConstants;

@Service
public class EventScheduler {

    private final UserRepository userRepository;
    private final EventGeneratorRandomService eventGeneratorRandomService;
    private final EventGeneratorByPriorityService eventGeneratorByPriorityService;
    private final EventGeneratorByScoreService eventGeneratorByScoreService;
    private final LikeService likeService;
    private final ScheduleRepository scheduleRepository;

    public EventScheduler(
            UserRepository userRepository,
            EventGeneratorRandomService eventGeneratorService,
            EventGeneratorByPriorityService eventGeneratorByPriorityService,
            EventGeneratorByScoreService eventGeneratorByScoreService,
            LikeService likeService, ScheduleRepository scheduleRepository) {
        this.userRepository = userRepository;
        this.eventGeneratorRandomService = eventGeneratorService;
        this.eventGeneratorByPriorityService = eventGeneratorByPriorityService;
        this.eventGeneratorByScoreService = eventGeneratorByScoreService;
        this.likeService = likeService;
        this.scheduleRepository = scheduleRepository;
    }

    @Scheduled(cron = "0 0 3 * * MON")
    public void autoGenerateRooms() {
        try {
            generateRandomEvents();
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 3 * * MON")
    public void autoPriorityRooms() {
        try {
            generateByPriorityEvents();
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 3 * * MON")
    public void autoScoreRooms() {
        try {
            generateByScoreEvents();
        } catch (FirebaseMessagingException e) {
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

        userRepository.findDeviceWithProfileStatusAll(lastTime, new String[] { "A" })
                .forEach(topic -> {
                    String topicName = topic.getName().replace(" ", "_");
                    try {
                        FirebaseMessaging.getInstance().subscribeToTopic(topic.getTokens(), topicName);
                    } catch (FirebaseMessagingException e) {
                        e.printStackTrace();
                    }
                    System.out.println(
                            topic.getTokens().size() + " tokens were subscribed successfully from " + topic.getName());
                });

        userRepository.findDeviceWithProfileStatusAll(lastTime, new String[] { "I", "F", "S", "P" })
                .forEach(topic -> {
                    String topicName = topic.getName().replace(" ", "_");
                    try {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic.getTokens(), topicName);
                    } catch (FirebaseMessagingException e) {
                        e.printStackTrace();
                    }
                    System.out.println(topic.getTokens().size() + " tokens were unsubscribed successfully from "
                            + topic.getName());
                });

        // only test!!!
        com.google.firebase.messaging.Message message = com.google.firebase.messaging.Message.builder()
                .setNotification(
                        Notification.builder()
                                .setTitle("Meet with Strangers - Notification has been activated!")
                                .build())
                .setTopic("Dating")
                .build();

        FirebaseMessaging.getInstance().send(message);
        System.out.println("Successfully sent message");

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    public void generateRandomEvents() throws FirebaseMessagingException {
        Authentication auth = new FirebaseAuthenticationToken("scheduler", "");
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<Schedule> schedule = scheduleRepository.findByKey(AppConstants.SCHEDULE_RANDOM_GROUP);
        long lastIdx = schedule.map(Schedule::getLastIdx).orElse(0L);
        long batchSize = schedule.map(Schedule::getBatchSize).orElse(1000L);

        String flags = schedule.map(Schedule::getFlags).orElse(null);

        FilteredEdges filteredEdges = likeService.getEdges(Set.of("A"));

        eventGeneratorRandomService.generate(filteredEdges, flags);

        sendRandomEventNotification();

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    public void generateByPriorityEvents() throws FirebaseMessagingException {
        Authentication auth = new FirebaseAuthenticationToken("scheduler", "");
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<Schedule> schedule = scheduleRepository.findByKey(AppConstants.SCHEDULE_RANDOM_GROUP);
        long lastIdx = schedule.map(Schedule::getLastIdx).orElse(0L);
        long batchSize = schedule.map(Schedule::getBatchSize).orElse(1000L);

        String flags = schedule.map(Schedule::getFlags).orElse(null);

        FilteredEdges filteredEdges = likeService.getEdges(Set.of("A", "F"));

        eventGeneratorByPriorityService.generate(filteredEdges, flags);

        sendRandomEventNotification();

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    public void generateByScoreEvents() throws FirebaseMessagingException {
        Authentication auth = new FirebaseAuthenticationToken("scheduler", "");
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<Schedule> schedule = scheduleRepository.findByKey(AppConstants.SCHEDULE_RANDOM_GROUP);
        long lastIdx = schedule.map(Schedule::getLastIdx).orElse(0L);
        long batchSize = schedule.map(Schedule::getBatchSize).orElse(1000L);

        String flags = schedule.map(Schedule::getFlags).orElse(null);

        FilteredEdges filteredEdges = likeService.getEdges(Set.of("A", "F"));

        eventGeneratorByScoreService.generate(filteredEdges, flags);

        sendRandomEventNotification();

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    private void sendRandomEventNotification() throws FirebaseMessagingException {
        com.google.firebase.messaging.Message message = com.google.firebase.messaging.Message.builder()
                .setNotification(
                        Notification.builder()
                                .setTitle("Rooms based on your priority has been created!")
                                .setBody("Rooms based on your priority has been created!")
                                .build())
                .setTopic("Dating")
                .build();

        FirebaseMessaging.getInstance().send(message);
        System.out.println("Successfully sent message");
    }
}

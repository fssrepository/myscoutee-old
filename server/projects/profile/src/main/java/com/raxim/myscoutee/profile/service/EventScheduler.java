package com.raxim.myscoutee.profile.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Notification;
import com.raxim.myscoutee.common.config.firebase.FirebaseAuthenticationToken;
import com.raxim.myscoutee.profile.data.document.mongo.DBMessage;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Schedule;
import com.raxim.myscoutee.profile.repository.mongo.MessageRepository;
import com.raxim.myscoutee.profile.repository.mongo.ScheduleRepository;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;
import com.raxim.myscoutee.profile.util.AppConstants;

@Service
public class EventScheduler {

    private final UserRepository userRepository;
    private final EventGeneratorRandomService eventGeneratorRandomService;
    private final EventGeneratorByPriorityService eventGeneratorByPriorityService;
    private final EventGeneratorByScoreService eventGeneratorByScoreService;
    private final ScheduleRepository scheduleRepository;
    private final MessageRepository messageRepository;

    public EventScheduler(
            UserRepository userRepository,
            EventGeneratorRandomService eventGeneratorService,
            EventGeneratorByPriorityService eventGeneratorByPriorityService,
            EventGeneratorByScoreService eventGeneratorByScoreService,
            LikeService likeService, ScheduleRepository scheduleRepository,
            MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.eventGeneratorRandomService = eventGeneratorService;
        this.eventGeneratorByPriorityService = eventGeneratorByPriorityService;
        this.eventGeneratorByScoreService = eventGeneratorByScoreService;
        this.scheduleRepository = scheduleRepository;
        this.messageRepository = messageRepository;
    }

    @Scheduled(cron = "0 0 3 * * MON")
    // @Scheduled(cron = "*/10 * * * * *")
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
        System.out.println("Generate Random Event Start!");

        Authentication auth = new FirebaseAuthenticationToken("scheduler", "");
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<Schedule> schedule = scheduleRepository.findByKey(AppConstants.SCHEDULE_RANDOM_GROUP);
        long lastIdx = schedule.map(Schedule::getLastIdx).orElse(0L);
        long batchSize = schedule.map(Schedule::getBatchSize).orElse(1000L);

        String flags = schedule.map(Schedule::getFlags).orElse(null);

        List<Event> genEvents = eventGeneratorRandomService.generate(flags);

        // generate messages and save with system, hence the user can see the channels!
        // on accept invitation it is the same
        List<DBMessage> dbMessages = genEvents.stream().map(event -> {
            DBMessage dbMessage = new DBMessage();
            dbMessage.setId(UUID.randomUUID());
            dbMessage.setEventId(event.getId());
            dbMessage.setType(AppConstants.MQTT_MSG);

            List<UUID> profileIds = event.getMembers().stream().map(member -> member.getProfile().getId()).toList();
            dbMessage.setTos(profileIds);
            dbMessage.setCreatedDate(LocalDateTime.now());
            return dbMessage;
        }).toList();

        this.messageRepository.saveAll(dbMessages);

        sendRandomEventNotification();

        SecurityContextHolder.getContext().setAuthentication(null);

        System.out.println("Generate Random Event End");
    }

    public void generateByPriorityEvents() throws FirebaseMessagingException {
        Authentication auth = new FirebaseAuthenticationToken("scheduler", "");
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<Schedule> schedule = scheduleRepository.findByKey(AppConstants.SCHEDULE_RANDOM_GROUP);
        long lastIdx = schedule.map(Schedule::getLastIdx).orElse(0L);
        long batchSize = schedule.map(Schedule::getBatchSize).orElse(1000L);

        String flags = schedule.map(Schedule::getFlags).orElse(null);

        eventGeneratorByPriorityService.generate(flags);

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

        eventGeneratorByScoreService.generate(flags);

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

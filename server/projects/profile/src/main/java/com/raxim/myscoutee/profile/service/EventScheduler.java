package com.raxim.myscoutee.profile.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Notification;
import com.raxim.myscoutee.common.config.firebase.FirebaseAuthenticationToken;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;

@Service
public class EventScheduler {

    private final UserRepository userRepository;
    private final EventGeneratorService eventGeneratorService;

    public EventScheduler(
            UserRepository userRepository,
            EventGeneratorService eventGeneratorService) {
        this.userRepository = userRepository;
        this.eventGeneratorService = eventGeneratorService;
    }

    @Scheduled(cron = "0 0 3 * * MON")
    public void autoGenerateRooms() {
        try {
            generateEvents();
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

    public void generateEvents() throws FirebaseMessagingException {
        Authentication auth = new FirebaseAuthenticationToken("scheduler", "");
        SecurityContextHolder.getContext().setAuthentication(auth);

        eventGeneratorService.generate();

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

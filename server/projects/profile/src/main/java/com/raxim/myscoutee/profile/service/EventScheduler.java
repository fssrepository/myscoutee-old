package com.raxim.myscoutee.profile.service;

import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.raxim.myscoutee.common.config.firebase.FirebaseAuthenticationToken;
import com.raxim.myscoutee.exception.InvalidScheduleSettingsException;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.ScheduleSetting;
import com.raxim.myscoutee.profile.data.dto.internal.BoundDto;
import com.raxim.myscoutee.profile.generator.GroupGenerator;
import com.raxim.myscoutee.profile.repository.mongo.ScheduleSettingRepository;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;

@Service
public class EventScheduler {
    private final UserRepository userRepository;
    private final GroupGenerator groupGenerator;
    private final ScheduleSettingRepository scheduleSettingRepository;
    private final EventService eventService;
    private final JsonConverterService jsonConverterService;

    public EventScheduler(UserRepository userRepository, GroupGenerator groupGenerator,
            ScheduleSettingRepository scheduleSettingRepository, EventService eventService,
            JsonConverterService jsonConverterService) {
        this.userRepository = userRepository;
        this.groupGenerator = groupGenerator;
        this.scheduleSettingRepository = scheduleSettingRepository;
        this.eventService = eventService;
        this.jsonConverterService = jsonConverterService;
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

    public void topicRegistration(String lastTime) throws FirebaseMessagingException {
        Authentication auth = new FirebaseAuthenticationToken("scheduler", "");
        SecurityContextHolder.getContext().setAuthentication(auth);

        userRepository.findDeviceWithProfileStatusAll(lastTime, new String[] { "A" }).forEach(topic -> {
            String topicName = topic.getName().replace(" ", "_");
            try {
                FirebaseMessaging.getInstance().subscribeToTopic(topic.getTokens(), topicName);
            } catch (FirebaseMessagingException e) {
                e.printStackTrace();
            }
            System.out.printf("%d tokens were subscribed successfully from %s%n",
                    topic.getTokens().size(), topic.getName());
        });

        userRepository.findDeviceWithProfileStatusAll(lastTime, new String[] { "I", "F", "S", "P" }).forEach(topic -> {
            String topicName = topic.getName().replace(" ", "_");
            try {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic.getTokens(), topicName);
            } catch (FirebaseMessagingException e) {
                e.printStackTrace();
            }
            System.out.printf("%d tokens were unsubscribed successfully from %s%n",
                    topic.getTokens().size(), topic.getName());
        });

        // Only for testing!
        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle("Meet with Strangers - Notification has been activated!")
                        .build())
                .setTopic("Dating")
                .build();

        String response = FirebaseMessaging.getInstance().send(message);
        System.out.println("Successfully sent message: " + response);

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    public void generateEvents() throws InvalidScheduleSettingsException, FirebaseMessagingException {
        Authentication auth = new FirebaseAuthenticationToken("scheduler", "");
        SecurityContextHolder.getContext().setAuthentication(auth);

        ScheduleSetting scheduleSetting = scheduleSettingRepository.findByKey(GroupGenerator.RANDOM_GROUP);

        if (scheduleSetting == null) {
            throw new InvalidScheduleSettingsException();
        }

        BoundDto bound = jsonConverterService.jsonToObject(scheduleSetting.getFlags(), BoundDto.class);

        if (bound == null) {
            throw new InvalidScheduleSettingsException();
        }

        List<Set<Profile>> profilesByGroup = groupGenerator.generate(bound);
        eventService.saveEvents(profilesByGroup);

        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle("Rooms based on your priority has been created!")
                        .setBody("Rooms based on your priority has been created!")
                        .build())
                .setTopic("Dating")
                .build();

        String response = FirebaseMessaging.getInstance().send(message);
        System.out.println("Successfully sent message: " + response);

        SecurityContextHolder.getContext().setAuthentication(null);
    }
}

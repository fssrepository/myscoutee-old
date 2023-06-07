package com.raxim.myscoutee.profile.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Notification
import com.raxim.myscoutee.common.config.firebase.FirebaseAuthenticationToken
import com.raxim.myscoutee.exception.InvalidScheduleSettingsException
import com.raxim.myscoutee.profile.data.document.mongo.ScheduleSetting
import com.raxim.myscoutee.profile.data.dto.internal.Bound
import com.raxim.myscoutee.profile.generator.GroupGenerator
import com.raxim.myscoutee.profile.repository.mongo.ScheduleSettingRepository
import com.raxim.myscoutee.profile.repository.mongo.UserRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class EventScheduler(
    val userRepository: UserRepository,
    val groupGenerator: GroupGenerator,
    val scheduleSettingRepository: ScheduleSettingRepository,
    val eventService: EventService,
    val jsonConverterService: JsonConverterService
) {
    @Scheduled(cron = "0 0 3 * * MON")
    fun autoGenerateRooms() {
        generateEvents()
    }

    @Scheduled(cron = "0 0 2 * * MON")
    fun autoTopicRegistration() {
        topicRegistration("1900-01-01")
    }

    //greater than profileUpdated field!
    fun topicRegistration(lastTime: String) {
        val auth: Authentication = FirebaseAuthenticationToken("scheduler", String())
        SecurityContextHolder.getContext().authentication = auth

        userRepository.findDeviceWithProfileStatusAll(lastTime, arrayOf("A"))?.forEach { topic ->
            val response = FirebaseMessaging.getInstance().subscribeToTopic(
                topic.tokens, topic.name!!.replace(" ", "_")
            )
            println("${response.successCount} tokens were subscribed successfully from from ${topic.name}");
        }

        userRepository.findDeviceWithProfileStatusAll(lastTime, arrayOf("I", "F", "S", "P"))?.forEach { topic ->
            val response = FirebaseMessaging.getInstance().unsubscribeFromTopic(
                topic.tokens, topic.name!!.replace(" ", "_")
            )
            println("${response.successCount} tokens were unsubscribed successfully from ${topic.name}");
        }

        // only test!!!
        val message: com.google.firebase.messaging.Message = com.google.firebase.messaging.Message.builder()
            .setNotification(
                Notification.builder()
                    .setTitle("Meet with Strangers - Notification has been activated!")
                    .build()
            )
            .setTopic("Dating")
            .build()

        val response = FirebaseMessaging.getInstance().send(message)
        println("Successfully sent message: $response")

        SecurityContextHolder.getContext().authentication = null
    }

    fun generateEvents() {

        //filter, who didn't met before
        val auth: Authentication = FirebaseAuthenticationToken("scheduler", String())
        SecurityContextHolder.getContext().authentication = auth

        val scheduleSetting = scheduleSettingRepository.findByKey(GroupGenerator.RANDOM_GROUP)

        if(scheduleSetting.isEmpty) {
            throw InvalidScheduleSettingsException();
        }

        val bound = jsonConverterService.jsonToObject(scheduleSetting.get().flags, Bound::class.java)!!

        val profilesByGroup = groupGenerator.generate(bound)
        eventService.saveEvents(profilesByGroup)

        val message: com.google.firebase.messaging.Message = com.google.firebase.messaging.Message.builder()
            .setNotification(
                Notification.builder()
                    .setTitle("Rooms based on your priority has been created!")
                    .setBody("Rooms based on your priority has been created!")
                    .build()
            )
            .setTopic("Dating")
            .build()

        val response = FirebaseMessaging.getInstance().send(message)
        println("Successfully sent message: $response")

        SecurityContextHolder.getContext().authentication = null
    }

}
package com.raxim.myscoutee.profile.service;

import java.util.List;
import java.util.UUID;

import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Notification;
import com.raxim.myscoutee.common.config.MqttConfig.MqttGateway;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Token;
import com.raxim.myscoutee.profile.data.dto.rest.MessageDTO;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.TokenRepository;

@Service
public class MqttMessageService {
    private final MqttGateway mqttGateway;
    private final TokenRepository tokenRepository;
    private final EventRepository eventRepository;

    public MqttMessageService(MqttGateway mqttGateway, TokenRepository tokenRepository,
            EventRepository eventRepository) {
        this.mqttGateway = mqttGateway;
        this.tokenRepository = tokenRepository;
        this.eventRepository = eventRepository;
    }

    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        System.out.println("Received message from topic: " + topic);

        MessageDTO messageDTO = (MessageDTO) message.getPayload();
        // the senderId should be added in the payload
        System.out.println("Received message: " + message.getPayload());

        UUID eventId = UUID.fromString(CommonUtil.getLastPartOfUrl(topic));

        //mqtt false
        //findTokensByEvent is not enough as you need to save the message to the db, and where to send with which type
        List<Token> tokens = this.eventRepository.findTokensByEvent(new UUID[] { eventId });

        if (!tokens.isEmpty()) {

            com.google.firebase.messaging.MulticastMessage fcmMessage = com.google.firebase.messaging.MulticastMessage
                    .builder()
                    .setNotification(
                            Notification.builder()
                                    .setTitle("Meet with Strangers - Notification has been activated!")
                                    .build())
                    .addAllTokens(null)
                    .build();

            ApiFuture<BatchResponse> future = FirebaseMessaging.getInstance().sendMulticastAsync(fcmMessage);
            ApiFutures.addCallback(future, new ApiFutureCallback<BatchResponse>() {
                @Override
                public void onSuccess(BatchResponse result) {
                    System.out.println("Operation completed with result: " + result);
                }

                @Override
                public void onFailure(Throwable t) {
                    System.out.println("Operation failed with error: " + t);
                }
            }, MoreExecutors.directExecutor());

            // it might need UUID to Base64 serialization
            mqttGateway.sendToMqtt("channels/profiles/" + messageDTO.getFrom(), "sent");
        }
    }
}

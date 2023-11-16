package com.raxim.myscoutee.profile.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Notification;
import com.raxim.myscoutee.common.config.MqttConfig.MqttGateway;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.DBMessage;
import com.raxim.myscoutee.profile.data.document.mongo.EventWithToken;
import com.raxim.myscoutee.profile.data.document.mongo.Token;
import com.raxim.myscoutee.profile.data.dto.rest.MessageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.MessageRepository;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;
import com.raxim.myscoutee.profile.util.AppConstants;

@Service
public class MessageService {
    private final MqttGateway mqttGateway;
    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final MessageRepository messageRepository;

    public MessageService(MqttGateway mqttGateway,
            EventRepository eventRepository, ObjectMapper objectMapper, MessageRepository messageRepository,
            UserRepository userRepository) {
        this.mqttGateway = mqttGateway;
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
        this.messageRepository = messageRepository;
    }

    public List<MessageDTO> getLastMessagesByChannels(PageParam pageParam) {
        return this.messageRepository.findLastMessageByChannels(pageParam);
    }

    public List<MessageDTO> getMessagesByChannel(UUID eventId, PageParam pageParam) {
        return this.messageRepository.findMessagesByChannel(eventId, pageParam);
    }

    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        System.out.println("Received message from topic: " + topic);

        MessageDTO messageDTO = (MessageDTO) message.getPayload();
        System.out.println("Received message: " + messageDTO);

        if (AppConstants.MQTT_WRITING.equals(messageDTO.getMessage().getType())) {
            System.out.println("Writing is handled by mosquitto!" + messageDTO.getMessage().getValue());
            return;
        }

        UUID eventId = UUID.fromString(CommonUtil.getPart(topic, "/", Integer.MAX_VALUE));

        Optional<EventWithToken> optEventWithToken = this.eventRepository.findTokensByEvent(eventId,
                messageDTO.getMessage().getFrom());

        // save message to the message table
        DBMessage dbMessage = saveMessage(optEventWithToken, messageDTO);

        // send message to participants
        // if it fails, it might need to check the db again and retry -> DBMessage has
        // no flag for it yet
        // filter out control messages
        if (!AppConstants.MQTT_CONTROL.contains(messageDTO.getMessage().getType())) {
            sendToMembers(optEventWithToken, messageDTO);

            // it might need UUID to Base64 serialization
            MessageDTO respMsgDTO = new MessageDTO();
            dbMessage.setType(AppConstants.MQTT_SENT);
            respMsgDTO.setMessage(dbMessage);

            sendToMqtt("channels/users/" + messageDTO.getFrom(), respMsgDTO);
        }
    }

    private DBMessage saveMessage(Optional<EventWithToken> optEventWithToken, MessageDTO messageDTO) {

        if (!optEventWithToken.isPresent()) {
            EventWithToken eventWithToken = optEventWithToken.get();

            DBMessage dbMessage = new DBMessage();
            dbMessage.setId(UUID.randomUUID());
            dbMessage.setEventUuid(eventWithToken.getId());
            dbMessage.setType(messageDTO.getMessage().getType());
            dbMessage.setFrom(messageDTO.getMessage().getFrom());
            dbMessage.setRef(messageDTO.getMessage().getRef());
            dbMessage.setCreatedDate(LocalDateTime.now());

            List<UUID> profileIds = eventWithToken.getProfiles().stream().map(profile -> profile.getId()).toList();
            dbMessage.setTos(profileIds);

            return this.messageRepository.save(dbMessage);
        }
        return null;
    }

    private void sendToMembers(Optional<EventWithToken> optEventWithToken, MessageDTO messageDTO) {

        if (!optEventWithToken.isPresent()) {
            EventWithToken eventWithToken = optEventWithToken.get();

            List<Token> fbTokens = eventWithToken.getTokens().stream()
                    .filter(token -> AppConstants.FIREBASE.equals(token.getType())).toList();

            List<String> fbKeys = fbTokens.stream().map(token -> token.getDeviceKey()).toList();

            com.google.firebase.messaging.MulticastMessage fcmMessage = com.google.firebase.messaging.MulticastMessage
                    .builder()
                    .setNotification(
                            Notification.builder()
                                    .setTitle(eventWithToken.getName())
                                    .setBody(messageDTO.getMessage().getValue())
                                    .build())
                    .addAllTokens(fbKeys)
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

            // mqtt will be handled by mosquitto
            /*
             * List<Token> mqttTokens = eventWithToken.getTokens().stream()
             * .filter(token -> AppConstants.MQTT.equals(token.getType())).toList();
             * 
             * List<String> mqttKeys = mqttTokens.stream().map(token ->
             * token.getUuid().toString()).toList();
             * 
             * for (String mqttKey : mqttKeys) {
             * sendToMqtt("channels/users/" + mqttKey, messageDTO);
             * }
             */
        }
    }

    // wrapper to use ObjectMapper
    public void sendToMqtt(String topic, Object data) {
        String dataStr = JsonUtil.toJson(data, objectMapper);
        mqttGateway.sendToMqtt(topic, dataStr);
    }
}

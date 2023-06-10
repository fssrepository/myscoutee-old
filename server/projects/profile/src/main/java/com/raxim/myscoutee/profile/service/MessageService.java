package com.raxim.myscoutee.profile.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.raxim.myscoutee.profile.data.document.mongo.Message;
import com.raxim.myscoutee.profile.repository.mongo.MessageRepository;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Cacheable(value = "messages", keyGenerator = "messageKeyGenerator")
    public Message getMessageByLang(String locale) {
        String lang = locale.split(",")[0].split("-")[0];
        Message message = messageRepository.findByLang(lang);

        // fallback
        if (message == null) {
            message = messageRepository.findByLang("en");
        }

        return message;
    }
}

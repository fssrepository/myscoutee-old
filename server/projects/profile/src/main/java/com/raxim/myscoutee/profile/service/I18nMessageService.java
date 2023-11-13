package com.raxim.myscoutee.profile.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.raxim.myscoutee.profile.data.document.mongo.I18nMessage;
import com.raxim.myscoutee.profile.repository.mongo.I18nMessageRepository;

@Service
public class I18nMessageService {

    private final I18nMessageRepository messageRepository;

    public I18nMessageService(I18nMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Cacheable(value = "messages", keyGenerator = "messageKeyGenerator")
    public I18nMessage getMessageByLang(String locale) {
        String lang = locale.split(",")[0].split("-")[0];
        I18nMessage message = messageRepository.findByLang(lang);

        // fallback
        if (message == null) {
            message = messageRepository.findByLang("en");
        }

        return message;
    }
}

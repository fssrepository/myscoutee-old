package com.raxim.myscoutee.profile.service

import com.raxim.myscoutee.profile.data.document.mongo.Message
import com.raxim.myscoutee.profile.repository.mongo.MessageRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class MessageService(
    private val messageRepository: MessageRepository
) {

    // rename message to dictionary
    @Cacheable("messages", keyGenerator = "messageKeyGenerator")
    fun getMessageByLang(locale: String): Message? {
        val lang = locale.split(",")[0].split("-")[0]
        var message = this.messageRepository.findByLang(lang)

        //fallback
        if (message == null) {
            message = this.messageRepository.findByLang("en")
        }

        return message;
    }
}
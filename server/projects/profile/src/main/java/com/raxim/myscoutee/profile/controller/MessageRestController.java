package com.raxim.myscoutee.profile.controller;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.raxim.myscoutee.profile.data.document.mongo.Message;
import com.raxim.myscoutee.profile.service.MessageService;

@RepositoryRestController("messages")
public class MessageRestController {

    private final MessageService messageService;

    public MessageRestController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("")
    public ResponseEntity<Message> getMsg(@RequestHeader("accept-language") String locale) {
        Message message = messageService.getMessageByLang(locale);
        return ResponseEntity.ok(message);
    }
}

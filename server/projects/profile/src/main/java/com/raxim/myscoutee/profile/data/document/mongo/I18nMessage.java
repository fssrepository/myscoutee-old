package com.raxim.myscoutee.profile.data.document.mongo;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "messages")
public class I18nMessage {
    @Id
    @JsonIgnore
    private UUID id;

    @JsonIgnore
    private String lang;

    @JsonProperty("msg")
    private Map<String, Object> msg;

    public UUID getId() {
        return id;
    }

    public String getLang() {
        return lang;
    }

    public Map<String, Object> getMsg() {
        return msg;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setMsg(Map<String, Object> msg) {
        this.msg = msg;
    }
}

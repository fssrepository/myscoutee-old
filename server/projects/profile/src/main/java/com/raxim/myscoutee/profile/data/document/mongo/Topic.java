package com.raxim.myscoutee.profile.data.document.mongo;

import java.util.Collections;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Topic {
    private String name;

    private List<String> tokens;

    public Topic() {
        this.tokens = Collections.emptyList();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }
}

package com.raxim.myscoutee.profile.data.document.mongo;

import java.util.List;

public class EventWithToken extends Event {

    private List<Token> tokens;

    public List<Token> getTokens() {
        return tokens;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }
}

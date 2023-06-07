package com.raxim.myscoutee.common.config.firebase.dto;

import java.util.List;
import java.util.Map;

import com.google.firebase.auth.FirebaseToken;

public class FirebaseCredential {
    private static final String FIREBASE = "firebase";
    private static final String IDENTITES = "identities";
    private static final String GOOGLE_COM = "google.com";

    private final FirebaseToken token;

    public FirebaseCredential(FirebaseToken token) {
        this.token = token;
    }

    public String getEmail() {
        return token.getEmail();
    }

    public String getIssuer() {
        return token.getIssuer();
    }

    public String getName() {
        return token.getName();
    }

    public String getUid() {
        return token.getUid();
    }

    public String getGoogleId() {
        Map<String, Object> firebaseClaim = (Map<String, Object>) token.getClaims().get(FIREBASE);
        Map<String, Object> identitiesMap = (Map<String, Object>) firebaseClaim.get(IDENTITES);
        List<String> identitiesGoogle = (List<String>) identitiesMap.get(GOOGLE_COM);
        return identitiesGoogle.get(0);
    }
}

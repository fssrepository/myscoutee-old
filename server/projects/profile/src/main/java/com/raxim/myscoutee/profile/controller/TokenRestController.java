package com.raxim.myscoutee.profile.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Token;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.repository.mongo.TokenRepository;

@RestController
@RequestMapping("tokens")
public class TokenRestController {

    private final TokenRepository tokenRepository;
    private final ObjectMapper objectMapper;

    public TokenRestController(TokenRepository tokenRepository, ObjectMapper objectMapper) {
        this.tokenRepository = tokenRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("")
    public ResponseEntity<Token> register(@RequestBody Token token, Authentication auth) {
        Token tokenObj = tokenRepository.findByDeviceId(token.getDeviceKey());

        if (tokenObj == null) {
            FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
            User user = principal.getUser();
            Token tokenToSave = JsonUtil.clone(token, objectMapper);
            tokenToSave.setId(user.getId());
            tokenObj = tokenRepository.save(tokenToSave);
        }

        return new ResponseEntity<>(tokenObj, HttpStatus.CREATED);
    }
}

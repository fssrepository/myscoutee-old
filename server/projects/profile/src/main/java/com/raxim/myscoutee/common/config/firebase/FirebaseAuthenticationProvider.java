package com.raxim.myscoutee.common.config.firebase

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.stereotype.Component

@Component
class FirebaseAuthenticationProvider : AuthenticationProvider {

    override fun supports(authentication: Class<*>?): Boolean {
        return FirebaseAuthenticationToken::class.java.isAssignableFrom(authentication)
    }

    @Throws(AuthenticationException::class)
    override fun authenticate(authentication: Authentication): Authentication? {
        if (!supports(authentication.javaClass)) {
            return null
        }
        return authentication as FirebaseAuthenticationToken
    }
}
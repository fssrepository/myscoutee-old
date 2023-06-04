package com.raxim.myscoutee.common.config.firebase

import org.springframework.security.authentication.BadCredentialsException


class FirebaseTokenInvalidException(msg: String?) : BadCredentialsException(msg) {
    companion object {
        private const val serialVersionUID = 789949671713648425L
    }
}
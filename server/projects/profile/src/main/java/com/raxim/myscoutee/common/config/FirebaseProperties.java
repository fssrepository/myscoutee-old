package com.raxim.myscoutee.common.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("fcm")
class FirebaseProperties {
    lateinit var url: String;
    lateinit var apiKey: String;
}
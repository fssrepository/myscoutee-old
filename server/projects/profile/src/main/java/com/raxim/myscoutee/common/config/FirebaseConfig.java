package com.raxim.myscoutee.common.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
class FirebaseConfig {

    @PostConstruct
    fun init() {

        val serviceAccount =
            FirebaseConfig::class.java.getResourceAsStream("/myscoutee-firebase-adminsdk-242c8-2aa0246e44.json")

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        FirebaseApp.initializeApp(options)

    }
}
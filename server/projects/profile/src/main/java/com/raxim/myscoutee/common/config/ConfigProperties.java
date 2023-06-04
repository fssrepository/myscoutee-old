package com.raxim.myscoutee.common.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("config")
class ConfigProperties {
    lateinit var adminUser: String;
}
package com.raxim.myscoutee.common.config;

import com.raxim.myscoutee.profile.repository.mongo.CarEventHandler;
import com.raxim.myscoutee.profile.repository.mongo.ProfileEventHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {

    @Bean
    public ProfileEventHandler profileEventHandler() {
        return new ProfileEventHandler();
    }

    @Bean
    public CarEventHandler carEventHandler() {
        return new CarEventHandler();
    }
}

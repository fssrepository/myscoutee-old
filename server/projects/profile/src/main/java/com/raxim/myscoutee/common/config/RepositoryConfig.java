package com.raxim.myscoutee.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.raxim.myscoutee.common.config.repository.MongoRepositoryFactoryBean;
import com.raxim.myscoutee.profile.repository.mongo.CarEventHandler;
import com.raxim.myscoutee.profile.repository.mongo.ProfileEventHandler;

@Configuration
@EnableMongoRepositories(repositoryFactoryBeanClass = MongoRepositoryFactoryBean.class, basePackages = "com.raxim.myscoutee.profile.repository.mongo")
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

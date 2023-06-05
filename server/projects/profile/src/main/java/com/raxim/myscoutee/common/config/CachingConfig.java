package com.raxim.myscoutee.common.config;

import com.raxim.myscoutee.profile.service.MessageKeyGenerator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CachingConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("messages");
    }

    @Bean("messageKeyGenerator")
    public KeyGenerator keyGenerator() {
        return new MessageKeyGenerator();
    }
}
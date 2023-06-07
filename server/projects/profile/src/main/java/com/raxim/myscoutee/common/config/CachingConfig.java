package com.raxim.myscoutee.common.config;

import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.raxim.myscoutee.common.config.caching.MessageKeyGenerator;

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

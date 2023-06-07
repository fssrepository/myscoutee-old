package com.raxim.myscoutee.common.config.caching;

import org.springframework.cache.interceptor.KeyGenerator;

import java.lang.reflect.Method;

public class MessageKeyGenerator implements KeyGenerator {

    // It can be cached by language only, not like en-US, but does not matter
    // params[0]!!.split(",")[0].split("-")[0]
    @Override
    public Object generate(Object target, Method method, Object... params) {
        return params[0];
    }
}

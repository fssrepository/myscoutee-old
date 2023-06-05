package com.raxim.myscoutee.profile.service;

import org.springframework.cache.interceptor.KeyGenerator;

import java.lang.reflect.Method;

public class MessageKeyGenerator implements KeyGenerator {

    // It can be cached by language only, not like en-US, but it does not matter
    // params[0]!!.split(",")[0].split("-")[0]
    @Override
    public Object generate(Object target, Method method, Object... params) {
        return params[0];
    }
}
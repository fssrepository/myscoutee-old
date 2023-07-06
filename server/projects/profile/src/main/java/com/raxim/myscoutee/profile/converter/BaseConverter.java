package com.raxim.myscoutee.profile.converter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.core.convert.converter.Converter;

//placeholder for autowire
public abstract class BaseConverter<T extends Object, U extends Object> implements Converter<T, U> {

    public boolean canConvert(Object t) {
        Type[] typeArguments = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
        if (typeArguments.length >= 1 && typeArguments[0] instanceof Class) {
            Class<?> typeArgument = (Class<?>) typeArguments[0];
            return typeArgument.isInstance(t);
        }
        return false;
    }
}

package com.raxim.myscoutee.profile.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class Converters<T extends Convertable<T>, U extends Convertable<U>> {
    private final List<BaseConverter<T, U>> converters;

    public Converters() {
        this(new ArrayList<>());
    }

    public Converters(List<BaseConverter<T, U>> converters) {
        this.converters = converters;
    }

    public Optional<U> convert(T obj) {
        for (BaseConverter<T, U> converter : converters) {
            if (converter.canConvert(obj)) {
                return Optional.of(converter.convert(obj));
            }
        }
        return Optional.empty();
    }

}

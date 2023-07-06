package com.raxim.myscoutee.profile.converter;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class Converters {
    private final List<? extends BaseConverter<Object, Object>> converters;

    public Converters(List<? extends BaseConverter<Object, Object>> converters) {
        this.converters = converters;
    }

    public Object convert(Object obj) {
        return this.converters.stream().filter(converter -> converter.canConvert(converter))
                .map(converter -> converter.convert(obj)).findFirst().get();
    }

}

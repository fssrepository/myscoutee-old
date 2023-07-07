package com.raxim.myscoutee.profile.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class Converters {
    private final List<? extends BaseConverter<Convertable, Convertable>> converters;

    public Converters() {
        this(new ArrayList<>());
    }

    public Converters(List<? extends BaseConverter<Convertable, Convertable>> converters) {
        this.converters = converters;
    }

    public Optional<Convertable> convert(Convertable obj) {
        Optional<Convertable> objConverted = this.converters.stream().filter(converter -> converter.canConvert(obj))
                .map(converter -> converter.convert(obj)).findFirst();
        return objConverted;
    }

}

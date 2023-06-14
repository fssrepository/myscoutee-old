package com.raxim.myscoutee.algo.generator;

import java.util.Set;

public interface IGenerator<T> {
    Set<T> generate(Integer num);
}

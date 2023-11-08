package com.raxim.myscoutee.common.util;

import com.mongodb.Function;

public class MetricsUtil {
    public static <T, R> R measure(Function<T, R> function, T input) {
        long startTime = System.currentTimeMillis();

        R result = function.apply(input);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Execution time: " + duration + " milliseconds");

        return result;
    }
}

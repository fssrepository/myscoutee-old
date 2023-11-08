package com.raxim.myscoutee.algo.generator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

public class RandomPairTests {

    private static final Integer LOW = 5;
    private final static Integer HIGH = 10;
    private static final Integer ITERATION = 10000;

    @Test
    public void generate() {
        System.out.println(Instant.now());

        for (int i = 0; i < ITERATION; i++) {
            List<Integer> pairs = new RandomPair().nextInt(LOW, HIGH);

            // System.out.println(pairs);

            assertTrue(pairs.size() == 2);
            assertTrue(pairs.get(0) >= LOW);
            assertTrue(pairs.get(1) < HIGH);
            assertTrue(pairs.get(0) < pairs.get(1));
        }

        System.out.println(Instant.now());
    }
}

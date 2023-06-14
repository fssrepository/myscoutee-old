package com.raxim.myscoutee.algo.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomPair {
    private final Random random = new Random();

    public List<Integer> nextInt(int low, int high) {
        int idxFrom = random.nextInt(high - low) + low;
        int idxTo = -1;

        if (idxFrom == low) {
            idxTo = random.nextInt(high - (idxFrom + 1)) + idxFrom + 1;
        } else if (idxFrom == high - 1) {
            idxTo = random.nextInt(idxFrom - low) + low;
        } else {
            boolean isUp = random.nextBoolean();
            if (isUp) {
                idxTo = random.nextInt(high - (idxFrom + 1)) + idxFrom + 1;
            } else {
                idxTo = random.nextInt(idxFrom - low) + low;
            }
        }

        if (idxFrom > idxTo) {
            int tmp = idxFrom;
            idxFrom = idxTo;
            idxTo = tmp;
        }

        List<Integer> nums = new ArrayList<>();
        nums.add(idxFrom);
        nums.add(idxTo);
        return nums;
    }
}

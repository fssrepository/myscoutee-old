package com.raxim.myscoutee.profile.util;

import java.util.List;

import com.raxim.myscoutee.common.util.CommonUtil;

public class LikeUtil {
    private static final double MAX_COUNT = 50;
    private static final double MAX_VALUE = 10;

    public static double calcAdjustedHarmonicMean(List<Double> values, List<Double> adjustments) {
        double harmonicMean = values.size() /
                values.stream()
                        .mapToDouble(value -> 1 / value)
                        .sum();

        double adjustedValue = 0;
        if (!adjustments.isEmpty()) {
            double averageValue = adjustments
                    .stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .getAsDouble();

            /*
             * f(x) = log(x+1) / log(n + 1), where n = MAX_RATE_COUNT, x = adjustment.size()
             */
            double exponencial = CommonUtil.exp(adjustments.size(), MAX_COUNT);
            adjustedValue = (averageValue * exponencial) / MAX_VALUE;
        }

        double adjustedHarmonicMeanForStatusP = harmonicMean + adjustedValue;
        return adjustedHarmonicMeanForStatusP;
    }
}

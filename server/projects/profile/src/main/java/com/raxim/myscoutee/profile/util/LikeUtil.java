package com.raxim.myscoutee.profile.util;

import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.DistanceUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Like;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.School;
import com.raxim.myscoutee.profile.data.dto.rest.LikeDTO;

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

    public static long calcDistance(Profile profile1, Profile profile2) {
        double locDist = DistanceUtil.haversine(profile1.getPosition(), profile2.getPosition());

        double ageDist = 0;
        if (profile1.getBirthday() != null && profile2.getBirthday() != null) {
            YearMonth profile1YM = YearMonth.from(profile1.getBirthday().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate());

            YearMonth profile2YM = YearMonth.from(profile2.getBirthday().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate());

            ageDist = Math.pow(Math.abs(ChronoUnit.MONTHS.between(profile1YM, profile2YM)), 3);
        }

        int childDist = 0;
        if (profile1.getHasChild() != null && profile2.getHasChild() != null) {
            childDist = 5 * (profile1.getHasChild().equals(profile2.getHasChild()) ? 0 : 1);
        }

        double heightDist = 0;
        if (profile1.getHeight() != null && profile2.getHeight() != null) {
            heightDist = Math.pow(Math.abs(profile1.getHeight() - profile2.getHeight()), 2);
        }

        int maritalDist = 0;
        if (profile1.getMarital() != null && profile2.getMarital() != null) {
            maritalDist = 10 * (profile1.getMarital().equals(profile2.getMarital()) ? 0 : 1);
        }

        int physiqueDist = 0;
        if (profile1.getPhysique() != null && profile2.getPhysique() != null) {
            physiqueDist = 2 * (profile1.getPhysique().equals(profile2.getPhysique()) ? 0 : 1);
        }

        int professionDist = 0;
        if (profile1.getProfession() != null && profile2.getProfession() != null) {
            professionDist = 6 * (profile1.getProfession().equals(profile2.getProfession()) ? 0 : 1);
        }

        int religionDist = 0;
        if (profile1.getReligion() != null && profile2.getReligion() != null) {
            religionDist = 3 * (profile1.getReligion().equals(profile2.getReligion()) ? 0 : 1);
        }

        int smokerDist = 0;
        if (profile1.getSmoker() != null && profile2.getSmoker() != null) {
            smokerDist = 15 * (profile1.getSmoker().equals(profile2.getSmoker()) ? 0 : 1);
        }

        int langDist = 0;
        if (profile1.getLanguages() != null && profile2.getLanguages() != null) {
            langDist = profile1.getLanguages().stream()
                    .filter(profile2.getLanguages()::contains)
                    .mapToInt(e -> 30)
                    .sum();
        }

        int schoolDist = 0;
        if (profile1.getSchools() != null && profile2.getSchools() != null) {
            Set<String> schoolNames1 = profile1.getSchools().stream().map(School::getName).collect(Collectors.toSet());
            Set<String> schoolNames2 = profile2.getSchools().stream().map(School::getName).collect(Collectors.toSet());

            schoolDist = schoolNames1.stream()
                    .filter(schoolNames2::contains)
                    .mapToInt(e -> 10)
                    .sum();
        }

        long dist = Math.round(
                1 * locDist +
                        5 * ageDist +
                        5 * childDist +
                        2 * heightDist +
                        10 * maritalDist +
                        1 * physiqueDist +
                        3 * professionDist +
                        3 * religionDist +
                        5 * smokerDist +
                        1 * langDist +
                        1 * schoolDist);

        return Math.max(0, Math.min(100, dist));
    }

    public static boolean isEqual(LikeDTO likeDTO, Like dbLike) {
        return Objects.equals(likeDTO.getFrom(), dbLike.getFrom().getId()) &&
                Objects.equals(likeDTO.getTo(), dbLike.getTo().getId()) &&
                Objects.equals(dbLike.getRef(), likeDTO.getRef());
    }

    public static boolean isReverseEqual(LikeDTO likeDTO, Like dbLike) {
        return Objects.equals(likeDTO.getFrom(), dbLike.getTo().getId()) &&
                Objects.equals(likeDTO.getTo(), dbLike.getFrom().getId()) &&
                Objects.equals(dbLike.getRef(), likeDTO.getRef());
    }
}

package com.raxim.myscoutee.common.util;

import java.net.URLDecoder;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.function.TriFunction;
import org.bson.BsonBinary;
import org.bson.BsonBinarySubType;
import org.bson.UuidRepresentation;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.raxim.myscoutee.profile.data.document.mongo.RangeLocal;
import com.raxim.myscoutee.profile.data.dto.rest.PageItemDTO;

public class CommonUtil {

    public static final String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
            "Nov", "Dec" };

    public static final Pattern dateRegex = Pattern.compile(
            "((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[\\s\\S]*(?:(?:1|2)[0-9]{3}))[\\s\\S]?[\u2010-\u2015\\-]?[\\s\\S]?((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)?[\\s\\S]*(?:(?:1|2)[0-9]{3}))?[\\s\\S]?");

    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("MMM yyyy")
            .toFormatter();

    public static RangeLocal parseRange(String rangeString) {
        String[] dateParts = rangeString.split("[\u2010-\u2015\\-]");

        LocalDateTime startDate = parseDate(dateParts[0].trim());

        LocalDateTime endDate = null;
        if (dateParts.length > 1 && !dateParts[1].isBlank()) {
            endDate = parseDate(dateParts[1].trim());
            endDate = endDate.withDayOfMonth(endDate.toLocalDate().lengthOfMonth()).truncatedTo(ChronoUnit.DAYS);
        }

        return new RangeLocal(startDate, endDate);
    }

    private static LocalDateTime parseDate(String dateString) {
        LocalDate date = null;

        for (String month : months) {
            if (dateString.contains(month)) {
                dateString = dateString.replace(month, "").trim();
                break;
            }
        }

        date = LocalDate.parse(dateString, formatter);

        return LocalDateTime.of(date.getYear(), date.getMonth(), 1, 0, 0).truncatedTo(ChronoUnit.DAYS);
    }

    public static String asISO(Date date) {
        Instant instant = date.toInstant();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        return formatter.format(instant);
    }

    public static String decode(String string) {
        try {
            return URLDecoder.decode(string, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return string;
        }
    }

    public static UUID parseUUID(String uuidString) {
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String asUUID(String base64) {
        byte[] bytes = Base64.getDecoder().decode(base64);
        UUID uuid = new BsonBinary(BsonBinarySubType.UUID_LEGACY, bytes)
                .asUuid(UuidRepresentation.JAVA_LEGACY);
        return uuid.toString();
    }

    public static String asBase64(UUID uuid) {
        byte[] data = new BsonBinary(uuid, UuidRepresentation.JAVA_LEGACY).getData();
        return Base64.getEncoder().encodeToString(data);
    }

    public static Point point(GeoJsonPoint point) {
        return new Point(new Position(point.getCoordinates()));
    }

    /*
     * return a number between 0..1, result is exponencially growing towards 1
     */
    public static double exp(double pValue, double maxPValue) {
        double value = pValue < 1 ? 1 : pValue > 50 ? 50 : pValue;
        return Math.log(value + 1) / Math.log(maxPValue + 1);
    }

    public static List<Integer> randomRange(int min, int max, int size) {
        List<Integer> numbers = IntStream.rangeClosed(min, max)
                .boxed()
                .collect(Collectors.toList());

        Collections.shuffle(numbers);

        return numbers.subList(0, size);
    }

    @SafeVarargs
    public static <T> List<List<List<T>>> permutation(Set<T>... sets) {
        return Arrays.stream(sets).map(subSet -> subSet.stream()
                .flatMap(a -> subSet.stream()
                        .filter(b -> !a.equals(b))
                        .map(b -> Arrays.asList(a, b)))
                .distinct().toList()).toList();
    }

    public static <T extends PageItemDTO> List<Object> offset(List<T> list, Object[] offset) {
        return !list.isEmpty() ? list.get(list.size() - 1).getOffset() : List.of();
    }
}

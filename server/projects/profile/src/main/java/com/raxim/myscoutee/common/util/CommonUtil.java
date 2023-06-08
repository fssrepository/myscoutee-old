package com.raxim.myscoutee.common.util;

import java.net.URLDecoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bson.BsonBinary;
import org.bson.BsonBinarySubType;
import org.bson.UuidRepresentation;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.raxim.myscoutee.profile.data.document.mongo.RangeLocal;

public class CommonUtil {

    public static final String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
            "Nov", "Dec" };

    public static final Pattern dateRegex = Pattern.compile("((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[\\s\\S]*(?:(?:1|2)[0-9]{3}))[\\s\\S]?[\u2010-\u2015\\-]?[\\s\\S]?((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)?[\\s\\S]*(?:(?:1|2)[0-9]{3}))?[\\s\\S]?");

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

    public static String asUUID(String string) {
        byte[] bytes = Base64.getDecoder().decode(string);
        UUID uuid = new BsonBinary(BsonBinarySubType.UUID_LEGACY, bytes)
                .asUuid(UuidRepresentation.JAVA_LEGACY);
        return uuid.toString();
    }

    public static Point point(GeoJsonPoint point) {
        return new Point(new Position(point.getCoordinates()));
    }
}

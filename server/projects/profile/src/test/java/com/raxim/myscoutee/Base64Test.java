package com.raxim.myscoutee;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.EventItem;
import com.raxim.myscoutee.profile.data.document.mongo.RangeLocal;

public class Base64Test {

    @Test
    public void base64() {
        UUID uuid = UUID.randomUUID();
        System.out.println(uuid);
        System.out.println(CommonUtil.asBase64(uuid));
        System.out.println(UUID.fromString("ce0e1cec-f698-4888-aedf-b1dc9aa2596f").toString());
        System.out.println(CommonUtil.asBase64(UUID.fromString("a73860cd-d9a2-4186-8a9b-3f9fc53c2342")));
        System.out.println(CommonUtil.asUUID("QVzopgl2TUiN9zD3CJIJAQ=="));
    }

    @Test
    public void rangeExp() {
        double parameter = 25;
        double maxParameterValue = 50;
        System.out.println(CommonUtil.exp(parameter, maxParameterValue));
    }

    @Test
    public void hash() {
        int result = new Date().hashCode();
        result = 31 * result + Boolean.TRUE.hashCode();
        System.out.println(result);
    }

    @Disabled
    @Test
    public void daysDiff() {
        LocalDateTime start = LocalDateTime.of(1998, 12, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(1997, 12, 1, 0, 0);

        int daysBetween = (int) ChronoUnit.DAYS.between(start, end);

        System.out.println(daysBetween);
    }

    @Disabled
    @Test
    public void maxDate() {
        List<EventItem> items = new ArrayList<>();
        for (int i = 0; i <= 5; i++) {
            items.add(genEventItem());
        }

        System.out.println(items);

        RangeLocal maxRange = items.stream()
                .map(item -> item.getRange())
                .reduce((acc, range) -> {
                    LocalDateTime start = acc.getStart();
                    if (range.getStart().isBefore(start)) {
                        start = range.getStart();
                    }

                    LocalDateTime end = acc.getEnd();
                    if (range.getEnd().isAfter(end)) {
                        end = range.getEnd();
                    }

                    return new RangeLocal(start, end);
                }).orElse(null);

        System.out.println(maxRange);
    }

    private EventItem genEventItem() {
        long startDay = LocalDateTime.of(1970, 1, 1, 0, 0)
                .toEpochSecond(ZoneOffset.UTC);
        long maxDay = LocalDateTime.now()
                .toEpochSecond(ZoneOffset.UTC);

        long startRand = ThreadLocalRandom.current()
                .nextLong(startDay, maxDay);
        LocalDateTime start = LocalDateTime.ofEpochSecond(startRand, 0, ZoneOffset.UTC);

        long midDay = start.toEpochSecond(ZoneOffset.UTC);

        long endRand = ThreadLocalRandom.current()
                .nextLong(midDay, maxDay);
        LocalDateTime end = LocalDateTime.ofEpochSecond(endRand, 0, ZoneOffset.UTC);

        RangeLocal range = new RangeLocal(start, end);

        var eventItem = new EventItem();
        eventItem.setRange(range);
        return eventItem;
    }

    @Disabled
    @Test
    public void monthsDiff() {
        YearMonth profile1YM = YearMonth.from(
                new Date().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate());

        YearMonth profile2YM = YearMonth.from(
                LocalDate.parse("2025-01-01"));

        double monthsBetween = ChronoUnit.MONTHS.between(profile1YM, profile2YM);

        System.out.println(monthsBetween);
    }

    @Test
    public void shouldPermutate() {
        List<List<List<String>>> nodes = CommonUtil.permutation(Set.of("2", "4", "5", "7", "8", "9"));
        System.out.println(nodes);
        List<Set<Edge>> edges = nodes.stream()
                .map(group -> group.stream().map(pair -> new Edge(pair.get(0), pair.get(1)))
                        .collect(Collectors.toSet()))
                .toList();

        Set<Edge> group1 = edges.get(0);
        assertEquals(15, group1.size());

        group1.add(new Edge("5", "4"));
        assertEquals(15, group1.size());

        group1.add(new Edge("11", "4"));
        assertEquals(16, group1.size());

    }
}

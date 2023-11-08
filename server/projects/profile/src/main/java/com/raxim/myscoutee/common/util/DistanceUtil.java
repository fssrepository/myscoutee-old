package com.raxim.myscoutee.common.util;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

public class DistanceUtil {
    private static final double EARTH_RADIUS = 6370986.0; // meters

    public static double haversine(GeoJsonPoint a, GeoJsonPoint b) {
        double dLat = Math.toRadians(b.getX() - a.getX());
        double dLon = Math.toRadians(b.getY() - a.getY());
        double lat1 = Math.toRadians(a.getX());
        double lat2 = Math.toRadians(b.getX());
        double dist = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
                * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(dist));
        return EARTH_RADIUS * c;
    }

    public static double vincenty(GeoJsonPoint a, GeoJsonPoint b) {
        double dLat = Math.toRadians(b.getX() - a.getX());
        double dLon = Math.toRadians(b.getY() - a.getY());
        double lat1 = Math.toRadians(a.getX());
        double lat2 = Math.toRadians(b.getX());
        double dist = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1)
                * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(dist), Math.sqrt(1 - dist));
        return EARTH_RADIUS * c;
    }
}

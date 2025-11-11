package com.gynaid.backend.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class LocationUtils {

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private static final double EARTH_RADIUS_KM = 6371.0;

    public static Point toPoint(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("Latitude and longitude must not be null");
        }
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    /**
     * Calculates the distance between two geographic points using the Haversine formula.
     * 
     * @param lat1 Latitude of the first point
     * @param lon1 Longitude of the first point
     * @param lat2 Latitude of the second point
     * @param lon2 Longitude of the second point
     * @return Distance in kilometers
     */
    public static double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Extracts latitude from a JTS Point geometry.
     */
    public static double getLatitude(Point point) {
        if (point == null) {
            throw new IllegalArgumentException("Point must not be null");
        }
        return point.getY();
    }

    /**
     * Extracts longitude from a JTS Point geometry.
     */
    public static double getLongitude(Point point) {
        if (point == null) {
            throw new IllegalArgumentException("Point must not be null");
        }
        return point.getX();
    }
}


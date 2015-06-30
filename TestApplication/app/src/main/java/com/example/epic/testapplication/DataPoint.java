package com.example.epic.testapplication;

import com.google.maps.android.geometry.Point;

/**
 * Created by henryshangguan on 6/30/15.
 */
public class DataPoint {
    private int route;
    private Point coordinates;
    private double altitude;
    private double timeElapsed;
    private double distanceFromLast;
    private double totalDistance;

    public DataPoint(int route, double lat, double lng, double alt, double timeElapsed, double diff, double dist) {
        this.route = route;
        this.coordinates = new Point(lat, lng);
        this.altitude = alt;
        this.timeElapsed = timeElapsed;
        this.distanceFromLast = diff;
        this.totalDistance = dist;
    }
}

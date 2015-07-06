package com.example.epic.testapplication;

import com.google.maps.android.geometry.Point;

/**
 * Created by henryshangguan on 6/30/15.
 */
public class DataPoint {
    private int route;
    private Point coordinates;
    private double altitude;
    private double time_elapsed_seconds;
    private double distance_from_prev_miles;
    private double total_distance_miles;
    private double battery;
    private double MPG;
    private double velocity_mph;

    public DataPoint(int route, double lat, double lng, double alt, double timeElapsed, double diff, double dist, double batt, double mpg, double vel) {
        this.route = route;
        this.coordinates = new Point(lat, lng);
        this.altitude = alt;
        this.time_elapsed_seconds = timeElapsed;
        this.distance_from_prev_miles = diff;
        this.total_distance_miles = dist;
        this.battery = batt;
        this.MPG = mpg;
        this.velocity_mph = vel;
    }
}

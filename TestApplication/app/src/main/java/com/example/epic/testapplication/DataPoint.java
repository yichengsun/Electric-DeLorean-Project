package com.example.epic.testapplication;

import com.google.maps.android.geometry.Point;

import java.text.DateFormat;

/**
 * Created by henryshangguan on 6/30/15.
 */
public class DataPoint {
    private String timestamp;
    private int route;
    private Point coordinates;
    private double time_elapsed_seconds;
    private double electricity_used;
    private double total_distance_miles;
    private double distance_to_empty_miles;
    private double velocity_mph;
    private double average_mpkwh;
    private double charge_state;
    private double amperage;
    private double power;
    private double voltage;
    private double rpm;

    public DataPoint(String time, int route, double lat, double lng, double timeElapsed, double dist,
                     double distLeft, double electricityUsed, double mpkwh, double vel,
                     double chargeState, double amperage, double power, double voltage, double rpm) {
        this.timestamp = time;
        this.route = route;
        this.coordinates = new Point(lat, lng);
        this.time_elapsed_seconds = timeElapsed;
        this.total_distance_miles = dist;
        this.distance_to_empty_miles = distLeft;
        this.electricity_used = electricityUsed;
        this.average_mpkwh = mpkwh;
        this.velocity_mph = vel;
        this.charge_state = chargeState;
        this.amperage = amperage;
        this.power = power;
        this.voltage = voltage;
        this.rpm = rpm;
    }
}

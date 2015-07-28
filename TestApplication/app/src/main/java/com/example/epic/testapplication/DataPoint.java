package com.example.epic.testapplication;

import com.google.maps.android.geometry.Point;

import java.text.DateFormat;

/**
 * Class to represent one logged data point.
 */
public class DataPoint {
    // All recorded data categories
    //TODO ADD UNITS TO BMS/MOTOR CONTROLLER DATA
    private String timestamp; // Time data was recorded
    private int route; // Route number for trip this data point belongs to
    private Point coordinates; // Point containing latitude and longitude data
    private double time_elapsed_seconds; // Time elapsed since start of trip
    private double electricity_used_kilowatthours; // Electricity used since start of trip
    private double total_distance_miles; // Total distance since start of trip
    private double distance_to_empty_miles; // Estimated remaining range of battery
    private double velocity_mph; // Instantaneous velocity
    private double average_mpkwh; // Average miles per kilowatt hour since start of trip
    private double charge_state_0_to_5; // Charge state of battery, from BMS
    private double amperage; // Instantaneous amperage, from BMS
    private double power; // Power, from motor controller
    private double voltage; // Voltage, from motor controller
    private double rpm; // RPM, from motor controller

    /**
     * Creates a data point
     * @param time Time data was recorded
     * @param route Route number for trip this data point belongs to
     * @param lat Latitude of recorded location
     * @param lng Longitude of recorded location
     * @param timeElapsed Time elapsed since start of trip
     * @param dist Total distance since start of trip
     * @param distLeft Estimated remaining range of battery
     * @param electricityUsed Electricity used since start of trip
     * @param mpkwh Average miles per kilowatt hour since start of trip
     * @param vel Average velocity since start of trip
     * @param chargeState Charge state of battery, from BMS
     * @param amperage Instantaneous amperage, from BMS
     * @param power Power, from motor controller
     * @param voltage Voltage, from motor controller
     * @param rpm RPM, from motor controller
     */
    public DataPoint(String time, int route, double lat, double lng, double timeElapsed, double dist,
                     double distLeft, double electricityUsed, double mpkwh, double vel,
                     double chargeState, double amperage, double power, double voltage, double rpm) {
        this.timestamp = time;
        this.route = route;
        this.coordinates = new Point(lat, lng);
        this.time_elapsed_seconds = timeElapsed;
        this.total_distance_miles = dist;
        this.distance_to_empty_miles = distLeft;
        this.electricity_used_kilowatthours = electricityUsed;
        this.average_mpkwh = mpkwh;
        this.velocity_mph = vel;
        this.charge_state_0_to_5 = chargeState;
        this.amperage = amperage;
        this.power = power;
        this.voltage = voltage;
        this.rpm = rpm;
    }
}

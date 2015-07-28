package com.example.epic.testapplication;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.text.DateFormat;
import java.util.Date;
import android.os.Handler;

/**
 *
 */
public class PollService extends Service implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener  {
    //TAG for log statements
    private static final String TAG = "PollService";
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    // GoogleApiClient, used to obtain location through LocationServices API
    protected GoogleApiClient mGoogleApiClient;
    // LocationRequest for LocationServices
    protected LocationRequest mLocationRequest;
    // Whether PollService is still requesting location updates
    protected boolean mRequestingLocationUpdates;

    protected String mTimestamp; // time stamp of when data was recorded
    protected int mLastRouteId; // Route id for most recent data point
    protected Location mLastLocation; // Most recent measurement of location
    protected double mLastLat, mLastLng; // Most recent measurement of latitude and longitude
    protected double mTimeElapsed; // Most recent measurement of time elapsed (seconds)
    protected double mDistanceInterval; // Linear distance from last data point location (miles)
    protected double mTotalDistance; // Most recent measurement of total distance traveled on trip (miles)
    protected double mMPKwh; // Most recent calculation of average miles per kilowatt hour
    protected double mVelocity; // Most recent calculation of instantaneous velocity (mph)

    protected double mChargeState; // Most recent reading of battery charge state, from BMS (0 to 5)
    protected double mAmperage; // Most recent reading of instantaneous amperage, from BMS (Amp)
    protected double mPower; // Most recent reading of Power, from motor controller (TODO units)
    protected double mAveragePower; // Most recent calculation of average power on trip (TODO units)
    protected double mElectricityUsed; // Most recent calculation of total electricity used on trip (Kwh)
    protected double mVoltage; // Most recent reading of voltage, from motor controller (Volts)
    protected double mRPM; // Most recent reading of RPM, from motor controller
    protected double mDistanceToEmpty;// Most recent estimate of remaining range of battery (miles)

    private long mStartTime; // Time PollService started recording data (start of trip)
    private int insertCount = 0; // Number of data points inserted

    // Refresh rate, in milliseconds
    public static final long UPDATE_INTERVAL = 1000;
    // Fastest refresh rate
    public static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;
    // TODO Capacity of battery pack
    private static final double BATTERY_CAPACITY = 9999999999.9;

    // meters to miles conversion
    private static final double METERS_TO_MILES = 0.000621371192;
    // Miles per second to miles per hour
    private static final double MPS_TO_MPH = 3600;
    // nanoseconds to seconds
    private static final double NANO_TO_SECONDS = 1000000000.0;
    // seconds to hours
    private static final double SECONDS_TO_HOURS = 3600.0;
    // zero
    private double ZERO = 0.0;

    /**
     * Return this instance of PollService so clients can call public methods
     */
    public class LocalBinder extends Binder {
        PollService getService() {
            return PollService.this;
        }
    }

    /**
     * Build Google Api Client with LocationServices API
     */
    protected synchronized void buildGoogleApiClient() {
        Log.d(TAG, "buildingGoogleApiClient called");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * Create location request with designated refresh rate
     */
    protected void createLocationRequest() {
        Log.d(TAG, "createLocationRequest called");
        mLocationRequest = new LocationRequest()
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Begin location updates from LocationServices
     */
    protected void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates called");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, (LocationListener) this);
    }

    /**
     * Updates all data variables and inserts a new data point into the database every time
     * the device's location has changed.
     * @param location most recent location
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged called");
        updateVariables(location);
        MainActivity.mDataDBHelper.insertDataPoint(mTimestamp, mLastRouteId, mLastLat, mLastLng, mTimeElapsed,
                mTotalDistance, mDistanceToEmpty, mMPKwh, mElectricityUsed, mVelocity, mChargeState,
                mAmperage, mPower, mVoltage, mRPM);
    }

    /**
     * When connected, start requesting location updates
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected called");

        if (mLastLocation != null) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } else {
            Toast.makeText(this, getResources().getString(R.string.no_location_detected),
                    Toast.LENGTH_LONG).show();
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    /**
     * If connection is suspended, reconnect to Google Api Client
     * @param cause cause of suspsension
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended called");
        mGoogleApiClient.connect();
    }

    /**
     * If connection fails, provide log statement with error code
     * @param result connection failure resuot
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    /**
     * Callback upon binding the service, starts location updates and inserts a new route into
     * the route database.
     * @param intent intent binding the service
     * @return successful completion of onBind()
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "PollService running");

        Runnable mainR = new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                mRequestingLocationUpdates = true;
                buildGoogleApiClient();

                mGoogleApiClient.connect();

                final Handler handler = new Handler();

                Runnable r = new Runnable() {
                    public void run() {
                        startLocationUpdates();
                    }
                };

                handler.postDelayed(r, 100); // Delay to provide time for connecting to Google Api Client
            }
        };

        Thread t = new Thread(mainR);
        t.start();

        mStartTime = System.nanoTime();
        mLastRouteId = MainActivity.mRouteDBHelper.getLastRouteId() + 1; // Increment from last route id
        Route route = new Route(mLastRouteId);
        MainActivity.mRouteDBHelper.insertRoute(route);

        return mBinder;
    }

    /**
     * Callback upon unbinding the service, terminates location updates, and calculates "lifetime" stats
     * for the trip
     * @param intent
     * @return successful completion of onUnbind()
     */
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "stopLocationUpdates called");
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mGoogleApiClient.disconnect();

        calculateLifetimeStats();
        //TODO insert lifetime stats into route database

        return true;
    }

/****************************** Calculation methods *******************************/
    /**
     * Performs calculations and updates all data variables
     * @param location location of recorded data
     */
    private void updateVariables(Location location) {
        double mOldTimeElapsed = mTimeElapsed; // Most recent measurement before this data point
        double mOldLat, mOldLng; // Most recent latitude and longitude before this location

        mLastLocation = location; // Update most recent location
        mLastLat = mLastLocation.getLatitude();
        mLastLng = mLastLocation.getLongitude();

        // If there are no other points in the data base, set mOldLat and mOldLng to this location
        if (Double.compare(ZERO, mLastLat) == 0 && Double.compare(ZERO, mLastLng) == 0)
        {
            mOldLat = mLastLocation.getLatitude();
            mOldLng = mLastLocation.getLongitude();
        } else {
            mOldLat = mLastLat;
            mOldLng = mLastLng;
        }

        mTimestamp = DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()));
        // Get time elapsed in nanoseconds, convert to seconds
        mTimeElapsed = (System.nanoTime() - mStartTime) / NANO_TO_SECONDS;

        mChargeState = MainActivity.getChargeState();
        mAmperage = MainActivity.getAmperage();
        mVoltage = MainActivity.getVoltage();
        mRPM = MainActivity.getRPM();
        mPower = MainActivity.getPower();

        // Calculate new average power by adding most recent power reading to old average
        // and re-averaging for trip
        mAveragePower = ((mAveragePower * insertCount) + mPower) / ++insertCount; // increment count
        // Calculate new total for electricity used (avg. power * time), result is in kwh
        mElectricityUsed = mAveragePower * (mTimeElapsed / SECONDS_TO_HOURS);
        // method returns distance in meters, convert to miles
        mDistanceInterval = distanceBetweenTwo(mOldLat, mOldLng, mLastLat, mLastLng) * METERS_TO_MILES;
        // new total distance = previous total distance + recently traveled distance
        mTotalDistance += mDistanceInterval;
        // Convert speed from meters per second to miles per hour
        mVelocity = (mDistanceInterval/(mTimeElapsed - mOldTimeElapsed)) * MPS_TO_MPH;
        // Efficiency = distance / power used
        mMPKwh = mTotalDistance / mElectricityUsed;
        // Estimated remaining range = reminaing battery percentage * efficiency so far
        mDistanceToEmpty = mChargeState * BATTERY_CAPACITY * mMPKwh; // TODO CONVERT BATTERY CHARGE STATE TO PERCENTAGE
    }

    /**
     * Helper method to calculate distance between two points on Earth
     * @param prevLat latitude of "from" point
     * @param prevLong longitude of "from" point
     * @param newLat latitude of "to" point
     * @param newLong longitude of "to" point
     * @return distance between two points, in meters
     */
    private double distanceBetweenTwo(double prevLat, double prevLong, double newLat, double newLong) {
        LatLng oldPoint = new LatLng(prevLat, prevLong);
        LatLng newPoint = new LatLng(newLat, newLong);
        return SphericalUtil.computeDistanceBetween(oldPoint, newPoint);
    }

    /**
     * TODO comment this
     */
    private void calculateLifetimeStats() {
        //TODO calculations
        //TODO enter into routeDB
    }

}

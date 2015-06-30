package com.example.epic.testapplication;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.PolylineOptions;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import android.os.Handler;

/**
 * Created by henryshangguan on 6/22/15.
 */
public class PollService extends Service implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener  {
    //TAG
    private static final String TAG = "PollService";
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public static final long UPDATE_INTERVAL = 10000;
    public static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;

    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected LocationRequest mLocationRequest;
    protected boolean mRequestingLocationUpdates;

    protected CoordDBHelper mCoordDBHelper;
    protected int mLastRouteId = 0;
    protected double mLastLat = 0.0;
    protected double mLastLng = 0.0;
    protected double mLastAlt = 0.0;
    protected Date mLastDate;
    protected String mLastUpdateTime;


    public class LocalBinder extends Binder {
        PollService getService() {
            // Return this instance of PollService so clients can call public methods
            return PollService.this;
        }
    }

    protected synchronized void buildGoogleApiClient() {
        Log.d(TAG, "buildingGoogleApiClient called");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        Log.d(TAG, "createLocationRequest called");
        mLocationRequest = new LocationRequest()
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates called");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, (LocationListener)this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged called");
        mLastLocation = location;
        mLastLat = mLastLocation.getLatitude();
        mLastLng = mLastLocation.getLongitude();
        //TODO getAltitude returns 0.0 every time
        mLastAlt = mLastLocation.getAltitude();
        mLastDate = new Date();
        mLastUpdateTime = DateFormat.getTimeInstance().format(mLastDate);

        mCoordDBHelper.insertCoord(mLastRouteId, mLastLat, mLastLng, mLastAlt);
        //updateUI();
        Toast.makeText(this, getResources().getString(R.string.location_updated),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected called");

        if (mLastLocation != null) {
            //TODO add accuracy, speed, time, altitude
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastDate = new Date();
            mLastUpdateTime = DateFormat.getTimeInstance().format(mLastDate);
            //updateUI();
        } else {
            Toast.makeText(this, getResources().getString(R.string.no_location_detected),
                    Toast.LENGTH_LONG).show();
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended called");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "PollService running");

        Runnable mainR = new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                mCoordDBHelper = new CoordDBHelper(PollService.this);
                mRequestingLocationUpdates = true;
                mLastUpdateTime = "";
                buildGoogleApiClient();

                mGoogleApiClient.connect();

                final Handler handler = new Handler();

                Runnable r = new Runnable() {
                    public void run() {
                        startLocationUpdates();
                    }
                };

                handler.postDelayed(r, 100);
            }
        };

        Thread t = new Thread(mainR);
        t.start();

        return mBinder;
    }

    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "stopLocationUpdates called");
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        // TODO GEOJSON FILE CREATION + PARSE UPLOAD
        return false;
    }
}
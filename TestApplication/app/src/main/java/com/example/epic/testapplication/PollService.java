package com.example.epic.testapplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
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
import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.text.DateFormat;
import java.util.Date;
import android.os.Handler;

/**
 * Created by henryshangguan on 6/22/15.
 */
public class PollService extends Service implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener  {
    //TAG
    private static final String TAG = "PollService";
    // meters to miles conversion
    private static final double METERS_TO_MILES = 0.000621371192;
    // Miles per second to miles per hour
    private static final double MPS_TO_MPH = 3600;
    // nanoseconds to seconds
    private static final double NANO_TO_SECONDS = 1000000000.0;
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public static final long UPDATE_INTERVAL = 1000;
    public static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;

    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected LocationRequest mLocationRequest;
    protected boolean mRequestingLocationUpdates;

    protected CoordDBHelper mCoordDBHelper;
    protected RouteDBHelper mRouteDBHelper;
    protected String mTimestamp;
    protected int mLastRouteId = 0;
    protected double mLastLat = 0.0;
    protected double mLastLng = 0.0;
    protected double mTimeElapsed = 0.0;
    protected double mDistanceInterval = 0.0;
    protected double mTotalDistance = 0.0;
    protected double mBatteryLevel = 0.0;
    protected double mMPG = 0.0;
    protected double mVelocity = 0.0;
    private long mStartTime;

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
        double zero = 0.0;
        double mOldLat, mOldLng;
        double mOldTimeElapsed = mTimeElapsed;
        mLastLocation = location;

        if (Double.compare(zero, mLastLat) == 0 && Double.compare(zero, mLastLng) == 0)
        {
            mOldLat = mLastLocation.getLatitude();
            mOldLng = mLastLocation.getLongitude();
        } else {
            mOldLat = mLastLat;
            mOldLng = mLastLng;
        }

        mLastLat = mLastLocation.getLatitude();
        mLastLng = mLastLocation.getLongitude();
        mTimestamp = DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()));
        mTimeElapsed = (System.nanoTime() - mStartTime) / NANO_TO_SECONDS;
        mDistanceInterval = distanceBetweenTwo(mOldLat, mOldLng, mLastLat, mLastLng) * METERS_TO_MILES;
        mTotalDistance += mDistanceInterval;
        mVelocity = (mDistanceInterval/(mTimeElapsed - mOldTimeElapsed)) * MPS_TO_MPH;
        double[] stats = MainActivity.getBatteryData();
        mBatteryLevel = stats[0];
        mMPG = stats[1];

        mCoordDBHelper.insertCoord(mTimestamp, mLastRouteId, mLastLat, mLastLng, mTimeElapsed, mDistanceInterval, mTotalDistance, mBatteryLevel, mMPG, mVelocity);
        Toast.makeText(this, getResources().getString(R.string.location_updated),
                Toast.LENGTH_SHORT).show();
    }

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

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended called");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    // helper method to calculate distance between two points
    private double distanceBetweenTwo(double prevLat, double prevLong, double newLat, double newLong) {
        LatLng oldPoint = new LatLng(prevLat, prevLong);
        LatLng newPoint = new LatLng(newLat, newLong);
        return SphericalUtil.computeDistanceBetween(oldPoint, newPoint);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "PollService running");
        mStartTime = System.nanoTime();
        mCoordDBHelper = new CoordDBHelper(PollService.this);
        mRouteDBHelper = new RouteDBHelper(PollService.this);

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

                handler.postDelayed(r, 100);
            }
        };

        Thread t = new Thread(mainR);
        t.start();
        mLastRouteId = mCoordDBHelper.getLastRouteId() + 1;

        Route route = new Route(mLastRouteId);
        mRouteDBHelper.insertRoute(route);
        return mBinder;
    }

    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "stopLocationUpdates called");
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mGoogleApiClient.disconnect();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()) {
            DeLoreanApplication.uploadToParse(mLastRouteId);
        }
        return false;
    }
}

//            String jsonFile = mCoordDBHelper.dataToJSON(mLastRouteId);
//            final byte[] translated = jsonFile.getBytes();
//            ParseFile stored = new ParseFile("route.json", translated);
//            stored.saveInBackground();
//
//            ParseObject DeLoreanRouteObject = new ParseObject("DeLoreanRouteObject");
//            DeLoreanRouteObject.put("File", stored);
//            DeLoreanRouteObject.saveInBackground();
//
//            mRouteDBHelper.updateUploaded(mLastRouteId);
//            Toast.makeText(this, "saved to parse", Toast.LENGTH_LONG).show();
package com.example.epic.testapplication;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import java.util.Date;

public class MainActivity extends ActionBarActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    protected static final String TAG = "LocationUpdate";

    public static final long UPDATE_INTERVAL = 10000;
    public static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;

    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    protected GoogleApiClient mGoogleApiClient;
    protected Location mCurrentLocation;
    protected LocationRequest mLocationRequest;

    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected TextView mCurrentUpdateTimeTextView;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;

    protected boolean mRequestingLocationUpdates;

    protected CoordDBHelper mCoordDBHelper;
    protected int mCurrentRouteId = 0;
    protected double mCurrentLat = 0.0;
    protected double mCurrentLng = 0.0;
    protected double mCurrentAlt = 0.0;
    protected Date mCurrentDate;
    protected String mCurrentUpdateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate(Bundle) called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mCurrentUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);

        mCoordDBHelper = new CoordDBHelper(this);
        mRequestingLocationUpdates = false;
        mCurrentUpdateTime = "";
        updateValuesFromBundle(savedInstanceState);
        buildGoogleApiClient();
        //TODO ensure google play services APK using isGooglePlayServicesAvailable()
    }

//    protected void drawRoute() {
//        List<LatLng> latLngs =
//        PolylineOptions line = new PolylineOptions()
//                .width(5)
//                .color(Color.BLUE)
//                .visible(true)
//                .geodesic(true);
//
//        for (LatLng latlng : latLngs) {
//            line.add(latLng);
//        }
//        getMap().addPolyline(line);
//
//    }

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

    public void startUpdatesButtonHandler(View view) {
        Log.d(TAG, "startUpdatesButtonHandler called");
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }
    }

    public void stopUpdatesButtonHandler(View view) {
        Log.d(TAG, "stopUpdatesButtonHandler called");
        mRequestingLocationUpdates = false;
        setButtonsEnabledState();
        stopLocationUpdates();
    }

    protected void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates called");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, (LocationListener) this);
    }

    protected void stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates called");
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    private void setButtonsEnabledState() {
        Log.d(TAG, "setButtonEnabledState called");
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }

    private void updateUI() {
        Log.d(TAG, "updateUI called");
        if (mCurrentLocation != null) {
            mLatitudeTextView.setText(String.valueOf(mCurrentLat));
            mLongitudeTextView.setText(String.valueOf(mCurrentLng));
            mCurrentUpdateTimeTextView.setText(String.valueOf(mCurrentUpdateTime));

            mCoordDBHelper.insertCoord(mCurrentRouteId, mCurrentLat, mCurrentLng, mCurrentAlt);
            //TODO more textviews for alt,route, etc
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged called");
        mCurrentLocation = location;
        mCurrentLat = mCurrentLocation.getLatitude();
        mCurrentLng = mCurrentLocation.getLongitude();
        mCurrentAlt = mCurrentLocation.getAltitude();
        mCurrentDate = new Date();
        mCurrentUpdateTime = DateFormat.getTimeInstance().format(mCurrentDate);

        updateUI();
        Toast.makeText(this, getResources().getString(R.string.location_updated),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected called");

        if (mCurrentLocation != null) {
            //TODO add accuracy, speed, time, altitude
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mCurrentDate = new Date();
            mCurrentUpdateTime = DateFormat.getTimeInstance().format(mCurrentDate);
            updateUI();
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
    protected void onStart() {
        Log.d(TAG, "onStart called");
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume called");
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause called");
        //TODO make sure data updates during on pause
        super.onPause();
//        if (mGoogleApiClient.isConnected()) {
//            stopLocationUpdates();
//        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop called");
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onSaveInstanceState called");
        //TODO figure this out
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mCurrentUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.d(TAG, "updateValuesFromBundle called");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and
            // make sure that the Start Updates and Stop Updates buttons are
            // correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
                setButtonsEnabledState();
            }

            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocationis not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mCurrentUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mCurrentUpdateTime = savedInstanceState.getString(
                        LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }
    }

}
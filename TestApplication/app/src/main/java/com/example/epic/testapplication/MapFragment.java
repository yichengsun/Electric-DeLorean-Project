package com.example.epic.testapplication;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.location.Location;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

import android.support.v4.app.Fragment;
import android.widget.Toast;

public class MapFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener /*implements OnMapReadyCallback*/ {

    protected static final String TAG = "MapFragment";
    public static final LatLng BELFAST = new LatLng(54.5970, -5.9300);
    public static  Bitmap car_full_bitmap;
    public static Bitmap car_half_bitmap;
    protected GoogleMap mMap;
    private Handler mHandler;
    private Activity mActivity;
    private Runnable mRunnable;
    private Marker delorean;

    public static final long UPDATE_INTERVAL = 1000;
    public static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;

    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected LocationRequest mLocationRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Map onCreate called");
        mActivity = getActivity();
        super.onCreate(savedInstanceState);
    }

    protected synchronized void buildGoogleApiClient() {
        Log.d(TAG, "buildingGoogleApiClient called");

        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
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
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected called");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        delorean.setPosition(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        delorean.setVisible(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 15));

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
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged called: " + mLastLocation.toString());
        delorean.setPosition(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        delorean.setVisible(true);
        Toast.makeText(mActivity, "location updated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.d(TAG, "Map onCreateView called");
        View v = inflater.inflate(R.layout.fragment_map, parent, false);

        mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                .getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(
                new OSMTileProvider(getResources().getAssets())));

        car_full_bitmap = BitmapFactory.decodeResource(
                getResources(), R.drawable.delorean_transparent);
        car_half_bitmap = Bitmap.createScaledBitmap(
                car_full_bitmap, car_full_bitmap.getWidth() / 2, car_full_bitmap.getHeight() / 2, false);

        buildGoogleApiClient();
        mGoogleApiClient.connect();
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startLocationUpdates();
            }
        }, 100);

        delorean = mMap.addMarker(new MarkerOptions()
                .position(BELFAST)
                .title("DeLorean DMC-12")
                .snippet("Roads? Where we're going, we don't need roads.")
                .visible(false)
                .icon(BitmapDescriptorFactory.fromBitmap(car_half_bitmap)));
        //TODO figure out how to set minimum zoom and maximum zoom
        return v;
    }

    @Override
    public void onPause() {
        Log.d(TAG, "Map onPause called");
        mHandler.removeCallbacksAndMessages(null);
        stopLocationUpdates();
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "Map onResume called");
        mHandler.postDelayed(mRunnable, 1000);
        super.onResume();
    }
}
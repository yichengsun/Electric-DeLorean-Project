package com.example.epic.testapplication;
import android.app.Activity;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import android.support.v4.app.Fragment;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * This fragment is created when the user switches to Map View when a trip has not bee started.
 * A google map object will be created and google location services will continuously update the
 * the Delorean marker to show the current location. The map will display all the electric
 * vehicle chargers in belfast
 */
public class MapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = "MapFragment";    //debug tag
    //google maps location update interval in milliseconds
    public static final long UPDATE_INTERVAL = 1000;
    //google maps fastest update interval in milliseconds
    public static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;

    private Marker delorean; //google maps marker for Delorean
    private MapHelper mMapHelper;
    private GoogleMap mMap; //google maps instance
    private Handler mHandler; //
    private Activity mActivity;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    //ImageButton for button that moves map to current location
    private ImageButton imgMyLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Map onCreate called");
        mActivity = getActivity();
        mMapHelper = new MapHelper(mActivity);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.d(TAG, "Map onCreateView called");
        View v = inflater.inflate(R.layout.fragment_map, parent, false);

        imgMyLocation = (ImageButton)v.findViewById(R.id.imgMyLocation);
        imgMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(delorean.getPosition()));
            }
        });

        mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                .getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(
                new OSMTileProvider(getResources().getAssets())));
        mMap.setOnCameraChangeListener(getCameraChangeListener());
        mMap.moveCamera(CameraUpdateFactory.zoomTo(mMapHelper.DEFAULT_ZOOM));

        buildGoogleApiClient();
        mGoogleApiClient.connect();
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startLocationUpdates();
            }
        }, 500);

        delorean = mMap.addMarker(new MarkerOptions()
                .position(mMapHelper.BELFAST)
                .title("DeLorean DMC-12")
                .snippet("Roads? Where we're going, we don't need roads.")
                .visible(false)
                .icon(BitmapDescriptorFactory.fromBitmap(mMapHelper.car_bitmap)));

        mMapHelper.updateMapChargers(mMap);

        return v;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected called");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        delorean.setPosition(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        delorean.setVisible(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(delorean.getPosition()));
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged called: " + mLastLocation.toString());
        delorean.setPosition(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        Toast.makeText(mActivity, "location updated", Toast.LENGTH_SHORT).show();
    }

    public GoogleMap.OnCameraChangeListener getCameraChangeListener() {
        return new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                if (position.zoom > mMapHelper.MAX_ZOOM)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(delorean.getPosition(),
                            mMapHelper.MAX_ZOOM));
                else if (position.zoom < mMapHelper.MIN_ZOOM)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(delorean.getPosition(),
                            mMapHelper.MIN_ZOOM));
            }
        };
    }

    private synchronized void buildGoogleApiClient() {
        Log.d(TAG, "buildingGoogleApiClient called");
        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    private void createLocationRequest() {
        Log.d(TAG, "createLocationRequest called");
        mLocationRequest = new LocationRequest()
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates called");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
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
    public void onPause() {
        Log.d(TAG, "Map onPause called");
        mHandler.removeCallbacksAndMessages(null);
        stopLocationUpdates();
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "Map onResume called");
        super.onResume();
    }
}


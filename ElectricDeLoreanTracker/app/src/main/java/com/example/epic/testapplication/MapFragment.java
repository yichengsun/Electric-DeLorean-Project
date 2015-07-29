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
 * A google map object will be created and google play services will continuously update the
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
    private MapHelper mMapHelper; //MapHelper for google map variables
    private GoogleMap mMap; //google maps instance
    private Activity mActivity; //current context
    private Handler mHandler; //handler to process runnable for delaying startlocationupdates
    private GoogleApiClient mGoogleApiClient; //google api client instance
    private Location mLastLocation; //last known location provided
    private LocationRequest mLocationRequest; //location request instance

    /**
     * Get application context, load bitmaps in MapHelper, and connect to Google Api Client
     * @param savedInstanceState saved instance state of fragment
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Map onCreate called");
        //extends onCreate of parent
        super.onCreate(savedInstanceState);
        //get current context
        mActivity = getActivity();
        //create instance of MapHelper to load bitmaps and map variables
        mMapHelper = new MapHelper(mActivity);
        //create instance of google play services api client
        buildGoogleApiClient();
        //connect to play services and location services api
        mGoogleApiClient.connect();
    }

    /**
     * Initializes google maps with tile overlay and OSMTileProvider. Inflates my location image
     * button and set delorean marker and EV charger icons
     * @param inflater LayoutInflater to inflate view
     * @param parent Viewgroup of parent
     * @param savedInstanceState saved instance state of fragment
     * @return inflated view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.d(TAG, "Map onCreateView called");
        View v = inflater.inflate(R.layout.fragment_map, parent, false);

        //get map from fragment_map.xml
        mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                .getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        //create tile overlay with open street maps
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(
                new OSMTileProvider(getResources().getAssets())));
        //create new OnCameraChangeListener to prevent user from zooming in/out beyond threshold
        mMap.setOnCameraChangeListener(getCameraChangeListener());
        //move camera to default zoom level
        mMap.moveCamera(CameraUpdateFactory.zoomTo(MapHelper.DEFAULT_ZOOM));

        //wait 500 ms before start location updates to ensure google api client has time to connect
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startLocationUpdates();
            }
        }, 500);

        /*set marker with delorean car icon, hidden at belfast city hall by
        default until location has been updated*/
        delorean = mMap.addMarker(new MarkerOptions()
                .position(MapHelper.BELFAST)
                .title("DeLorean DMC-12")
                .snippet("Roads? Where we're going, we don't need roads.")
                .visible(false)
                .icon(BitmapDescriptorFactory.fromBitmap(MapHelper.car_bitmap)));

        //inflate my location button and set onClick to center map on delorean marker
        ImageButton imgMyLocation = (ImageButton) v.findViewById(R.id.imgMyLocation);
        imgMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(delorean.getPosition()));
            }
        });

        //update map with EV charger icons
        mMapHelper.updateMapChargers(mMap);

        return v;
    }

    /**
     * Sets zoom level to MAX_ZOOM or MIN_ZOOM if user goes beyond the set zoom levels in MapHelper
     * @return OnCameraChangeListener called after every user action on map
     */
    public GoogleMap.OnCameraChangeListener getCameraChangeListener() {
        return new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                if (position.zoom > MapHelper.MAX_ZOOM)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(delorean.getPosition(),
                            MapHelper.MAX_ZOOM));
                else if (position.zoom < MapHelper.MIN_ZOOM)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(delorean.getPosition(),
                            MapHelper.MIN_ZOOM));
            }
        };
    }

    /**
     * creates instance of google play services API client, add location services API
     */
    private synchronized void buildGoogleApiClient() {
        Log.d(TAG, "buildingGoogleApiClient called");
        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * Called when Google Api Client is connected. Updates the delorean marker location with
     * last known location and toggles marker visibility to true and then center map on marker.
     * @param connectionHint data on connection
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected called");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        delorean.setPosition(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        delorean.setVisible(true);
        delorean.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(delorean.getPosition()));
    }

    /**
     * set parameters of LocationRequest for the fused location provider
     */
    private void createLocationRequest() {
        Log.d(TAG, "createLocationRequest called");
        mLocationRequest = new LocationRequest()
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * pass LocationRequest to fused location provider to invoke LocationListener.onLocationChanged
     * callback method
     */
    private void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates called");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * On location changed, update position of delorean marker and make a toast
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged called: " + mLastLocation.toString());
        delorean.setPosition(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        Toast.makeText(mActivity, "Location Updated", Toast.LENGTH_SHORT).show();
    }

    /**
     * Stop location updates by invoking FusedLocationApi.removeLocationUpdates
     */
    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /**
     * Reconnects to google api client if connection suspended
     * @param cause cause of connection suspension
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended called");
        mGoogleApiClient.connect();
    }

    /**
     * Prints source of connect failed error
     * @param result result of connection failed
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    /**
     * Pause location updates
     */
    @Override
    public void onPause() {
        Log.d(TAG, "Map onPause called");
        mHandler.removeCallbacksAndMessages(null);
        stopLocationUpdates();
        super.onPause();
    }

    /**
     * Resume location updates
     */
    @Override
    public void onResume() {
        Log.d(TAG, "Map onResume called");
        super.onResume();
    }
}
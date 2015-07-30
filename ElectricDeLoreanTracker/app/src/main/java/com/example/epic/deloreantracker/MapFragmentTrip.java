package com.example.epic.deloreantracker;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import android.support.v4.app.Fragment;
import android.widget.ImageButton;
import java.util.List;

/**
 * Fragment created when user has started a trip and switches to map view. A google map object
 * is created. A delorean marker tracks current location and the path of the previous locations
 * is drawn using a polyline.
 */
public class MapFragmentTrip extends Fragment {
    protected static final String TAG = "MapFragmentTrip";  // tag for debug
    protected PolylineOptions mPolyline;    //parameters for polyline route
    protected GoogleMap mMap;   //google map instance
    protected List<LatLng> mAllLatLng;  //list of all previous DataPoints
    private Handler mHandler;   //handler to process runnable
    private Runnable mRunnable; //runnable to update polyline ever 1 s
    private Activity mActivity; //current context
    private Marker delorean;    //delorean marker
    private MapHelper mMapHelper;   //MapHelper for map variable

    /**
     * Get application context, load bitmaps in MapHelper
     * @param savedInstanceState saved instance state of fragment
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MapTrip onCreate called");
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mMapHelper = new MapHelper(mActivity);
    }

    /**
     * Set google map to open street map tiles. Update delorean marker and polyline of route
     * previously driven as location is updated in PollServices every 1s.
     * @param inflater LayoutInflater
     * @param parent Parent ViewGroup
     * @param savedInstanceState Saved Instance State of fragment
     * @return Inflated view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.d(TAG, "MapTrip onCreateView called");
        View v = inflater.inflate(R.layout.fragment_map, parent, false);

        //inflate image button for my location
        ImageButton imgMyLocation = (ImageButton) v.findViewById(R.id.imgMyLocation);
        imgMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(delorean.getPosition()));
            }
        });

        //get map from fragment_map.xml
        mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        //open street map tile overlay
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(
                new OSMTileProvider(getResources().getAssets())));

        /**first handler to process runnable to delay setting the delorean marker until location
         * update has time to finish
         */
        Handler mHandlerInit = new Handler();
        mHandlerInit.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "first runnable called");

                delorean = mMap.addMarker(new MarkerOptions()
                        .position(MainActivity.mDataDBHelper.getLastLatLng())
                        .title("DeLorean DMC-12")
                        .snippet("Roads? Where we're going, we don't need roads.")
                        .visible(true)
                        .icon(BitmapDescriptorFactory.fromBitmap(MapHelper.car_bitmap)));
                delorean.showInfoWindow();
                //move camera to delorean marker
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(delorean.getPosition(),
                        MapHelper.DEFAULT_ZOOM));

                //set camerachangelistener to prevent user from zooming too far in/out
                mMap.setOnCameraChangeListener(getCameraChangeListener());

                //load EV chargers
                mMapHelper.updateMapChargers(mMap);

                /**second handler processes runnable that updates polyline with new location data
                 *every second
                 */
                mHandler = new Handler();
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "2nd runnable called");
                                //move delorean to new current position
                                delorean.setPosition(MainActivity.mDataDBHelper.getLastLatLng());

                                //get all route datapoints latlngs
                                mAllLatLng = MainActivity.mDataDBHelper.getAllLatLng();
                                //update polyline
                                mPolyline = new PolylineOptions()
                                        .addAll(mAllLatLng)
                                        .width(15)
                                        .color(Color.BLUE)
                                        .geodesic(true)
                                        .zIndex(1);

                                //add polyline to map
                                mMap.addPolyline(mPolyline);

                                //run runnable recursively every 1000 ms
                                mHandler.postDelayed(this, 1000);
                            }
                        });
                    }
                };
                //begins polyline updating
                mHandler.post(mRunnable);
            }
            //delays first runnable by 1000 ms
        }, 1000);

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
}
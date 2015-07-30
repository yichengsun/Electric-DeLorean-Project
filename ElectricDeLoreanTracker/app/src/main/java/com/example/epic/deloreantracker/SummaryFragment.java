package com.example.epic.deloreantracker;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

/**
 *
 */
public class SummaryFragment extends Fragment {
    // TAG for log statements
    private final String TAG = "StatsFragmentSummary";
    // Activity to store context
    private Activity mActivity;
    // TextViews for updatable data fields
    private TextView mStartTimeView;
    private TextView mEndTimeView;
    private TextView mTimeElapsedView;
    private TextView mDistanceTraveledView;
    private TextView mAverageRPMView;
    private TextView mAverageVelocityView;
    private TextView mEnergyUsedView;
    private TextView mAverageMPKWHView;
    private TextView mAveragePowerView;
    // Polyline to display route traveled on map
    private PolylineOptions mPolyline;
    // List of all LatLng positions on route
    protected List<LatLng> mAllLatLng;
    // Image of DeLorean for map
    private Marker delorean;
    // Map to display route
    private GoogleMap mMap;
    // Route number of route being displayed
    private int sel_route;
    private MapHelper mMapHelper;

    /**
     * Retrieves number of selected route from savedInstanceState bundle
     * @param savedInstanceState bundle of saved instance state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mMapHelper = new MapHelper(mActivity);
        //get selected route number from bundle
        Bundle bundle = getArguments();
        sel_route = bundle.getInt("sel_route");
    }

    /**
     * Initializes updatable text views, displays DeLorean and route taken on map, and
     * displays summary statistics for trip
     * @param inflater LayoutInflater to inflate view
     * @param parent ViewGroup to be inflated in
     * @param savedInstanceState Saved instance state
     * @return Newly created View of the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        View v = inflater.inflate(R.layout.fragment_summary, parent, false);

        mStartTimeView = (TextView) v.findViewById(R.id.startSummaryData);
        mEndTimeView = (TextView) v.findViewById(R.id.endSummaryData);
        mTimeElapsedView = (TextView) v.findViewById(R.id.timeSummaryData);
        mDistanceTraveledView = (TextView) v.findViewById(R.id.distanceSummaryData);
        mAverageVelocityView = (TextView) v.findViewById(R.id.velocitySummaryData);
        mAverageRPMView = (TextView) v.findViewById(R.id.rpmSummaryData);
        mEnergyUsedView = (TextView) v.findViewById(R.id.electricityUsedSummaryData);
        mAverageMPKWHView = (TextView) v.findViewById(R.id.efficiencySummaryData);
        mAveragePowerView = (TextView) v.findViewById(R.id.powerSummaryData);

        //initialize open street map overlay
        mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                .getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(
                new OSMTileProvider(getResources().getAssets())));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(MapHelper.DEFAULT_ZOOM));

        //set delorean marker at route end location
        delorean = mMap.addMarker(new MarkerOptions()
                .position(MainActivity.mDataDBHelper.getRouteLastLng(sel_route))
                .title("DeLorean DMC-12")
                .snippet("Roads? Where we're going, we don't need roads.")
                .visible(true)
                .icon(BitmapDescriptorFactory.fromBitmap(MapHelper.car_bitmap)));

        //get all points in route and draw polyline
        mAllLatLng = MainActivity.mDataDBHelper.getRouteAllLatLng(sel_route);
        mPolyline = new PolylineOptions()
                .addAll(mAllLatLng)
                .width(15)
                .color(Color.BLUE)
                .geodesic(true)
                .zIndex(1);
        mMap.addPolyline(mPolyline);

        //zoom out camera to show all of the polyline
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        for (LatLng point : mAllLatLng) {
            b.include(point);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(b.build(), 200, 200, 20));

        //set camera to prevent zooming in/out beyond bounds
        mMap.setOnCameraChangeListener(getCameraChangeListener());
        mMapHelper.updateMapChargers(mMap);
        // Set statistics data fields
        setData(sel_route);
        return v;
    }

    /**
     * Listener to listen for changes in map focus or zoom
     * @return listener
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
     * Obtain end-of-trip summary data from RouteDBHelper and set the relevant TextViews to display
     * thsi information.
     * @param selectedRoute Route number of the trip to be displayed
     */
    public void setData(int selectedRoute){
        HashMap<String, Object> endOfTripData = MainActivity.mRouteDBHelper.provideEndOfTripData(selectedRoute);

        mStartTimeView.setText((String) endOfTripData.get(getString(R.string.hash_map_start)));
        mEndTimeView.setText((String) endOfTripData.get(getString(R.string.hash_map_end)));

        // Convert hours to 00:00:00 form
        double timeInHours = Double.parseDouble(("" + endOfTripData.get(getString(R.string.hash_map_time))));
        int hours = (int) timeInHours;
        double extraTimeInMinutes = (timeInHours - hours) * 60;
        int minutes = (int) extraTimeInMinutes;
        int seconds = (int) ((extraTimeInMinutes - minutes) * 60);
        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        mTimeElapsedView.setText(timeString);

        mDistanceTraveledView.setText(new DecimalFormat("##.##").format(endOfTripData.get(getString(R.string.hash_map_distance))) + " mi.");
        mAverageVelocityView.setText(new DecimalFormat("##.##").format(endOfTripData.get(getString(R.string.hash_map_velocity))) + " mph");
        mAverageRPMView.setText(new DecimalFormat("##.##").format(endOfTripData.get(getString(R.string.hash_map_rpm))) + " rpm");
        mEnergyUsedView.setText(new DecimalFormat("##.##").format(endOfTripData.get(getString(R.string.hash_map_energy))) + " kWh");
        mAverageMPKWHView.setText(new DecimalFormat("##.##").format(endOfTripData.get(getString(R.string.hash_map_efficiency))) + " mpkWh");
        mAveragePowerView.setText(new DecimalFormat("##.##").format(endOfTripData.get(getString(R.string.hash_map_power))) + " kW");
    }
}

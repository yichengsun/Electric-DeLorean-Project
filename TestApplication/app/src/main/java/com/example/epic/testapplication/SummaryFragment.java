package com.example.epic.testapplication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.util.List;

/**
 * Created by henryshangguan on 7/9/15.
 */
public class SummaryFragment extends Fragment {
    private final String TAG = "StatsFragmentSummary";
    private RouteDBHelper mRouteDBHelper;
    private CoordDBHelper mCoordDBHelper;
    private Activity mActivity;

    private TextView mDistanceTraveledView;
    private TextView mAverageVelocityView;
    private TextView mEnergyUsedView;
    private TextView mAverageMPGView;
    private TextView mAverageCPMView;

    private float maxZoom = 16.9f;
    private float minZoom = 10.0f;
    private float defaultZoom = 15f;

    public static Bitmap car_full_bitmap;
    public static Bitmap car_resized_bitmap;

    private PolylineOptions mPolyline;
    protected List<LatLng> mAllLatLng;
    private Marker delorean;
    private GoogleMap mMap;
    private int sel_route;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        mActivity = getActivity();

        //initialize database helpers
        mRouteDBHelper = new RouteDBHelper(mActivity);
        mCoordDBHelper = new CoordDBHelper(mActivity);

        //load car bitmap file and resize
        car_full_bitmap = BitmapFactory.decodeResource(
                getResources(), R.drawable.rear);
        car_resized_bitmap = Bitmap.createScaledBitmap(
                car_full_bitmap, car_full_bitmap.getWidth() / 4, car_full_bitmap.getHeight() / 4, false);

        //get selected route number from bundle
        Bundle bundle = getArguments();
        sel_route = bundle.getInt("sel_route");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        View v = inflater.inflate(R.layout.fragment_summary, parent, false);

        mDistanceTraveledView = (TextView) v.findViewById(R.id.summary_dist_traveled);
        mAverageVelocityView = (TextView) v.findViewById(R.id.summary_avg_velocity);
        mEnergyUsedView = (TextView) v.findViewById(R.id.summary_energy_used);
        mAverageMPGView = (TextView) v.findViewById(R.id.summary_avg_mpg);
        mAverageCPMView = (TextView) v.findViewById(R.id.summary_cpm);

        //initialize open street map overlay
        mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                .getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(
                new OSMTileProvider(getResources().getAssets())));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(defaultZoom));

        //set delorean marker at route end location
        delorean = mMap.addMarker(new MarkerOptions()
                .position(mCoordDBHelper.getRouteLastLng(sel_route))
                .title("DeLorean DMC-12")
                .snippet("Roads? Where we're going, we don't need roads.")
                .visible(true)
                .icon(BitmapDescriptorFactory.fromBitmap(car_resized_bitmap)));

        //get all points in route and draw polyline
        mAllLatLng = mCoordDBHelper.getRouteAllLatLng(sel_route);
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
        return v;
    }

    public GoogleMap.OnCameraChangeListener getCameraChangeListener() {
        return new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                if (position.zoom > maxZoom)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(delorean.getPosition(), maxZoom));
                else if (position.zoom < minZoom)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(delorean.getPosition(), minZoom));
            }
        };
    }

    public void setData(String[] data){
        mDistanceTraveledView.setText("" + getDistanceTraveled());
        mAverageVelocityView.setText("" + getAverageVelocity());
        mEnergyUsedView.setText("" + getEnergyUsed());
        mAverageMPGView.setText("" + getAverageMPG());
        mAverageCPMView.setText("" + getAverageCPM());
    }

    //TODO all of these methods
    public double getDistanceTraveled() {

        return 1.0;
    }

    public double getAverageVelocity() {

        return 2.0;
    }

    public double getEnergyUsed() {

        return 3.0;
    }

    public double getAverageMPG() {

        return 4.0;
    }

    public double getAverageCPM() {

        return 5.0;
    }
}

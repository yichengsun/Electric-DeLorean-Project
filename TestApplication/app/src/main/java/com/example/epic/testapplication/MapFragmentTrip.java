package com.example.epic.testapplication;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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


public class MapFragmentTrip extends Fragment {
    protected static final String TAG = "MapFragmentTrip";
    public static final LatLng BELFAST = new LatLng(54.5970, -5.9300);
    protected PolylineOptions mPolyline;
    protected GoogleMap mMap;
    protected List<LatLng> mAllLatLng;
    protected CoordDBHelper mCoordDBHelper;
    private Handler mHandler;
    private Activity mActivity;
    private Runnable mRunnable;
    private Marker delorean;

    private float maxZoom = 16.9f;
    private float minZoom = 10.0f;
    private float defaultZoom = 15f;

    public static  Bitmap car_full_bitmap;
    public static Bitmap car_resized_bitmap;

    private ImageButton imgMyLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MapTrip onCreate called");
        mActivity = getActivity();

        car_full_bitmap = BitmapFactory.decodeResource(
                getResources(), R.drawable.rear);
        car_resized_bitmap = Bitmap.createScaledBitmap(
                car_full_bitmap, car_full_bitmap.getWidth() / 4, car_full_bitmap.getHeight() / 4, false);

        super.onCreate(savedInstanceState);
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.d(TAG, "MapTrip onCreateView called");
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

        mCoordDBHelper = new CoordDBHelper(getActivity());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCoordDBHelper.getLastLatLng(), defaultZoom));

        delorean = mMap.addMarker(new MarkerOptions()
                .position(mCoordDBHelper.getLastLatLng())
                .title("DeLorean DMC-12")
                .snippet("Roads? Where we're going, we don't need roads.")
                .visible(true)
                .icon(BitmapDescriptorFactory.fromBitmap(car_resized_bitmap)));

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        delorean.setPosition(mCoordDBHelper.getLastLatLng());

                        mAllLatLng = mCoordDBHelper.getAllLatLng();
                        mPolyline = new PolylineOptions()
                                .addAll(mAllLatLng)
                                .width(15)
                                .color(Color.BLUE)
                                .geodesic(true)
                                .zIndex(1);
                        mMap.addPolyline(mPolyline);

                        mHandler.postDelayed(this, 1000);
                    }
                });
            }
        };
        mHandler.post(mRunnable);
        return v;
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    @Override
    public void onResume() {
        mHandler.postDelayed(mRunnable, 1000);
        super.onResume();
    }
}
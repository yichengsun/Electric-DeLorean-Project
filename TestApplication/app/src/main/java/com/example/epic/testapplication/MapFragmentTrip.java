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

    public static Bitmap oos_bitmap;
    public static Bitmap standard_bitmap;
    public static Bitmap fast_bitmap;
    public static Bitmap rapid_bitmap;
    public static  Bitmap car_full_bitmap;
    public static Bitmap car_resized_bitmap;

    private ImageButton imgMyLocation;

    double[][] NI_Rapid_Out_of_Service = new double[][] {
            { 54.846012, -5.807106 }
    };

    double[][] NI_Fast_Out_of_Service = new double[][] {
            { 54.445926, -6.364334 },
            { 55.071775, -6.519416 },
            { 54.844648, -6.273606 },
            { 55.163904, -6.871463 },
            { 55.0375, -6.9537 },
            { 54.99325, -7.319705 },
            { 54.659146, -7.334948 },
            { 54.403634, -5.899707 },
            { 54.59371, -5.839133 },
            { 54.173287, -6.335203 },
            { 54.178594, -6.335694 },
            { 54.9989, -7.32095 },
            { 54.0607, -6.0077 }
    };

    double[][] NI_Rapid_Operational = new double[][] {
            { 55.200943, -6.642507 },
            { 54.995194, -7.306255 },
            { 54.572178, -5.970464 },
            { 54.404049, -5.76861 },
            { 54.54723, -5.938462 },
            { 54.853932, -6.275528 },
            { 54.676914, -7.352932 },
            { 54.359383, -7.632199 },
            { 54.36808, -6.218712 },
            { 54.73022, -6.227475 },
            { 54.62053, -5.86373 },
            { 54.48691, -6.74451 },
            { 54.1917, -6.35143 },
            { 54.847389, -6.727441 }
    };

    double[][] NI_Fast_Operational = new double[][] {
            { 54.8087, -7.47446 },
            { 54.70753, -7.59209 },
            { 54.7144, -7.3766 },
            { 54.647992, -6.742508 },
            { 54.64707, -6.747388 },
            { 54.688089, -6.671085 },
            { 54.451549, -6.393503 },
            { 54.496159, -6.384416 },
            { 54.482015, -6.354253 },
            { 54.4207, -6.4464 },
            { 54.422202, -6.44002 },
            { 54.423635, -6.446591 },
            { 54.5137, -6.0538 },
            { 54.550341, -6.005085 },
            { 54.756443, -6.610751 },
            { 54.836708, -6.683699 },
            { 54.8434, -6.6713 },
            { 54.647352, -5.658459 },
            { 54.600304, -5.912613 },
            { 55.01272, -7.322001 },
            { 54.4171, -7.1484 },
            { 54.33123, -6.277062 },
            { 55.200932, -6.253492 },
            { 55.204278, -6.522941 },
            { 54.994629, -7.323503 },
            { 55.133625, -6.663814 },
            { 55.132465, -6.676898 },
            { 55.2049, -6.6502 },
            { 55.21123, -6.656502 },
            { 54.717053, -5.810334 },
            { 54.864446, -6.284769 },
            { 54.857101, -6.308798 },
            { 54.877617, -6.346985 },
            { 54.8621, -6.2673 },
            { 54.869939, -6.27494 },
            { 54.456951, -7.033401 },
            { 54.5399, -6.7 },
            { 54.502841, -6.769526 },
            { 54.512094, -6.763396 },
            { 54.216337, -5.883547 },
            { 54.582112, -5.979061 },
            { 54.2205, -5.8866 },
            { 55.0709, -6.5013 },
            { 54.968494, -5.950874 },
            { 54.664614, -5.66827 },
            { 55.052944, -6.951432 },
            { 54.751025, -6.323984 },
            { 54.352986, -6.412773 },
            { 55.00322, -6.35912 },
            { 55.063572, -6.494441 },
            { 54.349378, -6.2711 },
            { 54.5774, -5.88767 },
            { 54.64163, -6.73992 },
            { 54.325206, -5.72036 },
            { 54.205899, -5.89405 },
            { 54.37254, -5.555773 },
            { 54.19802, -7.57355 },
            { 54.62047, -6.2147 },
            { 54.37878, -7.31583 },
            { 54.466053, -6.084082 },
            { 54.770986, -6.57608 },
            { 54.793289, -6.785016 },
            { 54.257052, -5.944811 },
            { 54.687176, -5.897521 },
            { 54.99277, -7.313558 },
            { 55.001546, -7.322345 },
            { 54.99548, -7.32185 },
            { 55.025118, -7.337706 },
            { 54.345539, -7.638513 },
            { 54.345051, -7.640047 },
            { 54.343861, -7.633197 },
            { 54.343512, -7.637848 },
            { 54.34741, -7.640307 },
            { 54.252651, -7.442562 },
            { 54.472437, -7.630603 },
            { 54.480109, -8.091973 },
            { 54.850414, -5.818398 },
            { 54.852279, -5.815372 },
            { 54.85092, -5.824385 },
            { 54.993933, -5.98926 },
            { 54.647349, -6.747952 },
            { 54.696594, -5.951624 },
            { 54.661953, -5.930754 },
            { 54.750814, -5.999819 },
            { 54.752969, -5.997569 },
            { 54.66953, -5.95782 },
            { 54.68885, -5.948989 },
            { 54.601591, -7.307725 },
            { 54.4448, -6.6708 },
            { 54.601215, -7.306509 },
            { 54.595862, -7.289644 },
            { 54.604011, -7.298569 },
            { 54.512866, -7.458019 },
            { 54.73014, -6.2305 },
            { 54.717252, -6.210415 },
            { 54.74636, -6.234244 },
            { 54.7176, -6.2192 },
            { 54.712504, -6.218678 },
            { 54.710224, -6.093044 },
            { 54.71464, -6.224314 },
            { 54.396759, -5.758739 },
            { 54.5895, -5.821811 },
            { 54.529472, -5.893496 },
            { 54.0577, -5.99877 },
            { 54.652793, -5.800031 },
            { 54.919368, -6.915843 },
            { 54.95111, -6.555297 },
            { 54.347077, -6.269575 },
            { 54.855201, -5.803055 },
            { 54.6344, -6.7506 },
            { 54.899439, -5.861231 },
            { 55.049487, -6.953681 },
            { 54.749279, -6.44828 },
            { 54.168319, -6.34252 },
            { 54.592703, -5.93343 },
            { 54.604646, -5.931866 },
            { 54.594109, -5.924292 },
            { 54.593365, -5.935574 },
            { 54.594342, -5.928256 },
            { 54.591051, -5.933497 },
            { 54.587893, -5.934377 },
            { 54.595384, -5.915776 },
            { 54.546408, -5.905207 },
            { 54.34671, -6.652213 },
            { 54.34048, -6.651155 },
            { 54.170394, -6.34045 },
            { 54.176301, -6.340184 },
            { 54.175246, -6.338146 },
            { 54.177068, -6.341665 },
            { 54.189322, -6.361695 },
            { 54.331, -5.7139 },
            { 54.7677, -7.2495 },
            { 54.0987, -6.2534 },
            { 54.3821, -5.5476 },
            { 54.3839, -6.2247 },
            { 55.044, -6.9395 },
            { 54.8221, -7.4743 },
            { 54.5045, -6.027934 },
            { 54.214826, -5.890661 },
            { 54.826859, -7.46552 },
            { 54.829324, -7.464149 },
            { 54.824472, -7.461848 }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MapTrip onCreate called");
        mActivity = getActivity();


        oos_bitmap = BitmapFactory.decodeResource(
                getResources(), R.drawable.oos);
        oos_bitmap = Bitmap.createScaledBitmap(oos_bitmap, oos_bitmap.getWidth() * 2,
                oos_bitmap.getHeight() * 2, false);
        rapid_bitmap = BitmapFactory.decodeResource(
                getResources(), R.drawable.rapid);
        rapid_bitmap= Bitmap.createScaledBitmap(rapid_bitmap, rapid_bitmap.getWidth() * 2,
                rapid_bitmap.getHeight() * 2, false);
        standard_bitmap = BitmapFactory.decodeResource(
                getResources(), R.drawable.standard);
        standard_bitmap= Bitmap.createScaledBitmap(standard_bitmap, standard_bitmap.getWidth() * 2,
                standard_bitmap.getHeight() * 2, false);
        fast_bitmap = BitmapFactory.decodeResource(
                getResources(), R.drawable.fast);
        fast_bitmap = Bitmap.createScaledBitmap(fast_bitmap, fast_bitmap.getWidth() * 2,
                fast_bitmap.getHeight() * 2, false);
        car_full_bitmap = BitmapFactory.decodeResource(
                getResources(), R.drawable.rear);
        car_resized_bitmap = Bitmap.createScaledBitmap(
                car_full_bitmap, car_full_bitmap.getWidth() / 5, car_full_bitmap.getHeight() / 5, false);

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

        mCoordDBHelper = new CoordDBHelper(getActivity());

        Handler mHandlerInit = new Handler();
        mHandlerInit.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "first runnable called");
                delorean = mMap.addMarker(new MarkerOptions()
                        .position(mCoordDBHelper.getLastLatLng())
                        .title("DeLorean DMC-12")
                        .snippet("Roads? Where we're going, we don't need roads.")
                        .visible(true)
                        .icon(BitmapDescriptorFactory.fromBitmap(car_resized_bitmap)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(delorean.getPosition(), defaultZoom));

                mMap.setOnCameraChangeListener(getCameraChangeListener());

                for (int i = 0; i < NI_Fast_Out_of_Service.length; i++) {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(NI_Fast_Out_of_Service[i][0], NI_Fast_Out_of_Service[i][1]))
                            .title("NI Fast (Out of Service)")
                            .icon(BitmapDescriptorFactory.fromBitmap(oos_bitmap)));
                }
                for (int i = 0; i < NI_Fast_Operational.length; i++) {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(NI_Fast_Operational[i][0], NI_Fast_Operational[i][1]))
                            .title("NI Fast (Operational)")
                            .icon(BitmapDescriptorFactory.fromBitmap(fast_bitmap)));
                }
                for (int i = 0; i < NI_Rapid_Operational.length; i++) {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(NI_Rapid_Operational[i][0], NI_Rapid_Operational[i][1]))
                            .title("NI Rapid (Operational)")
                            .icon(BitmapDescriptorFactory.fromBitmap(rapid_bitmap)));
                }
                for (int i = 0; i < NI_Rapid_Out_of_Service.length; i++) {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(NI_Rapid_Out_of_Service[i][0], NI_Rapid_Out_of_Service[i][1]))
                            .title("NI Rapid (Out of Service)")
                            .icon(BitmapDescriptorFactory.fromBitmap(oos_bitmap)));
                }

                mHandler = new Handler();
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "2nd runnable called");
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
            }
        }, 1000);

        return v;
    }

    @Override
    public void onPause() {
        Log.d(TAG, "MapTrip onPause called");
        mHandler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "MapTrip onResume called");
        //mHandler.postDelayed(mRunnable, 1000);
        super.onResume();
    }
}
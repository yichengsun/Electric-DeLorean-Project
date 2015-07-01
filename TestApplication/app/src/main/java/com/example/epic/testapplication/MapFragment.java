package com.example.epic.testapplication;
import android.app.Activity;
import android.content.Context;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.support.v4.app.Fragment;

import java.util.List;


public class MapFragment extends Fragment /*implements OnMapReadyCallback*/ {

    protected static final String TAG = "MapFragment";
    public static final LatLng BELFAST = new LatLng(54.5970, -5.9300);
    protected PolylineOptions polyline;
    protected GoogleMap mMap;
    protected List<LatLng> allLatLng;
    protected CoordDBHelper mCoordDBHelper;
    private Handler mHandler;
    private Activity mActivity;
    private Runnable mRunnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Map onCreate called");
        mActivity = getActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.d(TAG, "Map onCreateView called");
        View v = inflater.inflate(R.layout.fragment_map, parent, false);
        mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                .getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        //mMap.setMyLocationEnabled(true);



        final Bitmap car_full_bitmap = BitmapFactory.decodeResource(
                getResources(), R.drawable.delorean_transparent);
        final Bitmap car_half_bitmap = Bitmap.createScaledBitmap(
                car_full_bitmap, car_full_bitmap.getWidth() * 2 / 3, car_full_bitmap.getHeight() * 2 / 3, false);


        mCoordDBHelper = new CoordDBHelper(getActivity());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCoordDBHelper.getLastLatLng(), 16));

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMap.clear();
                        Marker delorean = mMap.addMarker(new MarkerOptions()
                                .position(mCoordDBHelper.getLastLatLng())
                                .title("DeLorean DMC-12")
                                .snippet("Roads? Where we're going, we don't need roads.")
                                .draggable(true)
                                .icon(BitmapDescriptorFactory.fromBitmap(car_half_bitmap)));

                        allLatLng = mCoordDBHelper.getAllLatLng();
                        polyline = new PolylineOptions()
                                .addAll(allLatLng)
                                .width(20)
                                .color(Color.BLUE)
                                .geodesic(false)
                                .zIndex(1);
                        mMap.addPolyline(polyline);


                        mHandler.postDelayed(this, 1000);
                    }
                });
            }
        };
        mHandler.post(mRunnable);

        return v;
    }


//    private void updateUI() {
//        Log.d(TAG, "updateUI called");
//        if (mLastLocation != null) {
//            mLatitudeTextView.setText(String.valueOf(mLastLat));
//            mLongitudeTextView.setText(String.valueOf(mLastLng));
//            mLastUpdateTimeTextView.setText(String.valueOf(mLastUpdateTime));
//
//            mCoordDBHelper.insertCoord(mLastRouteId, mLastLat, mLastLng, mLastAlt);
//            //TODO more textviews for alt,route, etc
//        }
//    }
}
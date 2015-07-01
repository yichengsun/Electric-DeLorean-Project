package com.example.epic.testapplication;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.os.Bundle;


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


public class MapFragment extends Fragment /*implements OnMapReadyCallback*/ {

    protected static final String TAG = "MapFragment";
    public static final LatLng BELFAST = new LatLng(54.5970, -5.9300);
    protected PolylineOptions polyline;
    protected GoogleMap mMap;
    protected LatLng allLatLng;
    protected CoordDBHelper mCoordDBHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Map onCreate called");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.d(TAG, "Map onCreateView called");
        View v = inflater.inflate(R.layout.fragment_map, parent, false);
        mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                .getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.setMyLocationEnabled(true);



        Bitmap car_full_bitmap = BitmapFactory.decodeResource(
                getResources(), R.drawable.delorean_transparent);
        Bitmap car_half_bitmap = Bitmap.createScaledBitmap(
                car_full_bitmap, car_full_bitmap.getWidth() * 2 / 3, car_full_bitmap.getHeight() * 2 / 3, false);


        Marker delorean = mMap.addMarker(new MarkerOptions()
                .position(BELFAST)
                .title("DeLorean DMC-12")
                .snippet("Roads? Where we're going, we don't need roads.")
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromBitmap(car_half_bitmap)));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(BELFAST, 16));

//        polyline = new PolylineOptions()
//                .add(BELFAST, new LatLng(55, -6))
//                .width(25)
//                .color(Color.BLUE)
//                .geodesic(true)
//                .zIndex(1);
//
//        mCoordDBHelper = new CoordDBHelper(getActivity());
//        allLatLng = mCoordDBHelper.getAllLatLng();
//
//        mMap.addPolyline(polyline);

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
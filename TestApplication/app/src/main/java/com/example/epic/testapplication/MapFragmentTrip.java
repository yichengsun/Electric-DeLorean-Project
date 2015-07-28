package com.example.epic.testapplication;
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

public class MapFragmentTrip extends Fragment {
    protected static final String TAG = "MapFragmentTrip";
    protected PolylineOptions mPolyline;
    protected GoogleMap mMap;
    protected List<LatLng> mAllLatLng;
    protected DataDBHelper mDataPointDBHelper;
    private Handler mHandler;
    private Activity mActivity;
    private Runnable mRunnable;
    private Marker delorean;

    private ImageButton imgMyLocation;
    private MapHelper mMapHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MapTrip onCreate called");
        mActivity = getActivity();
        mMapHelper = new MapHelper(mActivity);

        super.onCreate(savedInstanceState);
    }

    public GoogleMap.OnCameraChangeListener getCameraChangeListener() {
        return new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                if (position.zoom > mMapHelper.MAX_ZOOM)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(delorean.getPosition(), mMapHelper.MAX_ZOOM));
                else if (position.zoom < mMapHelper.MIN_ZOOM)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(delorean.getPosition(), mMapHelper.MIN_ZOOM));
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

        mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(
                new OSMTileProvider(getResources().getAssets())));

        mDataPointDBHelper = new DataDBHelper(getActivity());

        Handler mHandlerInit = new Handler();
        mHandlerInit.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "first runnable called");
                delorean = mMap.addMarker(new MarkerOptions()
                        .position(mDataPointDBHelper.getLastLatLng())
                        .title("DeLorean DMC-12")
                        .snippet("Roads? Where we're going, we don't need roads.")
                        .visible(true)
                        .icon(BitmapDescriptorFactory.fromBitmap(mMapHelper.car_bitmap)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(delorean.getPosition(),
                        mMapHelper.DEFAULT_ZOOM));

                mMap.setOnCameraChangeListener(getCameraChangeListener());

                mMapHelper.updateMapChargers(mMap);

                mHandler = new Handler();
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "2nd runnable called");
                                delorean.setPosition(mDataPointDBHelper.getLastLatLng());

                                mAllLatLng = mDataPointDBHelper.getAllLatLng();
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

//    @Override
//    public void onPause() {
//        Log.d(TAG, "MapTrip onPause called");
//        mHandler.removeCallbacksAndMessages(null);
//        super.onPause();
//    }
//
//    @Override
//    public void onResume() {
//        Log.d(TAG, "MapTrip onResume called");
//        //mHandler.postDelayed(mRunnable, 1000);
//        super.onResume();
//    }
}
//package com.example.epic.testapplication;
//import android.location.Location;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
//import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
//import com.google.android.gms.location.LocationListener;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationServices;
//import com.parse.ParseFile;
//import com.parse.ParseObject;
//
//import java.text.DateFormat;
//import java.util.Calendar;
//import java.util.Date;
//
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//public class MapFragment extends Fragment implements
//        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
//
//    protected static final String TAG = "LocationUpdate";
//
//    protected Button mStartUpdatesButton;
//    protected Button mStopUpdatesButton;
//    protected Button mStoreDataButton;
//
//    protected TextView mLastUpdateTimeTextView;
//    protected TextView mLatitudeTextView;
//    protected TextView mLongitudeTextView;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        Log.d(TAG, "onCreate(Bundle) called");
//        super.onCreate(savedInstanceState);
//
//        mCoordDBHelper = new CoordDBHelper(getActivity());
//        mRequestingLocationUpdates = false;
//        mLastUpdateTime = "";
//        updateValuesFromBundle(savedInstanceState);
//        buildGoogleApiClient();
//        //TODO ensure google play services APK using isGooglePlayServicesAvailable()
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.fragment_map, parent, false);
//        mStartUpdatesButton = (Button) v.findViewById(R.id.start_updates_button);
//        mStopUpdatesButton = (Button) v.findViewById(R.id.stop_updates_button);
//        mStoreDataButton = (Button)v.findViewById(R.id.store_data_button);
//        mLatitudeTextView = (TextView) v.findViewById(R.id.latitude_text);
//        mLongitudeTextView = (TextView) v.findViewById(R.id.longitude_text);
//        mLastUpdateTimeTextView = (TextView) v.findViewById(R.id.last_update_time_text);
//        return v;
//    }
//
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
//}
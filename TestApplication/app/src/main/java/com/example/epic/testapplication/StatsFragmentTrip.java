package com.example.epic.testapplication;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;

public class StatsFragmentTrip extends Fragment {
    private TextView mBatteryData;
    private TextView mMPGData;
    private TextView mDistanceData;
    private TextView mVelocityData;
    private Handler mHandler;
    private Runnable mRunnable;
    private Activity mActivity;
    private CoordDBHelper mCoordDBHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mCoordDBHelper = new CoordDBHelper(mActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stats_trip, parent, false);

        mBatteryData = (TextView) v.findViewById(R.id.batteryDataTrip);
        mMPGData = (TextView) v.findViewById(R.id.mpgData);
        mDistanceData = (TextView) v.findViewById(R.id.distanceTraveled);
        mVelocityData = (TextView) v.findViewById(R.id.velocityData);

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("statsfragmenttrip", "running");
                        if (!mCoordDBHelper.isEmpty()) {
                            mBatteryData.setText(new DecimalFormat("##.##").format(mCoordDBHelper.getLastBatt()));
                            mDistanceData.setText(new DecimalFormat("##.##").format(mCoordDBHelper.getLastDistTotal()));
                            mMPGData.setText(new DecimalFormat("##.##").format(mCoordDBHelper.getLastMPG()));
                            mVelocityData.setText(new DecimalFormat("##.##").format(mCoordDBHelper.getLastVelocity()));
                        }
                        mHandler.postDelayed(this, 1000);
                    }
                });
            }
        };
        mHandler.postDelayed(mRunnable, 1000);

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

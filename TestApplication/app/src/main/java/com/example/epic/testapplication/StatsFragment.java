package com.example.epic.testapplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;

public class StatsFragment extends Fragment {
    private static final String TAG = "StatsFragment";
    private TextView mPowerData;
    private TextView mVoltageData;
    private TextView mRPMData;
    private TextView mChargeStateData;
    private TextView mAmperageData;
    private Handler mHandler;
    private Runnable mRunnable;
    private Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Stats onCreate called");
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.d(TAG, "Stats onCreateView called");
        View v = inflater.inflate(R.layout.fragment_stats, parent, false);

        // Data from Pi (BMS and motor controller)
        mChargeStateData = (TextView) v.findViewById(R.id.chargeStateData);
        mAmperageData = (TextView) v.findViewById(R.id.amperageData);
        mPowerData = (TextView) v.findViewById(R.id.powerData);
        mVoltageData = (TextView) v.findViewById(R.id.voltageData);
        mRPMData = (TextView) v.findViewById(R.id.rpmData);

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayBatteryData();
                        mHandler.postDelayed(this, 1000);
                    }
                });
            }
        };
        //delay in starting
        mHandler.postDelayed(mRunnable, 2000);

        return v;
    }

    /**
     * Updates all BMS and motor controller related fields
     */
    private void displayBatteryData() {
        mChargeStateData.setText(new DecimalFormat("##").format(MainActivity.getChargeState()));
        mAmperageData.setText(new DecimalFormat("##.##").format(MainActivity.getAmperage()));
        mPowerData.setText(new DecimalFormat("##.##").format(MainActivity.getPower()));
        mVoltageData.setText(new DecimalFormat("##.##").format(MainActivity.getVoltage()));
        mRPMData.setText(new DecimalFormat("####").format(MainActivity.getRPM()));
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


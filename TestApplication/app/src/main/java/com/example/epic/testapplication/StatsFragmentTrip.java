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

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class StatsFragmentTrip extends Fragment {
    private TextView mStartTimeData;
    private TextView mTimeElapsedData;
    private TextView mChargeStateData;
    private TextView mAmperageData;
    private TextView mVelocityData;
    private TextView mMPGData;
    private TextView mEnergyData;
    private TextView mPowerData;
    private TextView mVoltageData;
    private TextView mRPMData;
    private TextView mDistanceData;
    private TextView mDistanceToEmptyData;

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

        mStartTimeData = (TextView) v.findViewById(R.id.startTimeData);
        mStartTimeData.setText(DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis())));
        mTimeElapsedData = (TextView) v.findViewById(R.id.timeElapsedData);

        mChargeStateData = (TextView) v.findViewById(R.id.chargeStateData);
        mAmperageData = (TextView) v.findViewById(R.id.amperageData);
        mPowerData = (TextView) v.findViewById(R.id.powerData);
        mVoltageData = (TextView) v.findViewById(R.id.voltageData);
        mRPMData = (TextView) v.findViewById(R.id.rpmData);

        mVelocityData = (TextView) v.findViewById(R.id.velocityData);
        mMPGData = (TextView) v.findViewById(R.id.mpgData);
        mEnergyData = (TextView) v.findViewById(R.id.energyData);
        mDistanceData = (TextView) v.findViewById(R.id.distanceData);
        mDistanceToEmptyData = (TextView) v.findViewById(R.id.distanceToEmptyData);


        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("statsfragmenttrip", "running");
                        if (!mCoordDBHelper.isEmpty()) {
                            displayBatteryData();
                            displayTripData();
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

    private void displayBatteryData() {
        mChargeStateData.setText(new DecimalFormat("##.##").format(MainActivity.getChargeState()));
        mAmperageData.setText(new DecimalFormat("##.##").format(MainActivity.getAmperage()));
        mPowerData.setText(new DecimalFormat("##.##").format(MainActivity.getPower()));
        mVoltageData.setText(new DecimalFormat("##.##").format(MainActivity.getVoltage()));
        mRPMData.setText(new DecimalFormat("##.##").format(MainActivity.getRPM()));
    }

    private void displayTripData() {
        mDistanceData.setText(new DecimalFormat("##.##").format(mCoordDBHelper.getLastDistTotal()));
        mDistanceToEmptyData.setText(new DecimalFormat("##.##").format(mCoordDBHelper.getLastDistToEmpty()));
        mMPGData.setText(new DecimalFormat("##.##").format(mCoordDBHelper.getLastMPKWH()));
        mEnergyData.setText(new DecimalFormat("##.##").format(mCoordDBHelper.getLastElectricityUsed()));
        mVelocityData.setText(new DecimalFormat("##.##").format(mCoordDBHelper.getLastVelocity()));

        long seconds = (long) mCoordDBHelper.getLastTimeElapsed();
        String timeElapsed = String.format("%02d:%02d:%02d",
                TimeUnit.SECONDS.toHours(seconds), TimeUnit.SECONDS.toMinutes(seconds)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(seconds)),
                seconds - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds)));

        mTimeElapsedData.setText(timeElapsed);
    }
}

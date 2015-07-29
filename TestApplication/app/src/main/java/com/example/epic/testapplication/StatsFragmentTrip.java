package com.example.epic.testapplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class StatsFragmentTrip extends Fragment {
    // TAG for log statements
    private static final String TAG = "StatsFragmentTrip";

    // All updatable data fields in pane
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

    // Handler for mRunnable
    private Handler mHandler;
    // Runnable to continuously update data
    private Runnable mRunnable;
    // Stored for methods that require a context
    private Activity mActivity;

    // Refresh rate for data
    private int REFRESH_RATE_MILLISECONDS = 1000;

    /**
     * Creates fragment from savedInstanceState
     * @param savedInstanceState the saved instance state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    /**
     * Initializes all text views and starts runnable which regularly refreshes textviews
     * @param inflater LayoutInflater used to inflate view
     * @param parent ViewGroup this view will be in
     * @param savedInstanceState the saved instance state
     * @return View of fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stats_trip, parent, false);

        // Time related data
        mStartTimeData = (TextView) v.findViewById(R.id.startTimeData);
        mStartTimeData.setText(DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis())));
        mTimeElapsedData = (TextView) v.findViewById(R.id.timeElapsedData);

        // Data from Pi (BMS and motor controller)
        mChargeStateData = (TextView) v.findViewById(R.id.chargeStateData);
        mAmperageData = (TextView) v.findViewById(R.id.amperageData);
        mPowerData = (TextView) v.findViewById(R.id.powerData);
        mVoltageData = (TextView) v.findViewById(R.id.voltageData);
        mRPMData = (TextView) v.findViewById(R.id.rpmData);

        // Data from PollService (location and trip calculations)
        mVelocityData = (TextView) v.findViewById(R.id.velocityData);
        mMPGData = (TextView) v.findViewById(R.id.mpgData);
        mEnergyData = (TextView) v.findViewById(R.id.energyData);
        mDistanceData = (TextView) v.findViewById(R.id.distanceData);
        mDistanceToEmptyData = (TextView) v.findViewById(R.id.distanceToEmptyData);

        // Update display every second with most recent data
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!MainActivity.mDataDBHelper.isEmpty()) {
                            displayBatteryData();
                            displayTripData();
                        }
                        mHandler.postDelayed(this, REFRESH_RATE_MILLISECONDS);
                    }
                });
            }
        };
        mHandler.postDelayed(mRunnable, 1000); // 1000 milisecond delay while data enters into database

        return v;
    }

    /**
     * Stops runnable when fragment is paused
     */
    @Override
    public void onPause() {
        mHandler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    /**
     * Restarts runnable when fragment is resumed
     */
    @Override
    public void onResume() {
        mHandler.postDelayed(mRunnable, 1000); // 1000 milisecond delay to allow data entry into database
        super.onResume();
    }

    /**
     * Updates all BMS and motor controller related fields
     */
    private void displayBatteryData() {
        double chargeState = MainActivity.getChargeState();
        double chargeStatePercentage = chargeState / 5.0;
        mChargeStateData.setText(new DecimalFormat("##").format(chargeStatePercentage) + "%");
        mAmperageData.setText(new DecimalFormat("##.##").format(MainActivity.getAmperage()));
        mPowerData.setText(new DecimalFormat("##.##").format(MainActivity.getPower()));
        mVoltageData.setText(new DecimalFormat("##.##").format(MainActivity.getVoltage()));
        mRPMData.setText(new DecimalFormat("##.##").format(MainActivity.getRPM()));
    }

    /**
     * Updates all location and trip calculation related fields
     */
    private void displayTripData() {
        mDistanceData.setText(new DecimalFormat("##.##").format(MainActivity.mDataDBHelper.getLastDistTotal()) + " mi.");
        mDistanceToEmptyData.setText(new DecimalFormat("##.##").format(MainActivity.mDataDBHelper.getLastDistToEmpty()) + " mi.");
        mMPGData.setText(new DecimalFormat("##.##").format(MainActivity.mDataDBHelper.getLastMPKWH()) + " MPKwh");
        mEnergyData.setText(new DecimalFormat("##.##").format(MainActivity.mDataDBHelper.getLastElectricityUsed()) + " Kwh");
        mVelocityData.setText(new DecimalFormat("##.##").format(MainActivity.mDataDBHelper.getLastVelocity()) + " MPH");

        double timeInHours = (double) MainActivity.mDataDBHelper.getLastTimeElapsed();
        int hours = (int) timeInHours;
        double extraTimeInMinutes = (timeInHours - hours) * 60;
        int minutes = (int) extraTimeInMinutes;
        int seconds = (int) ((extraTimeInMinutes - minutes) * 60);

        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        mTimeElapsedData.setText(timeString);
    }
}

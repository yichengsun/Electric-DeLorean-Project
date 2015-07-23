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

public class StatsFragment extends Fragment {
    private static final String TAG = "StatsFragment";
    private TextView mBatteryData;
    private TextView mDistToEmptyData;
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

        mBatteryData = (TextView) v.findViewById(R.id.batteryData);
        mDistToEmptyData = (TextView) v.findViewById(R.id.distToEmpty);

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBatteryData.setText("" + MainActivity.getBatteryLevel());
                        mDistToEmptyData.setText("TODO");
                        mHandler.postDelayed(this, 1000);
                    }
                });
            }
        };
        //delay in starting
        mHandler.postDelayed(mRunnable, 2000);

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


package com.example.epic.testapplication;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by henryshangguan on 7/9/15.
 */
public class StatsFragmentSummary extends Fragment {
    private final String TAG = "StatsFragmentSummary";
    private TextView mRouteNumView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        View v = inflater.inflate(R.layout.fragment_stats_summary, parent, false);
        mRouteNumView = (TextView) v.findViewById(R.id.summary_route_num);
        return v;
    }

    // TODO THIS METHOD
    public void setData(String[] data){

    }
}

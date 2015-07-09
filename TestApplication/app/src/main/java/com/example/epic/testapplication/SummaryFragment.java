package com.example.epic.testapplication;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by henryshangguan on 7/9/15.
 */
public class SummaryFragment extends Fragment {
    private final String TAG = "StatsFragmentSummary";
    private RouteDBHelper mRouteDBHelper;
    private Activity mActivity;

    private TextView mDistanceTraveledView;
    private TextView mAverageVelocityView;
    private TextView mEnergyUsedView;
    private TextView mAverageMPGView;
    private TextView mAverageCPMView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mRouteDBHelper = new RouteDBHelper(mActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        View v = inflater.inflate(R.layout.fragment_summary, parent, false);

        mDistanceTraveledView = (TextView) v.findViewById(R.id.summary_dist_traveled);
        mAverageVelocityView = (TextView) v.findViewById(R.id.summary_avg_velocity);
        mEnergyUsedView = (TextView) v.findViewById(R.id.summary_energy_used);
        mAverageMPGView = (TextView) v.findViewById(R.id.summary_avg_mpg);
        mAverageCPMView = (TextView) v.findViewById(R.id.summary_cpm);

        return v;
    }

    public void setData(String[] data){
        mDistanceTraveledView.setText("" + getDistanceTraveled());
        mAverageVelocityView.setText("" + getAverageVelocity());
        mEnergyUsedView.setText("" + getEnergyUsed());
        mAverageMPGView.setText("" + getAverageMPG());
        mAverageCPMView.setText("" + getAverageCPM());
    }

    //TODO all of these methods
    public double getDistanceTraveled() {

        return 1.0;
    }

    public double getAverageVelocity() {

        return 2.0;
    }

    public double getEnergyUsed() {

        return 3.0;
    }

    public double getAverageMPG() {

        return 4.0;
    }

    public double getAverageCPM() {

        return 5.0;
    }
}

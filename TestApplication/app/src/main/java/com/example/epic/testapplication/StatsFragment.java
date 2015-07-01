package com.example.epic.testapplication;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.cast.CastRemoteDisplayLocalService;

import java.util.Timer;
import java.util.TimerTask;

public class StatsFragment extends Fragment implements AsyncResponse {
    private TextView mBatteryData;
    private TextView mMPGData;
    private TextView mDistanceData;
    private Handler mHandler;
    private Runnable mRunnable;
    private Integer[] mCurrentData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stats, parent, false);

        mBatteryData = (TextView) v.findViewById(R.id.batteryData);
        mMPGData = (TextView) v.findViewById(R.id.mpgData);
        mDistanceData = (TextView) v.findViewById(R.id.distanceTraveled);

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CalculationsTask calculate = new CalculationsTask(getActivity());
                        calculate.delegate = StatsFragment.this;
                        calculate.execute(4); // any int is fine
                        mHandler.postDelayed(this, 10000);
                    }
                });
            }
        };
        mHandler.postDelayed(mRunnable, 2000);

        return v;
    }

    public void processFinish(Integer[] output) {
        mCurrentData = output;
        mBatteryData.setText("" + mCurrentData[0]);
        mMPGData.setText("" + mCurrentData[1]);
        mDistanceData.setText("" + mCurrentData[2]);
    }
}

    /** Old Code **/

//        mCardView3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                FragmentManager fm = getActivity().getSupportFragmentManager();
//                FragmentTransaction ft = fm.beginTransaction();
//
//                Fragment oldFragment = fm.findFragmentById(R.id.mapFragmentContainer);
//                ft.remove(oldFragment).commit();
//
////                FragmentManager fm = getActivity().getSupportFragmentManager();
////                Fragment oldFragment = fm.findFragmentById(R.id.mapFragmentContainer);
////
////                if(oldFragment != null) {
////                    fm.beginTransaction().remove(oldFragment).commit();
////                }
//
////                setBackgrounds(mCardView3);
////                Fragment newFragment = DetailedStatsFragment.newInstance("world");
////                swapDetail(newFragment);
//            }
//        });
//        return v;
//    }

//    private void swapDetail(Fragment newFragment) {
//        FragmentManager fm = getActivity().getSupportFragmentManager();
//        FragmentTransaction ft = fm.beginTransaction();

//        if (fm.findFragmentById(R.id.mapFragmentContainer) != null) {
//            Fragment oldFragment = fm.findFragmentById(R.id.mapFragmentContainer);
//            ft.remove(oldFragment);
//        }
        //ft.add(R.id.mapFragmentContainer, newFragment).commit();

//        if(fm.getBackStackEntryCount() == 0) {
//            int backstackCount = fm.getBackStackEntryCount();
//            String s = "" + backstackCount;
//            Log.d("map", s);
//            ft.add(R.id.mapFragmentContainer, newFragment);
//            ft.addToBackStack(null);
//            ft.commit();
//        }
//
//        else {
//            int backstackCount = fm.getBackStackEntryCount();
//            String s = "" + backstackCount;
//            Log.d("detail", s);
//            Fragment oldFragment = fm.findFragmentById(R.id.mapFragmentContainer);
//            ft.remove(oldFragment);
//            ft.add(R.id.mapFragmentContainer, newFragment);
//            //ft.replace(R.id.mapFragmentContainer, newFragment);
//            ft.commit();
//        }
//    }
//}

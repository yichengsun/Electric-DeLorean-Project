package com.example.epic.testapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
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

public class StatsFragment extends Fragment {
    private TextView mBatteryData;
    private Handler mHandler;
    private Runnable mRunnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stats, parent, false);

        mBatteryData = (TextView) v.findViewById(R.id.batteryData);
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBatteryData.setText("" + MainActivity.provideData());
                        mHandler.postDelayed(this, 1000);
                    }
                });
            }
        };
        mHandler.post(mRunnable);

        return v;
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

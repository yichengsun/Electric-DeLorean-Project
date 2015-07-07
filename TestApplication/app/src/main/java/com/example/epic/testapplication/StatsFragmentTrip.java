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
                        Cursor cursor = mCoordDBHelper.getAllData();
                        if (cursor.getCount() > 0) {
                            cursor.moveToLast();
                            mBatteryData.setText(new DecimalFormat("##.##").format(cursor.getDouble(8)));
                            mDistanceData.setText(new DecimalFormat("##.##").format(cursor.getDouble(7)));
                            mMPGData.setText(new DecimalFormat("##.##").format(cursor.getDouble(9)));
                            mVelocityData.setText(new DecimalFormat("##.##").format(cursor.getDouble(10)));
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

package com.example.epic.testapplication;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.StateSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseObject;

public class MainActivity extends ActionBarActivity {
    public static PollService mService;
    private boolean mOnTrip;
    private boolean mMapView;
    private long mStartTime;
    private long mEndTime;

    //dummy variable
    private static double mCalculatedBatteryLevel = 100.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Electric DeLorean Tracker");

        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.Fragment fragmentStats = new StatsFragment();
        fm.beginTransaction().add(R.id.mainFragmentContainer, fragmentStats).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = new Intent(MainActivity.this, PollService.class);
        switch (item.getItemId()) {
            case R.id.trip_start:
                bindService(i, mConnection, Context.BIND_AUTO_CREATE);
                mStartTime = System.nanoTime();
                mOnTrip = !mOnTrip;
                if (!mMapView) {
                    StatsFragmentTrip statsFragmentTrip = new StatsFragmentTrip();
                    android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
                    fm.beginTransaction().replace(R.id.mainFragmentContainer, statsFragmentTrip).commit();
                }
                return true;
            case R.id.trip_stop:
                MainActivity.this.unbindService(mConnection);
                mEndTime = System.nanoTime();
                mOnTrip = !mOnTrip;
                if (!mMapView) {
                    StatsFragment fragmentStats = new StatsFragment();
                    android.support.v4.app.FragmentManager fm1 = getSupportFragmentManager();
                    fm1.beginTransaction().replace(R.id.mainFragmentContainer, fragmentStats).commit();
                }
                return true;
            case R.id.view_switch:
                if (!mMapView) {
                    MapFragment fragmentMap = new MapFragment();
                    android.support.v4.app.FragmentManager fm2 = getSupportFragmentManager();
                    fm2.beginTransaction().replace(R.id.mainFragmentContainer, fragmentMap).commit();
                    mMapView = true;
                } else {
                    if (!mOnTrip) {
                        StatsFragment fragmentStats2 = new StatsFragment();
                        android.support.v4.app.FragmentManager fm3 = getSupportFragmentManager();
                        fm3.beginTransaction().replace(R.id.mainFragmentContainer, fragmentStats2).commit();
                    } else {
                        StatsFragmentTrip tripFragmentStats = new StatsFragmentTrip();
                        android.support.v4.app.FragmentManager fm3 = getSupportFragmentManager();
                        fm3.beginTransaction().replace(R.id.mainFragmentContainer, tripFragmentStats).commit();
                    }
                    mMapView = false;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static double[] getBatteryData() {
        mCalculatedBatteryLevel -= 0.01;
        return new double[]{mCalculatedBatteryLevel, 0.0};
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            PollService.LocalBinder binder = (PollService.LocalBinder) service;
            mService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };
}

/**
 * Old Code
 */

//    // Provides data to populate stats screen (dummy data for now)
//    public static int provideData() {
//        CalculationsTask x = new CalculationsTask();
//        x.execute(4);
//        Calendar c = Calendar.getInstance();
//        int seconds = c.get(Calendar.SECOND);
//        return seconds;
//    }

////        // Start service to poll for battery data
////        Intent i = new Intent(this, PollService.class);
////        this.bindService();
//
//        // Two-pane fragment layout
//        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
//        android.support.v4.app.Fragment fragmentMap = fm.findFragmentById(R.id.mapFragmentContainer);
//        android.support.v4.app.Fragment fragmentStats = fm.findFragmentById(R.id.statsFragmentContainer);
//
//        if (fragmentMap == null) {
//            fragmentMap = new MapFragment();
//            fm.beginTransaction().add(R.id.mapFragmentContainer, fragmentMap).commit();
//        }
//
//        if (fragmentStats == null) {
//            fragmentStats = new StatsFragment();
//            fm.beginTransaction().add(R.id.statsFragmentContainer, fragmentStats).commit();
//        }
//
//        // Button to store data
////        mStoreDataButton = (Button)findViewById(R.id.store_data_button);
////        mStoreDataButton.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                // Get trip end time
////                Calendar rightNow = Calendar.getInstance();
////                Date date = rightNow.getTime();
////
////                // Parse file-storing test code
////                byte[] translated = "Hello World!".getBytes();
////                ParseFile stored = new ParseFile("test.txt", translated);
////                stored.saveInBackground();
////
////                ParseObject testFileObject = new ParseObject("TestFileObject");
////                testFileObject.put("testFile", stored);
////                testFileObject.put("time", date);
////                testFileObject.saveInBackground();
////                Toast.makeText(MainActivity.this, R.string.data_saved, Toast.LENGTH_SHORT).show();
////            }
////        });
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//}

//public void tripButtonHandler(View view) {
//    Intent i = new Intent(MainActivity.this, PollService.class);
//    if (!mOnTrip) {
//        bindService(i, mConnection, Context.BIND_AUTO_CREATE);
//        Runnable r = new Runnable() {
//            @Override
//            public void run() {
//                int num = mService.getData();
//                Log.d("test number", "" + num);
//            }
//        };
//        Handler h = new Handler();
//        h.postDelayed(r, 50);
//        mOnTrip = !mOnTrip;
//        mButton.setText("End This Trip");
//    }
//
//    else {
//        MainActivity.this.unbindService(mConnection);
//        mOnTrip = !mOnTrip;
//        mButton.setText("Start New Trip");
//    }
//}

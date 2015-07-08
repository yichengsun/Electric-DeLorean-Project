package com.example.epic.testapplication;

import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseObject;

public class MainActivity extends ActionBarActivity {
    private static final String TAG = "MainActivity";
    public static PollService mService;
    private boolean mOnTrip;
    private boolean mMapView;
    private RouteDBHelper mRouteDBHelper;
    private CoordDBHelper mCoordDBHelper;

    //dummy variable
    private static double mCalculatedBatteryLevel = 100.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Main onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Electric DeLorean Tracker");

        mCoordDBHelper = new CoordDBHelper(this);
        mRouteDBHelper = new RouteDBHelper(this);

        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.Fragment fragmentStats = new StatsFragment();
        fm.beginTransaction().add(R.id.mainFragmentContainer, fragmentStats).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "Main onCreateOptionsMenu called");
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = new Intent(MainActivity.this, PollService.class);
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        switch (item.getItemId()) {
            case R.id.trip_start:
                Log.d(TAG, "Main trip_start called");
                if (!mOnTrip) {
                    bindService(i, mConnection, Context.BIND_AUTO_CREATE);
                    mOnTrip = true;
                    if (!mMapView) {
                        StatsFragmentTrip statsFragmentTrip = new StatsFragmentTrip();
                        fm.beginTransaction().replace(R.id.mainFragmentContainer, statsFragmentTrip).commit();
                    } else {
                        MapFragmentTrip mapFragmentTrip = new MapFragmentTrip();
                        fm.beginTransaction().replace(R.id.mainFragmentContainer, mapFragmentTrip).commit();
                    }
                    findViewById(R.id.trip_stop).setEnabled(true);
                    findViewById(R.id.trip_start).setEnabled(false);
                }
                return true;

            case R.id.trip_stop:
                Log.d(TAG, "Main trip_stop called");
                if (mOnTrip) {
                    mOnTrip = false;
                    showNameRouteDialog();

                    if (!mMapView) {
                        StatsFragment fragmentStats = new StatsFragment();
                        fm.beginTransaction().replace(R.id.mainFragmentContainer, fragmentStats).commit();
                    }
//                else {
//                    MapFragment fragmentMap = new MapFragment();
//                    fm.beginTransaction().replace(R.id.mainFragmentContainer, fragmentMap).commit();
//                }
                    findViewById(R.id.trip_stop).setEnabled(false);
                    findViewById(R.id.trip_start).setEnabled(true);
                }
                return true;

            case R.id.view_switch:
                if (!mMapView && !mOnTrip) {
                    Log.d(TAG, "Main map_fragment called");
                    MapFragment fragmentMap = new MapFragment();
                    fm.beginTransaction().replace(R.id.mainFragmentContainer, fragmentMap).commit();
//                    MapFragmentTrip mapFragmentTrip = new MapFragmentTrip();
//                    fm.beginTransaction().replace(R.id.mainFragmentContainer, mapFragmentTrip).commit();
                    mMapView = true;
                } else if (!mMapView && mOnTrip) {
                    MapFragmentTrip mapFragmentTrip = new MapFragmentTrip();
                    fm.beginTransaction().replace(R.id.mainFragmentContainer, mapFragmentTrip).commit();
                    mMapView = true;
                } else if (mMapView && !mOnTrip) {
                    StatsFragment fragmentStats2 = new StatsFragment();
                    fm.beginTransaction().replace(R.id.mainFragmentContainer, fragmentStats2).commit();
                    mMapView = false;
                } else {
                    StatsFragmentTrip tripFragmentStats = new StatsFragmentTrip();
                    fm.beginTransaction().replace(R.id.mainFragmentContainer, tripFragmentStats).commit();
                    mMapView = false;
                }
                return true;

            case R.id.parse_push:
                Log.d(TAG, "parse push called");
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (DeLoreanApplication.isParseInitialized() && mWifi.isConnected()) {
                    Cursor routeCursor = mRouteDBHelper.getAllData();
                    int count = routeCursor.getCount();
                    if (count > 0) {
                        routeCursor.moveToLast();
                        while (!routeCursor.isBeforeFirst()) {
                            DeLoreanApplication.uploadToParse(--count);
                            routeCursor.moveToPrevious();
                        }
                        routeCursor.close();
                    }
                } else {
                    Toast.makeText(this, "Please connect to wifi", Toast.LENGTH_LONG);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // dummy data for now
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
            Log.d(TAG, "Main onServiceConnected called");
            PollService.LocalBinder binder = (PollService.LocalBinder) service;
            mService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "Main onServiceDisconnected called");
        }
    };

    private void showNameRouteDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        final EditText nameInput = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        nameInput.setLayoutParams(lp);
        alertDialog.setView(nameInput);
        alertDialog.setTitle("Name this route");
        alertDialog.setMessage("Please enter a name for this trip (e.g. Belfast to Princeton)");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mRouteDBHelper.updateName(nameInput.getText().toString(), mCoordDBHelper.getLastRouteId());
                MainActivity.this.unbindService(mConnection);
            }
        });
        alertDialog.show();
    }
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

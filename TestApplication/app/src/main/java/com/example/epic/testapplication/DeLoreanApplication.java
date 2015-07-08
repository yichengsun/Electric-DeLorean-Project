package com.example.epic.testapplication;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseObject;

/**
 * Created by henryshangguan on 7/1/15.
 */
public class DeLoreanApplication extends Application {
    private String TAG = "DeLoreanApplication";
    private static boolean mParseInitialized;
    private RouteDBHelper mRouteDBHelper;
    private CoordDBHelper mCoordDBHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        mRouteDBHelper = new RouteDBHelper(this);
        mCoordDBHelper = new CoordDBHelper(this);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            Log.d("app", "wifi connected");
            initializeParse();
            mParseInitialized = true;
            checkAndUpload();
        }

        LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        boolean network = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        WifiManager mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        boolean wifi = mWifiManager.isWifiEnabled();

        if (!(network && wifi)) {
            Toast.makeText(this, "Ensure Location setting set to 'High Accuracy' and Wifi enabled", Toast.LENGTH_LONG).show();
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                    if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
                        if (!mParseInitialized) {
                            initializeParse();
                            mParseInitialized = true;
                        }
                        checkAndUpload();
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }

    public void initializeParse() {
        Parse.enableLocalDatastore(DeLoreanApplication.this);
        Parse.initialize(DeLoreanApplication.this, "uSrtODrZBDyDwNPUXviACZ2QU3SiMWezzQ9v1Pl9",
                "Ul60j3g3iqTRPAxgWZYGSB85RjPTOZAsaFMtMNhH");
    }

    public void checkAndUpload() {
        Log.d(TAG, "checkAndUpload() called");
        Cursor routeCursor = mRouteDBHelper.getAllData();
        int count = routeCursor.getCount();
        if (count > 0) {
            routeCursor.moveToLast();
            while (!routeCursor.isBeforeFirst()) {
                if (!mRouteDBHelper.isUploaded(--count)) {
                    String jsonFile = mCoordDBHelper.dataToJSON(count);
                    final byte[] translated = jsonFile.getBytes();
                    ParseFile stored = new ParseFile("route.json", translated);
                    stored.saveInBackground();

                    ParseObject DeLoreanRouteObject = new ParseObject("DeLoreanRouteObject");
                    DeLoreanRouteObject.put("File", stored);
                    DeLoreanRouteObject.saveInBackground();
                    mRouteDBHelper.updateUploaded(count);
                    routeCursor.moveToPrevious();
                    Toast.makeText(this, "Route " + count + " saved to parse", Toast.LENGTH_SHORT).show();
                } else {
                    routeCursor.close();
                    break;
                }
            }
        }
    }

    public static boolean isParseInitialized() {
        return mParseInitialized;
    }

}


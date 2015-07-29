package com.example.epic.testapplication;

import android.app.Application;
import android.content.Context;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.widget.Toast;


/**
 *
 */
public class DeLoreanApplication extends Application {
    // TAG for log statements
    private String TAG = "DeLoreanApplication";

    /**
     * On creation of the app, remind user to enable wifi and location tracking if not already enbled
     */
    @Override
    public void onCreate() {
        super.onCreate();

        LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        boolean network = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        boolean wifi = mWifiManager.isWifiEnabled();

        if (!(network && wifi)) {
            Toast.makeText(this, "Ensure Location setting set to 'High Accuracy' and Wifi enabled", Toast.LENGTH_LONG).show();
        }
    }

}


package com.example.epic.testapplication;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import com.parse.Parse;

/**
 * Created by henryshangguan on 7/1/15.
 */
public class DeLoreanApplication extends Application{
    private boolean mParseInitialized;

    @Override
    public void onCreate() {
        super.onCreate();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            //Parse initialization
            Parse.enableLocalDatastore(this);
            Parse.initialize(this, "uSrtODrZBDyDwNPUXviACZ2QU3SiMWezzQ9v1Pl9",
                    "Ul60j3g3iqTRPAxgWZYGSB85RjPTOZAsaFMtMNhH");
        } else {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
            BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final String action = intent.getAction();
                    if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                        if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false) && !mParseInitialized){
                            Parse.enableLocalDatastore(DeLoreanApplication.this);
                            Parse.initialize(DeLoreanApplication.this, "uSrtODrZBDyDwNPUXviACZ2QU3SiMWezzQ9v1Pl9",
                                    "Ul60j3g3iqTRPAxgWZYGSB85RjPTOZAsaFMtMNhH");
                            mParseInitialized = true;
                        }
                    }
                }
            };
            registerReceiver(broadcastReceiver, intentFilter);
        }

    }
}

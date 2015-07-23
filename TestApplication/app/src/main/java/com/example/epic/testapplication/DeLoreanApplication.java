//package com.example.epic.testapplication;
//
//import android.app.Activity;
//import android.app.Application;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.database.Cursor;
//import android.location.LocationManager;
//import android.location.LocationProvider;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.net.wifi.WifiManager;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.parse.Parse;
//import com.parse.ParseFile;
//import com.parse.ParseObject;
//
//import java.util.Set;
//
///**
// * Created by henryshangguan on 7/1/15.
// */
//public class DeLoreanApplication extends Application {
//    private String TAG = "DeLoreanApplication";
////    private static boolean mParseInitialized;
////    private static RouteDBHelper mRouteDBHelper;
////    private static CoordDBHelper mCoordDBHelper;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
////
////        mRouteDBHelper = new RouteDBHelper(this);
////        mCoordDBHelper = new CoordDBHelper(this);
//
////        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
////        NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
////
////        if (mWifi.isConnected()) {
////            Log.d("app", "wifi connected");
////            initializeParse();
////            mParseInitialized = true;
////            checkAndUpload();
////        }
//
//        LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
//        boolean network = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//        WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        boolean wifi = mWifiManager.isWifiEnabled();
//
//        if (!(network && wifi)) {
//            Toast.makeText(this, "Ensure Location setting set to 'High Accuracy' and Wifi enabled", Toast.LENGTH_LONG).show();
//        }
//
////        IntentFilter intentFilter = new IntentFilter();
////        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
////        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
////            @Override
////            public void onReceive(Context context, Intent intent) {
////                final String action = intent.getAction();
////                if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
////                    if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
////                        if (!mParseInitialized) {
////                            initializeParse();
////                            mParseInitialized = true;
////                        }
////                        checkAndUpload();
////                    }
////                }
////            }
////        };
////        registerReceiver(broadcastReceiver, intentFilter);
////    }
//    }
////
////    public void initializeParse() {
////        Parse.enableLocalDatastore(DeLoreanApplication.this);
////        Parse.initialize(DeLoreanApplication.this, "uSrtODrZBDyDwNPUXviACZ2QU3SiMWezzQ9v1Pl9",
////                "Ul60j3g3iqTRPAxgWZYGSB85RjPTOZAsaFMtMNhH");
////    }
//
////    public void checkAndUpload() {
////        Log.d(TAG, "checkAndUpload() called");
////        Cursor routeCursor = mRouteDBHelper.getAllData();
////        int count = routeCursor.getCount();
////        if (count > 0) {
////            routeCursor.moveToLast();
////            while (!routeCursor.isBeforeFirst()) {
////                if (!mRouteDBHelper.isUploaded(--count)) {
////                    uploadToParse(routeCursor.getInt(1));
////                    routeCursor.moveToPrevious();
////                    Toast.makeText(this, "Route " + count + " saved to parse", Toast.LENGTH_SHORT).show();
////                } else {
////                    routeCursor.close();
////                    break;
////                }
////            }
////        }
////    }
//
////    public static boolean isParseInitialized() {
////        return mParseInitialized;
////    }
//
////    public static void uploadToParse(int route){
////        String jsonFile = mCoordDBHelper.dataToJSON(route);
////        final byte[] translated = jsonFile.getBytes();
////        String fileName = mRouteDBHelper.getRowName(route);
////        ParseFile stored = new ParseFile(fileName + ".json", translated);
////        stored.saveInBackground();
////
////        ParseObject DeLoreanRouteObject = new ParseObject("DeLoreanRouteObject");
////        DeLoreanRouteObject.put("File", stored);
////        DeLoreanRouteObject.saveInBackground();
////
////        mRouteDBHelper.setUploaded(route);
////    }
//
////    public void bluetoothSetup() {
////        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
////        if (!mBluetoothAdapter.isEnabled()) {
////            Toast.makeText(this, "Please enable bluetooth", Toast.LENGTH_LONG).show();
////        }
////
////        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
////        // If there are paired devices
////        if (pairedDevices.size() > 0) {
////            // Loop through paired devices
////            for (BluetoothDevice device : pairedDevices) {
////                // Add the name and address to an array adapter to show in a ListView
////                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
////            }
////        }
////    }
//
//}
//

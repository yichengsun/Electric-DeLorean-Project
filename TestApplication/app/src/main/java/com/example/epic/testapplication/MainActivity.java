package com.example.epic.testapplication;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends ActionBarActivity implements android.support.v7.app.ActionBar.OnNavigationListener {
    private static final String TAG = "MainActivity";
    private static final int MAX_NAME_LENGTH = 32;
    public static PollService mService;
    private boolean mOnTrip;
    private boolean mMapView;
    private RouteDBHelper mRouteDBHelper;
    private CoordDBHelper mCoordDBHelper;
    private static String[] mDropdownValues;
    private android.support.v4.app.FragmentManager mFM;
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothDevice mmDevice;
    private BluetoothSocket mmSocket;
    private InputStream mmInputStream;
    private OutputStream mmOutputStream;
    private boolean stopWorker;
    private int readBufferPosition;
    private byte[] readBuffer;
    private int displayedRouteNum;
    private boolean mParseInitialized;

    // Order of data coming in from pi
    public static int MAIN_INDEX_CHARGE_STATE = 0;
    public static int MAIN_INDEX_AMPERAGE = 1;
    public static int MAIN_INDEX_POWER = 2;
    public static int MAIN_INDEX_VOLTAGE = 3;
    public static int MAIN_INDEX_RPM = 4;

    private static double mChargeState = 0.0;
    private static double mAmperage = 0.0;
    private static double mPower = 0.0;
    private static double mVoltage = 0.0;
    private static double mRPM = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Main onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCoordDBHelper = new CoordDBHelper(this);
        mRouteDBHelper = new RouteDBHelper(this);
        mDropdownValues = mRouteDBHelper.getAllRouteNames();

        setActionBar();

        mFM = getSupportFragmentManager();
        android.support.v4.app.Fragment fragmentStats = new StatsFragment();
        mFM.beginTransaction().add(R.id.mainFragmentContainer, fragmentStats).commit();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Main onDestroy called");
        cancelNotification();
        try {
            closeBT();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

/*********** MENU/ACTION BAR METHODS ************/
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
        switch (item.getItemId()) {
            case R.id.trip_start:
                Log.d(TAG, "Main trip_start called");
                Log.d(TAG, mCoordDBHelper.getTableAsString());
                setActionBar();
                createNotification();
                if (!mOnTrip) {
                    bindService(i, mConnection, Context.BIND_AUTO_CREATE);
                    mOnTrip = true;
                    if (!mMapView) {
                        StatsFragmentTrip statsFragmentTrip = new StatsFragmentTrip();
                        mFM.beginTransaction().replace(R.id.mainFragmentContainer, statsFragmentTrip).commit();
                    } else {
                        MapFragmentTrip mapFragmentTrip = new MapFragmentTrip();
                        mFM.beginTransaction().replace(R.id.mainFragmentContainer, mapFragmentTrip).commit();
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
                        mFM.beginTransaction().replace(R.id.mainFragmentContainer, fragmentStats).commit();
                    } else {
                    MapFragment fragmentMap = new MapFragment();
                    mFM.beginTransaction().replace(R.id.mainFragmentContainer, fragmentMap).commit();
                }
                    cancelNotification();
                    findViewById(R.id.trip_stop).setEnabled(false);
                    findViewById(R.id.trip_start).setEnabled(true);
                }
                return true;

            case R.id.trip_delete:

                new AlertDialog.Builder(this)
                        .setTitle("Delete trip")
                        .setMessage("Are you sure you want to delete data for this trip?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mRouteDBHelper.deleteRoute(displayedRouteNum);
                                mCoordDBHelper.deleteDataForRoute(displayedRouteNum);
                                updateDropdownValues(mRouteDBHelper.getAllRouteNames());
                                setActionBar();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;

            case R.id.view_switch:
                if (!mMapView && !mOnTrip) {
                    Log.d(TAG, "Main map_fragment called");
                    MapFragment fragmentMap = new MapFragment();
                    mFM.beginTransaction().replace(R.id.mainFragmentContainer, fragmentMap).commit();
                    mMapView = true;
                } else if (!mMapView && mOnTrip) {
                    MapFragmentTrip mapFragmentTrip = new MapFragmentTrip();
                    mFM.beginTransaction().replace(R.id.mainFragmentContainer, mapFragmentTrip).commit();
                    mMapView = true;
                } else if (mMapView && !mOnTrip) {
                    StatsFragment fragmentStats = new StatsFragment();
                    mFM.beginTransaction().replace(R.id.mainFragmentContainer, fragmentStats).commit();
                    mMapView = false;
                } else {
                    StatsFragmentTrip tripFragmentStats = new StatsFragmentTrip();
                    mFM.beginTransaction().replace(R.id.mainFragmentContainer, tripFragmentStats).commit();
                    mMapView = false;
                }
                return true;

            case R.id.parse_push:
                Log.d(TAG, "parse push called");

                WifiManager mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
                if (!mWifiManager.isWifiEnabled()) {
                    Toast.makeText(this, "Please enable wifi", Toast.LENGTH_LONG);
                }

                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (mWifi.isConnected()) {
                    syncWithParse();
                } else {
                    Toast.makeText(this, "No network connection", Toast.LENGTH_LONG);
                }
                return true;

            case R.id.connect_to_pi:
                try {
                    findBT();
                    openBT();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        if (position != 0) {
            // Dropdown numbering starts at 1 ("Current trip" occupies 0), routeDB numbering starts at 0
            final String routeName = mDropdownValues[position];
            displayedRouteNum = mRouteDBHelper.getRouteNum(routeName);
            Bundle bundle = new Bundle();
            bundle.putInt("sel_route", displayedRouteNum);
            final SummaryFragment summaryFragmentStats = new SummaryFragment();
            summaryFragmentStats.setArguments(bundle);
            findViewById(R.id.view_switch).setEnabled(false);
            findViewById(R.id.trip_delete).setEnabled(true);
            mFM.beginTransaction().replace(R.id.mainFragmentContainer, summaryFragmentStats).commit();
            return true;
        } else {
            if (!mOnTrip) {
                StatsFragment fragmentStats = new StatsFragment();
                mFM.beginTransaction().replace(R.id.mainFragmentContainer, fragmentStats).commit();
            } else {
                findViewById(R.id.view_switch).setEnabled(true);
                StatsFragmentTrip fragmentStatsTrip = new StatsFragmentTrip();
                mFM.beginTransaction().replace(R.id.mainFragmentContainer, fragmentStatsTrip).commit();
            }
            findViewById(R.id.trip_delete).setEnabled(false);
            findViewById(R.id.view_switch).setEnabled(true);
            return false;
        }
    }

    public static void updateDropdownValues(String[] names) {
        mDropdownValues = names;
    }

    public void setActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(actionBar.getThemedContext(), android.R.layout.simple_spinner_item, android.R.id.text1, mDropdownValues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionBar.setListNavigationCallbacks(adapter, this);
    }

/************* SERVICE/TRIP RELATED METHODS *************/

    // Defines callbacks for service binding, passed to bindService()
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
        nameInput.setFilters(new InputFilter[] {new InputFilter.LengthFilter(MAX_NAME_LENGTH)});
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        nameInput.setLayoutParams(lp);
        alertDialog.setView(nameInput);
        alertDialog.setTitle("Name this route");
        alertDialog.setMessage("Please enter a name for this trip (e.g. Queen's - Princeton)");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mRouteDBHelper.updateName(nameInput.getText().toString(), mRouteDBHelper.getLastRouteId());
                updateDropdownValues(mRouteDBHelper.getAllRouteNames());
                MainActivity.this.unbindService(mConnection);
                setActionBar();
            }
        });
        alertDialog.show();
    }


/************* Notification Methods ***********/
    private void createNotification() {
        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notif = new Notification.Builder(this)
                .setContentTitle("DeLorean Tracker")
                .setContentText("Currently recording location and battery data")
                .setSmallIcon(R.mipmap.qub_launcher)
                .setContentIntent(pIntent).build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, notif);
    }

    private void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

/******** PARSE METHODS *******/
    private void syncWithParse() {
        Log.d(TAG, "syncWithParse() called");
        if (!mParseInitialized) {
            Parse.enableLocalDatastore(this);
            Parse.initialize(this, "uSrtODrZBDyDwNPUXviACZ2QU3SiMWezzQ9v1Pl9",
                    "Ul60j3g3iqTRPAxgWZYGSB85RjPTOZAsaFMtMNhH");
            mParseInitialized = true;
            Log.d(TAG, "parse initialization finished");
        }

        Cursor routeCursor = mRouteDBHelper.getUnuploadedRoutes();
        int count = routeCursor.getCount();
        if (count > 0) {
            Log.d(TAG, "count > 0");
            routeCursor.moveToLast();
            Log.d(TAG, "cursor moved to last");
            while (!routeCursor.isBeforeFirst()) {
                int routeNum = routeCursor.getInt(mRouteDBHelper.INDEX_NUM);
                Log.d(TAG, "route num = " + routeNum);
                uploadToParse(routeNum);
                Toast.makeText(this, "'" + routeCursor.getString(mRouteDBHelper.INDEX_NAME) + "' uploaded to Parse", Toast.LENGTH_SHORT).show();
                routeCursor.moveToPrevious();
                Log.d(TAG, "moved to previous");

            }
            routeCursor.close();
            Log.d(TAG, "routeCursor closed");
        } else {
            Toast.makeText(this, "All files uploaded", Toast.LENGTH_LONG).show();
        }
    }

    private void uploadToParse(int routeNum) {
        Log.d(TAG, "uploadToParse() called");
        String jsonFile = mCoordDBHelper.dataToJSON(routeNum);
        Log.d(TAG, "jsonFile finished");
        final byte[] translated = jsonFile.getBytes();
        String fileName = mRouteDBHelper.getRowName(routeNum);
        ParseFile stored = new ParseFile(fileName + ".json", translated);
        stored.saveInBackground();
        Log.d(TAG, "file saved in backbround");

        ParseObject DeLoreanRouteObject = new ParseObject("DeLoreanRouteObject");
        DeLoreanRouteObject.put("File", stored);
        DeLoreanRouteObject.saveInBackground();
        Log.d(TAG, "routeObject saved in background");

        mRouteDBHelper.setUploaded(routeNum);
        Log.d(TAG, "setUploaded called");
    }

/************** BLUETOOTH/BATTERY DATA METHODS *************/
    void findBT()
    {
        Log.d(TAG, "findBT() called");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            Toast.makeText(this, "Device not bluetooth enabled", Toast.LENGTH_LONG).show();
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("DeLorean Pi"))
                {
                    mmDevice = device;
                    Log.d(TAG, "DeLorean Pi found");
                    break;
                }
            }
        }
        Log.d(TAG, "Bluetooth device found");
    }

    void openBT() throws IOException
    {
        if (mmDevice != null) {
            Log.d(TAG, "openBT() called");
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
            Log.d(TAG, "UUID SET");
            mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            Log.d(TAG, "mmSocket Set");
            mmSocket.connect();
            Log.d(TAG, "mmSocket Connected");
            mmOutputStream = mmSocket.getOutputStream();
            Log.d(TAG, "mmOutputStream set");
            mmInputStream = mmSocket.getInputStream();
            Log.d(TAG, "mmInputStream set");
            Toast.makeText(this, "Bluetooth connection opened", Toast.LENGTH_LONG);

            beginListenForData();

            Log.d(TAG, "Bluetooth Opened");
        }
    }
//
    void beginListenForData()
    {
        Log.d(TAG, "beginListenForData() called");
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        Thread workerThread = new Thread(new Runnable() {
            public void run() {
                Log.d(TAG, "workerThread started");
                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    String[] splitData = data.split("_");

                                    mChargeState = Double.parseDouble(splitData[MAIN_INDEX_CHARGE_STATE]);
                                    mAmperage = Double.parseDouble(splitData[MAIN_INDEX_AMPERAGE]);
                                    mPower = Double.parseDouble(splitData[MAIN_INDEX_POWER]);
                                    mVoltage = Double.parseDouble(splitData[MAIN_INDEX_VOLTAGE]);
                                    mRPM = Double.parseDouble(splitData[MAIN_INDEX_RPM]);

                                    readBufferPosition = 0;

//                                    handler.post(new Runnable() {
//                                        public void run() {
//                                            Log.d(TAG, data);
//                                            mChargeState = Double.parseDouble(data);
//
//                                        }
//                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                        Log.d(TAG, "thread stopped");
                    }
                }
            }
        });

        workerThread.start();
    }

    void closeBT() throws IOException
    {
        Log.d(TAG, "closeBT() called");
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
    }

//    void sendData() throws IOException
//    {
//        String msg = myTextbox.getText().toString();
//        msg += "\n";
//        mmOutputStream.write(msg.getBytes());
//        myLabel.setText("Data Sent");
//    }
//

    public static double getChargeState() {
        return mChargeState;
    }

    public static double getAmperage() {
        return mAmperage;
    }

    public static double getPower() {
        return mPower;
    }

    public static double getVoltage() {
        return mVoltage;
    }

    public static double getRPM() {
        return mRPM;
    }

}

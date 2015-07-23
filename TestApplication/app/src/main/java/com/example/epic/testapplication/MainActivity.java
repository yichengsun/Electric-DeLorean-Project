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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
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
    private static double mBatteryLevel = 9999.9;
    private int displayedRouteNum;

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

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    findBT();
                    openBT();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Handler handler = new Handler();
        handler.post(r);
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
        nameInput.setFilters(new InputFilter[] {new InputFilter.LengthFilter(MAX_NAME_LENGTH)});
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        nameInput.setLayoutParams(lp);
        alertDialog.setView(nameInput);
        alertDialog.setTitle("Name this route");
        alertDialog.setMessage("Please enter a name for this trip (e.g. Belfast - Princeton)");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mRouteDBHelper.updateName(nameInput.getText().toString(), mCoordDBHelper.getLastRouteId());
                updateDropdownValues(mRouteDBHelper.getAllRouteNames());
                MainActivity.this.unbindService(mConnection);
                setActionBar();
            }
        });
        alertDialog.show();
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

/************** BLUETOOTH METHODS *************/
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
        //myLabel.setText("Bluetooth Device Found");
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
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            Log.d(TAG, data);
                                            //Update battery level
                                            mBatteryLevel = Double.parseDouble(data);

                                        }
                                    });
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

    public static double getBatteryLevel() {
        return mBatteryLevel;
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

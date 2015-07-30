package com.example.epic.deloreantracker;

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

/**
 *
 */
public class MainActivity extends ActionBarActivity implements android.support.v7.app.ActionBar.OnNavigationListener {
    // TAG for log statements
    private static final String TAG = "MainActivity";

    // Fragment manager to handle which fragment is being displayed
    private android.support.v4.app.FragmentManager mFM;

    // Service which updates location data and trip calculations while on trip
    public static PollService mService;

    // Database helpers to access stored data points and route information
    public static RouteDBHelper mRouteDBHelper;
    public static DataDBHelper mDataDBHelper;

    // Components for bluetooth connection with Raspberry Pi
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mmDevice;
    private BluetoothSocket mmSocket;
    private InputStream mmInputStream;
    private OutputStream mmOutputStream;

    // Buffer to string together all incoming bytes from bluetooth before delimiter
    private byte[] readBuffer;
    // Position in readBuffer
    private int readBufferPosition;
    // Condition for reading and translating incoming bytes from bluetooth connection
    private boolean stopWorker;

    // Indices corresponding to channels of ADC on Raspberry Pi
    private static int MAIN_INDEX_CHARGE_STATE = 4;
    private static int MAIN_INDEX_AMPERAGE = 1;
    private static int MAIN_INDEX_POWER = 3;
    private static int MAIN_INDEX_VOLTAGE = 2;
    private static int MAIN_INDEX_RPM = 0;

    // Doubles to store most recent readings from BMS and motor controller, sent through Pi
    private static double mChargeState = 0.0;
    private static double mAmperage = 0.0;
    private static double mPower = 0.0;
    private static double mVoltage = 0.0;
    private static double mRPM = 0.0;

    // True if currently on a trip ('Start trip' has been pressed)
    private boolean mOnTrip;
    // True if currently viewing the map pane
    private boolean mMapView;
    // True if currently viewing a trip summary pane
    private boolean mSummaryView;
    // True if the Parse connection has been initialized
    private boolean mParseInitialized;

    // Route number of trip whose summary map and data are being displayed
    // (when not viewing current data)
    private int displayedRouteNum;
    // Max character length of route name
    private static final int MAX_NAME_LENGTH = 32;
    // String array to store all route names, used to populate dropdown list of past routes
    private static String[] mDropdownValues;

    /**
     * Initializes database helpers, sets action bar/dropdown menu, commits off-trip stats pane
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Main onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database helpers
        mDataDBHelper = new DataDBHelper(this);
        mRouteDBHelper = new RouteDBHelper(this);

        // Create list of names for dropdown route list
        mDropdownValues = mRouteDBHelper.getAllRouteNames();
        setActionBar();

        // App opens to the off-trip stats fragment by default
        mFM = getSupportFragmentManager();
        android.support.v4.app.Fragment fragmentStats = new StatsFragment();
        mFM.beginTransaction().add(R.id.mainFragmentContainer, fragmentStats).commit();
    }

    /**
     * Closes bluetooth connection and removes notification, if applicable
     */
    @Override
    protected void onDestroy() {
        Log.d(TAG, "Main onDestroy called");
        cancelNotification();

        // Close bluetooth properly
        try {
            closeBT();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

/************************* DROPDOWN MENU/ACTION BAR METHODS *************************/

    /**
     * Inflate the menu items for use in the action bar
     * @param menu menu to be inflated
     * @return successful creation of menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "Main onCreateOptionsMenu called");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handles all button presses from action bar or dropdown menu
     * @param item the menu item pressed
     * @return Successful completion of all methods in switch statement case
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = new Intent(MainActivity.this, PollService.class);

        switch (item.getItemId()) {
            // "Start" button, starts a new trip
            case R.id.trip_start:
                Log.d(TAG, "Main trip_start called");
                if (!mOnTrip) {
                    mOnTrip = true;
                    setActionBar();
                    setButtons();
                    createNotification();
                    // Start instance of PollService
                    bindService(i, mConnection, Context.BIND_AUTO_CREATE);
                    updateView();
                    return true;
                }
                return false;

            // "End" button, ends the current trip
            case R.id.trip_stop:
                Log.d(TAG, "Main trip_stop called");
                if (mOnTrip) {
                    mOnTrip = false;
                    showNameRouteDialog();
                    cancelNotification();
                    setButtons();
                    updateView();
                    return true;
                }
                return false;

            // "Delete" button, deletes the data for the past trip being viewed
            case R.id.trip_delete:
                // Shows alert dialog which confirms that the user wants to delete the data
                new AlertDialog.Builder(this)
                        .setTitle("Delete trip")
                        .setMessage("Are you sure you want to permanently delete data for this trip?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Delete route from route database
                                mRouteDBHelper.deleteRoute(displayedRouteNum);
                                // Delete all data from data point database
                                mDataDBHelper.deleteDataForRoute(displayedRouteNum);
                                // Update list of routes in dropdown menu
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

            // "View Switch" button, toggles between map and stats panes
            case R.id.view_switch:
                if (!mMapView && !mOnTrip) {
                    Log.d(TAG, "Main map_fragment called");
                    mMapView = true;
                    updateView();
                } else if (!mMapView && mOnTrip) {
                    mMapView = true;
                    updateView();
                } else if (mMapView && !mOnTrip) {
                    mMapView = false;
                    updateView();
                } else {
                    mMapView = false;
                    updateView();
                }
                return true;

            // "Upload files" button in extended menu, uploads files to Parse
            case R.id.parse_push:
                Log.d(TAG, "parse push called");

                // Checks to see if wifi is enabled, prompts user to enable if not
                WifiManager mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
                if (!mWifiManager.isWifiEnabled()) {
                    Toast.makeText(this, "Please enable wifi", Toast.LENGTH_LONG);
                }

                // Check that wifi is connected and call syncWithParse() if it is,
                // inform user if it is not
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (mWifi.isConnected()) {
                    syncWithParse();
                } else {
                    Toast.makeText(this, "No network connection", Toast.LENGTH_LONG);
                }
                return true;

            // "Connect to Pi" button, opens bluetooth connection with Pi
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

    /**
     * Handles selection of an item from the dropdown menu of past trips
     * @param position position of selected item in dropdown menu
     * @param id id of selected item
     * @return Whether selected item is a past route (true), or "Current Data" (false)
     */
    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        // Any selection other than the first option, "Current Data"
        if (position != 0) {
            // Get name and number of selected route
            final String routeName = mDropdownValues[position];
            displayedRouteNum = mRouteDBHelper.getRouteNum(routeName);

            // Display proper SummaryFragment for this specific route
            Bundle bundle = new Bundle();
            bundle.putInt("sel_route", displayedRouteNum);
            final SummaryFragment summaryFragmentStats = new SummaryFragment();
            summaryFragmentStats.setArguments(bundle);
            mFM.beginTransaction().replace(R.id.mainFragmentContainer, summaryFragmentStats).commit();

            mSummaryView = true;
            setButtons();
            return true;
        } else {
            mSummaryView = false;
            setButtons();
            updateView();
            return false;
        }
    }

    /**
     * Updates list of past routes which will be used to populate dropdown menu
     * @param names names of all past trips currently stored
     */
    public static void updateDropdownValues(String[] names) {
        mDropdownValues = names;
    }


    /**
     * Populate dropdown menu with names of past routes
     * (also prevents application title from being displayed in order to conserve space)
     */
    private void setActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(actionBar.getThemedContext(),
                android.R.layout.simple_spinner_item, android.R.id.text1, mDropdownValues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionBar.setListNavigationCallbacks(adapter, this);
    }

    /**
     * Make action bar buttons enabled or disabled as necessary
     */
    private void setButtons() {
        if (mOnTrip) {
            findViewById(R.id.trip_stop).setEnabled(true);
            findViewById(R.id.trip_start).setEnabled(false);
        } else {
            findViewById(R.id.trip_stop).setEnabled(false);
            findViewById(R.id.trip_start).setEnabled(true);
        }

        if (mSummaryView) {
            findViewById(R.id.trip_delete).setEnabled(true);
            findViewById(R.id.view_switch).setEnabled(false);
        } else {
            findViewById(R.id.trip_delete).setEnabled(false);
            findViewById(R.id.view_switch).setEnabled(true);
        }
    }

    /**
     *  Update what fragment is being shown:
     *  If not on trip, show off-trip map or stats fragment (consistent with what it is switching from)
     *  If on trip, show on-trip map or stats fragment (consistent with what it is switching from)
     */
    private void updateView() {
        if (!mOnTrip && !mMapView) {
            StatsFragment statsFragment = new StatsFragment();
            mFM.beginTransaction().replace(R.id.mainFragmentContainer, statsFragment).commit();
        } else if (!mOnTrip && mMapView) {
            MapFragment mapFragment = new MapFragment();
            mFM.beginTransaction().replace(R.id.mainFragmentContainer, mapFragment).commit();
        } else if (mOnTrip && !mMapView) {
            StatsFragmentTrip statsFragmentTrip = new StatsFragmentTrip();
            mFM.beginTransaction().replace(R.id.mainFragmentContainer, statsFragmentTrip).commit();
        } else if (mOnTrip && mMapView) {
            MapFragmentTrip mapFragmentTrip = new MapFragmentTrip();
            mFM.beginTransaction().replace(R.id.mainFragmentContainer, mapFragmentTrip).commit();
        }
    }

/************************* BATTERY DATA ACCESSOR METHODS *************************/
    /**
     * Accesses charge state data
     * @return the most recent reading of battery charge state from the BMS
     */
    public static double getChargeState() {
        return mChargeState;
    }

    /**
     * Accesses amperage data
     * @return the most recent reading of instantaneous amperage from the BMS
     */
    public static double getAmperage() {
        return mAmperage;
    }

    /**
     * Accesses power data
     * @return the most recent reading of power from the motor controller
     */
    public static double getPower() {
        return mPower;
    }

    /**
     * Accesses voltage data
     * @return the most recent reading of voltage from the motor controller
     */
    public static double getVoltage() {
        return mVoltage;
    }

    /**
     * Accesses RPM data
     * @return the most recent reading of RPM from the motor controller
     */
    public static double getRPM() {
        return mRPM;
    }

/************************* POLLSERVICE/TRIP RELATED METHODS *************************/

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

    /**
     * Presents alert after ending trip prompting the user to name the trip. Sets this name
     * for the trip in the route database
     */
    private void showNameRouteDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        // EditText to enter route name
        final EditText nameInput = new EditText(this);
        // Limits name length to designated length
        nameInput.setFilters(new InputFilter[] {new InputFilter.LengthFilter(MAX_NAME_LENGTH)});
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        nameInput.setLayoutParams(lp);
        // Configure alert
        alertDialog.setView(nameInput);
        alertDialog.setTitle("Name this route");
        alertDialog.setMessage("Please enter a name for this trip (e.g. Queen's - Princeton)");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mRouteDBHelper.updateName(nameInput.getText().toString(), mRouteDBHelper.getLastRouteId());
                updateDropdownValues(mRouteDBHelper.getAllRouteNames());
                // Unbind PollService (stops data updating)
                MainActivity.this.unbindService(mConnection);
                setActionBar();
            }
        });
        alertDialog.show();
    }

/************************* NOTIFICATION METHODS *************************/
    /**
     * Creates notification that the app is recording data
     */
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

    /**
     * Cancels notification that the app is recording data
     */
    private void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

/************************* PARSE DATABASE METHODS *************************/
    /**
     * Initializes Parse connection if necessary and calls uploadToParse as needed to upload all
     * unuploaded trips to Parse. Notifies      * user when each trip is uploaded or if all have
     * already been uploaded.
     */
    private void syncWithParse() {
        Log.d(TAG, "syncWithParse() called");
        // Parse initialization only happens once per activity lifecycle
        if (!mParseInitialized) {
            Parse.enableLocalDatastore(this);
            Parse.initialize(this, "uSrtODrZBDyDwNPUXviACZ2QU3SiMWezzQ9v1Pl9",
                    "Ul60j3g3iqTRPAxgWZYGSB85RjPTOZAsaFMtMNhH");
            mParseInitialized = true;
            Log.d(TAG, "parse initialization finished");
        }

        Cursor routeCursor = mRouteDBHelper.getUnuploadedRoutes();
        // if there are unuploaded routes, iterate through cursor and upload each one
        if (routeCursor.getCount() > 0) {
            routeCursor.moveToLast();
            while (!routeCursor.isBeforeFirst()) {
                int routeNum = routeCursor.getInt(mRouteDBHelper.INDEX_NUM);
                uploadToParse(routeNum);
                Toast.makeText(this, "'" + routeCursor.getString(mRouteDBHelper.INDEX_NAME) + "' uploaded to Parse", Toast.LENGTH_SHORT).show();
                routeCursor.moveToPrevious();
            }
            routeCursor.close();
        } else {
            Toast.makeText(this, "All files uploaded", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Converts data for the designated trip into a JSON file, uploads that file to Parse
     * with the name of the route as the name of the file. Also marks that trip as "uploaded"
     * in the route database.
     * @param routeNum number of route to be uploaded
     */
    private void uploadToParse(int routeNum) {
        // File creation and store
        String jsonFile = mDataDBHelper.dataToJSON(routeNum);
        final byte[] translated = jsonFile.getBytes();
        String fileName = mRouteDBHelper.getRowName(routeNum);
        ParseFile stored = new ParseFile(fileName + ".json", translated);
        stored.saveInBackground();

        // Parse object creation and store
        ParseObject DeLoreanRouteObject = new ParseObject("DeLoreanRouteObject");
        DeLoreanRouteObject.put("File", stored);
        DeLoreanRouteObject.saveInBackground();

        mRouteDBHelper.updateUploaded(routeNum);
    }

/************************* BLUETOOTH & BATTERY DATA METHODS *************************/
    /**
     * Ensures that device is bluetooth capable and enabled, then searches paired devices and
     * connects with DeLorean Pi, if it is paired
     */
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
                    break;
                }
            }
        }
    }

    /**
     * Opens bluetooth connection with DeLorean Pi
     * @throws IOException exception if bluetooth connection opening fails
     */
    void openBT() throws IOException
    {
        if (mmDevice != null) {
            //Standard SerialPortService ID
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();

            Toast.makeText(this, "Bluetooth connection opened", Toast.LENGTH_LONG);

            beginListenForData();
        }
    }

    /**
     * Listens for incoming data from Pi, processes it, and updates pertinent battery data
     * instance variables
     */
    void beginListenForData()
    {
        Log.d(TAG, "beginListenForData() called");
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character
        // Initial conditions for empty buffer
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        // Thread which continuously reads bytes from input stream and stores them in buffer until
        // the delimiter is read, then uncodes the bytes into a String and extracts the data to
        // update battery data instance variables
        Thread workerThread = new Thread(new Runnable() {
            public void run() {
                Log.d(TAG, "workerThread started");
                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        // There are bytes to be read
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    // Copy all bytes in buffer to encodedBytes
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    // Uncode bytes
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    // Split by delimiter ("_")
                                    String[] splitData = data.split("_");

                                    //Update battery instance variables
                                    mChargeState = Double.parseDouble(splitData[MAIN_INDEX_CHARGE_STATE]);
                                    mAmperage = Double.parseDouble(splitData[MAIN_INDEX_AMPERAGE]);
                                    mPower = Double.parseDouble(splitData[MAIN_INDEX_POWER]);
                                    mVoltage = Double.parseDouble(splitData[MAIN_INDEX_VOLTAGE]);
                                    mRPM = Double.parseDouble(splitData[MAIN_INDEX_RPM]);

                                    readBufferPosition = 0;
                                } else {
                                    // Since not delimiter, add byte to buffer
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        // Condition which stops thread
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    /**
     * Close bluetooth connection and stop listening for data
     * @throws IOException if bluetooth closing is not successful
     */
    void closeBT() throws IOException
    {
        Log.d(TAG, "closeBT() called");
        // Condition that will cause the thread to stop
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
    }

    // In case you want to implement functionality that sends data to Pi
//    void sendData() throws IOException
//    {
//        String msg = myTextbox.getText().toString();
//        msg += "\n";
//        mmOutputStream.write(msg.getBytes());
//        myLabel.setText("Data Sent");
//    }
//
}

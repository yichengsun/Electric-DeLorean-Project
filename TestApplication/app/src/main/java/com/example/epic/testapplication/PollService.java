package com.example.epic.testapplication;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by henryshangguan on 6/22/15.
 */
public class PollService extends Service  {
    //TAG
    private static final String TAG = "PollService";
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        PollService getService() {
            // Return this instance of PollService so clients can call public methods
            return PollService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Runnable r = new Runnable() {
            public void run() {
                Log.i(TAG, "PollService running");
                //TODO continuous data pulling and storing to file
            }
        };

        Thread t = new Thread(r);
        t.start();

        return mBinder;
    }
}

/**
 * Old code
 */

//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d(TAG, "started PollService");
//

//
//        //TODO new thread and polling data
//        return Service.START_STICKY;
//    }
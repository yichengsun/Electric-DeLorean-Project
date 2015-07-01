package com.example.epic.testapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by henryshangguan on 6/25/15.
 */
public class CalculationsTask extends AsyncTask<Integer, Void, Integer[]> {
    public AsyncResponse delegate = null;
    private static final String TAG = "CalculationsTask";
    private Context c;

    public CalculationsTask(Context c) {
        this.c = c;
    }

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "starting CalculationsTask");
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        Log.d(TAG, "reporting back from CalculationsTask");
        super.onProgressUpdate(values);
    }

    protected Integer[] doInBackground(Integer... ints) {
        Log.d(TAG, "doing calculations for CalculationsTask");
        CoordDBHelper mCoordDBHelper = new CoordDBHelper(c);
        //dummy calculations
        // Battery Level
        double x = 7 * Math.random();
        // Avg. MPG
        double y = 7 * Math.random();
        // Distance Traveled
        double z = mCoordDBHelper.getDistance();
        return new Integer[]{(int)x, (int)y, (int)z};
    }

    protected void onPostExecute(Integer[] result) {
        Log.d(TAG, "finished CalculationsTask");
        delegate.processFinish(result);
    }

}

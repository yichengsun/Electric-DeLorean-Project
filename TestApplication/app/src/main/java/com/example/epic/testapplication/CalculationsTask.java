package com.example.epic.testapplication;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by henryshangguan on 6/25/15.
 */
public class CalculationsTask extends AsyncTask<Double, Void, Double[]> {
    public AsyncResponse delegate = null;
    private static final String TAG = "CalculationsTask";
    private Context c;

    public CalculationsTask(Context c) {
        this.c = c;
    }

    @Override
    protected void onPreExecute() {
        Log.v(TAG, "starting CalculationsTask");
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        Log.v(TAG, "reporting back from CalculationsTask");
        super.onProgressUpdate(values);
    }

    protected Double[] doInBackground(Double... doubles) {
        Log.v(TAG, "doing calculations for CalculationsTask");
        CoordDBHelper mCoordDBHelper = new CoordDBHelper(c);
        Cursor cursor = mCoordDBHelper.getAllData();
        cursor.moveToLast();
        //dummy calculations
        // Battery Level
        double x = cursor.getDouble(8);
        // Distance Traveled
        double y = cursor.getDouble(7);
        // Avg. MPG
        double z = cursor.getDouble(9);

        return new Double[]{x, y, z};
    }

    protected void onPostExecute(double[] result) {
        Log.v(TAG, "finished CalculationsTask");
        delegate.processFinish(result);
    }

}

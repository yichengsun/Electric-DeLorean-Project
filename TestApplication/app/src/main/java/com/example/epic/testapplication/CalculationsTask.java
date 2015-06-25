package com.example.epic.testapplication;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by henryshangguan on 6/25/15.
 */
public class CalculationsTask extends AsyncTask<Integer, Void, Integer[]> {
    public AsyncResponse delegate = null;

    private static final String TAG = "CalculationsTask";

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

    protected Integer[] doInBackground(Integer... ints) {
        Log.v(TAG, "doing calculations for CalculationsTask");
        //dummy calculation
        double x = 7 * Math.random();
        double y = 7 * Math.random();
        return new Integer[]{(int)x, (int)y};
    }

    protected void onPostExecute(Integer[] result) {
        Log.v(TAG, "finished CalculationsTask");
        delegate.processFinish(result);
    }
}

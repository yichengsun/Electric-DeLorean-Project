package com.example.epic.testapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.google.android.gms.maps.model.LatLng;
import com.github.filosganga.geogson.gson.GeometryAdapterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
<<<<<<< HEAD
import java.util.List;

=======
>>>>>>> bf066fb3fb6cdb5da72f0b0a14475c6ae9038f62

/**
 * Created by Yicheng on 6/22/2015.
 */
public class CoordDBHelper extends SQLiteOpenHelper{
    private static final String TAG = "CoordDBHelper";

    private static String DBNAME = "coordDB";
    private static int VERSION = 1;

    public static final String COORD_ID = "_id";
    public static final String COORD_ROUTE = "route";
    public static final String COORD_LAT = "lat";
    public static final String COORD_LNG = "lng";
    public static final String COORD_ALT = "alt";
    public static final String COORD_TIME_ELPSD = "time_elapsed";
    public static final String COORD_DIST_DIFF = "distance_interval";
    public static final String COORD_DIST_TOTAL = "cumulative_distance_traveled";
    public static final String COORD_BATT = "battery_level";
    public static final String COORD_MPG = "average_mpg";
    public static final String COORD_VEL = "velocity";
    private static final String TABLE_COORD = "coordinates";
    private static final String[] COLUMNS = {COORD_ID,COORD_ROUTE,COORD_LAT,COORD_LNG,COORD_ALT,
            COORD_TIME_ELPSD,COORD_DIST_DIFF,COORD_DIST_TOTAL,COORD_BATT,COORD_MPG, COORD_VEL};
    private SQLiteDatabase mDB;

    public CoordDBHelper(Context context) {
        super(context, DBNAME, null, VERSION);
        mDB = this.getWritableDatabase();
    }

    //callback invoked when getReadableDatabse()/getWritableDatabase() is called
    //provided the database does not exist
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Coord onCreate called");
        //TODO add timestamp
        String sql = "create table " + TABLE_COORD + " ( " +
                COORD_ID + " integer primary key autoincrement, " +
                COORD_ROUTE + " integer , " +
                COORD_LAT + " real , " +
                COORD_LNG + " real , " +
                COORD_ALT + " real , " +
                COORD_TIME_ELPSD + " real , " +
                COORD_DIST_DIFF + " real , " +
                COORD_DIST_TOTAL + " real ," +
                COORD_BATT + " real , " +
                COORD_MPG + " real, " +
                COORD_VEL + " real " +
                " ) ";
        db.execSQL(sql);
    }

    public long insertCoord(int route, double lat, double lng, double alt, double time, double diff, double dist, double batt, double mpg, double vel) {
        Log.d(TAG, "Coord insert " + route + ", " + lat + ", " + lng  + ", " + alt + ", " + time + ", " + diff + ", " + dist + ", " + batt + ", " + mpg + ", " + vel);
        ContentValues cv = new ContentValues();
        cv.put(COORD_ROUTE, route);
        cv.put(COORD_LAT, lat);
        cv.put(COORD_LNG, lng);
        cv.put(COORD_ALT, alt);
        cv.put(COORD_TIME_ELPSD, time);
        cv.put(COORD_DIST_DIFF, diff);
        cv.put(COORD_DIST_TOTAL, dist);
        cv.put(COORD_BATT, batt);
        cv.put(COORD_MPG, mpg);
        cv.put(COORD_VEL, vel);
        return mDB.insert(TABLE_COORD, null, cv);
    }

    //deletes all coordinates from table
    public int del() {
        Log.d(TAG, "Coord delete called");
        int cnt = mDB.delete(TABLE_COORD, "1", null);
        return cnt;
    }

    //returns all coordinates from table
    public Cursor getAllData() {
        //Log.d(TAG, "Coord getAllData called");
        return mDB.query(TABLE_COORD, COLUMNS, null, null, null, null, null);
    }

    // returns latest cumulative distance info
    public double getDistance() {
        Log.d(TAG, "Coord getDistance called");
        Cursor cursor = getAllData();
        if (cursor.getCount() > 0) {
            cursor.moveToLast();
            return cursor.getDouble(7);
        }
        return 0;
    }

    // Converts database to JSON
    public String dataToJSON(int routeNum) {
        Log.d(TAG, "Coord dataToJSON called");
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GeometryAdapterFactory()).create();
        //TODO only convert most recent route
        Cursor cursor = getAllData();
        ArrayList points = new ArrayList<DataPoint>();
        //DataPoint[] points = new DataPoint[cursor.getCount()];
        cursor.moveToLast();

        while (cursor.getInt(1) == routeNum) {
            cursor.moveToPrevious();
        }

        int i = 0;
        cursor.moveToNext();
        while (!cursor.isAfterLast()) {
            DataPoint point = new DataPoint(cursor.getInt(1), cursor.getDouble(2), cursor.getDouble(3),
                    cursor.getDouble(4), cursor.getDouble(5), cursor.getDouble(6), cursor.getDouble(7),
                    cursor.getDouble(8), cursor.getDouble(9), cursor.getDouble(10));
            points.add(point);
            cursor.moveToNext();
        }

        String json = gson.toJson(points);
        return json;
    }
//
//    // Converts route (coordinates only) to geoJSON - not quite functional
//    public String routeToJSON() {
//        Log.d(TAG, "Coord routeToJSON called");
//        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GeometryAdapterFactory()).create();
//
//        Cursor cursor = getAllData();
//        SinglePosition[] points = new SinglePosition[cursor.getCount()];
//        cursor.moveToFirst();
//        int i = 0;
//
//        while (!cursor.isAfterLast()) {
//            Coordinates xy = Coordinates.of(cursor.getDouble(2), cursor.getDouble(3));
//            SinglePosition x = new SinglePosition(xy);
//            points[i++] = x;
//            cursor.moveToNext();
//        }
//
//        String json = gson.toJson(points);
//        del();
//        return json;
//    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //for database upgrades
    }

    public String getTableAsString() {
        Log.d(TAG, "getTableAsString called");
        String tableString = String.format("Table %s:\n", TABLE_COORD);
        Cursor allRows  = mDB.rawQuery("SELECT * FROM " + TABLE_COORD, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }

        return tableString;
    }

    public List<LatLng> getAllLatLng() {
        Log.d(TAG, "getAllLatLng called");
        List latLngArrayList = null;
        Cursor cur = null;

        try {
//        Cursor cur = mDB.query(TABLE_COORD,
//                new String[]{COORD_LAT, COORD_LNG},
//                null, null, null, null, null);
            latLngArrayList = new ArrayList<LatLng>();
            cur = getAllData();
            while (cur.moveToNext()) {
                LatLng latLng = new LatLng(cur.getDouble(2), cur.getDouble(3));
                latLngArrayList.add(latLng);
                Log.d(TAG, latLng.toString());

            }
        } catch (Exception e) {
            Log.e(TAG, "getAllLatLng error: ", e);
        } finally {
            if (cur != null) {
                cur.close();
                Log.d(TAG, "cur!=null");
            }
        }
        return latLngArrayList;
    }

    public LatLng getLastLatLng() {
        Log.d(TAG, "getLastLatLng called");
        Cursor cur = getAllData();
        cur.moveToLast();
        return new LatLng(cur.getDouble(2), cur.getDouble(3));
    }
}

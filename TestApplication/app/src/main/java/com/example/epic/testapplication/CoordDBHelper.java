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
import java.util.List;

/**
 * Created by Yicheng on 6/22/2015.
 */
public class CoordDBHelper extends SQLiteOpenHelper{
    private static final String TAG = "CoordDBHelper";

    private static String DBNAME = "coordDB";
    private static int VERSION = 1;

    public static final String COORD_ID = "_id";
    public static final String COORD_TIMESTAMP = "timestamp";
    public static final String COORD_ROUTE = "route";
    public static final String COORD_LAT = "lat";
    public static final String COORD_LNG = "lng";
    public static final String COORD_TIME_ELPSD = "time_elapsed";
    public static final String COORD_DIST_TOTAL = "cumulative_distance_traveled";
    public static final String COORD_DIST_TO_EMPTY = "distance_to_empty";
    public static final String COORD_MPKWH = "average_mpkwh";
    public static final String COORD_ELECTRICITY_USED = "electricity_used";
    public static final String COORD_VEL = "velocity";
    public static final String COORD_CHARGE_STATE = "charge_state";
    public static final String COORD_AMPERAGE = "amperage";
    public static final String COORD_POWER = "power";
    public static final String COORD_VOLTAGE = "voltage";
    public static final String COORD_RPM = "rpm";



    private static final String TABLE_COORD = "coordinates";
    private static final String[] COLUMNS = {COORD_ID, COORD_TIMESTAMP, COORD_ROUTE, COORD_LAT, COORD_LNG,
            COORD_TIME_ELPSD, COORD_DIST_TOTAL, COORD_DIST_TO_EMPTY, COORD_MPKWH, COORD_ELECTRICITY_USED, COORD_VEL, COORD_CHARGE_STATE, COORD_AMPERAGE, COORD_POWER, COORD_VOLTAGE, COORD_RPM};
    private SQLiteDatabase mDB;

    private final int INDEX_TIMESTAMP = 1;
    private final int INDEX_ROUTE = 2;
    private final int INDEX_LAT = 3;
    private final int INDEX_LNG = 4;
    private final int INDEX_TIME_ELPSD = 5;
    private final int INDEX_DIST_TOTAL = 6;
    private final int INDEX_DIST_TO_EMPTY = 7;
    private final int INDEX_MPKWH = 8;
    private final int INDEX_ELETRICITY_USED = 9;
    private final int INDEX_VEL = 10;
    private final int INDEX_CHARGE_STATE = 11;
    private final int INDEX_AMPERAGE = 12;
    private final int INDEX_POWER = 13;
    private final int INDEX_VOLTAGE = 14;
    private final int INDEX_RPM = 15;

    public CoordDBHelper(Context context) {
        super(context, DBNAME, null, VERSION);
        mDB = this.getWritableDatabase();
    }

    //callback invoked when getReadableDatabase()/getWritableDatabase() is called
    //provided the database does not exist
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Coord onCreate called");
        String sql = "create table " + TABLE_COORD + " ( " +
                COORD_ID + " integer primary key autoincrement, " +
                COORD_TIMESTAMP + " string , " +
                COORD_ROUTE + " integer , " +
                COORD_LAT + " real , " +
                COORD_LNG + " real , " +
                COORD_TIME_ELPSD + " real , " +
                COORD_DIST_TOTAL + " real ," +
                COORD_DIST_TO_EMPTY + " real ," +
                COORD_MPKWH + " real, " +
                COORD_ELECTRICITY_USED + " real, " +
                COORD_VEL + " real, " +
                COORD_CHARGE_STATE + " real, " +
                COORD_AMPERAGE + " real, " +
                COORD_POWER + " real, " +
                COORD_VOLTAGE + " real, " +
                COORD_RPM + " real " +
                " ) ";
        db.execSQL(sql);
    }

    public long insertCoord(String timestamp, int route, double lat, double lng, double time,
                            double dist, double distLeft, double mpkwh, double electricityUsed,
                            double vel, double chargeState, double amperage, double power,
                            double voltage, double rpm) {
        ContentValues cv = new ContentValues();
        cv.put(COORD_TIMESTAMP, timestamp);
        cv.put(COORD_ROUTE, route);
        cv.put(COORD_LAT, lat);
        cv.put(COORD_LNG, lng);
        cv.put(COORD_TIME_ELPSD, time);
        cv.put(COORD_DIST_TOTAL, dist);
        cv.put(COORD_DIST_TO_EMPTY, distLeft);
        cv.put(COORD_MPKWH, mpkwh);
        cv.put(COORD_ELECTRICITY_USED, electricityUsed);
        cv.put(COORD_VEL, vel);
        cv.put(COORD_CHARGE_STATE, chargeState);
        cv.put(COORD_AMPERAGE, amperage);
        cv.put(COORD_POWER, power);
        cv.put(COORD_VOLTAGE, voltage);
        cv.put(COORD_RPM, rpm);
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
        return mDB.query(TABLE_COORD, null, null, null, null, null, null);
    }

    public Cursor getLastEntry() {
        return mDB.query(TABLE_COORD, null, null, null, null, null, COORD_ID + " DESC", "1");
    }

    // returns database
    public SQLiteDatabase getDB() {
        return mDB;
    }

    // Converts database to JSON
    public String dataToJSON(int routeNum) {
        Log.d(TAG, "Coord dataToJSON called");
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GeometryAdapterFactory()).create();

        Log.d(TAG, "gson finished");
        Cursor cursor = mDB.query(TABLE_COORD, null, COORD_ROUTE + "=?", new String[]{"" + routeNum}, null, null, COORD_TIME_ELPSD + " ASC");
        Log.d(TAG, "Cursor created");
        Log.d(TAG, "count = " + cursor.getCount());
        ArrayList points = new ArrayList<DataPoint>();

        cursor.moveToFirst();
        Log.d(TAG, "cursor moved to First");
        while (!cursor.isAfterLast()) {
            DataPoint point = new DataPoint(cursor.getString(INDEX_TIMESTAMP), cursor.getInt(INDEX_ROUTE),
                    cursor.getDouble(INDEX_LAT), cursor.getDouble(INDEX_LNG), cursor.getDouble(INDEX_TIME_ELPSD),
                    cursor.getDouble(INDEX_DIST_TOTAL), cursor.getDouble(INDEX_DIST_TO_EMPTY), cursor.getDouble(INDEX_MPKWH),
                    cursor.getDouble(INDEX_ELETRICITY_USED), cursor.getDouble(INDEX_VEL), cursor.getDouble(INDEX_CHARGE_STATE),
                    cursor.getDouble(INDEX_AMPERAGE), cursor.getDouble(INDEX_POWER),
                    cursor.getDouble(INDEX_VOLTAGE), cursor.getDouble(INDEX_RPM));
            points.add(point);
            Log.d(TAG, "point added");
            cursor.moveToNext();
            Log.d(TAG, "cursor moved to next");
        }

        String json = gson.toJson(points);
        cursor.close();
        Log.d(TAG, "cursor closed");
        return json;
    }

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
        if (allRows != null) {
            allRows.close();
        }
        return tableString;
    }

    public List<LatLng> getAllLatLng() {
        Log.d(TAG, "getAllLatLng called");
        List latLngArrayList = null;
        Cursor cur = null;

        try {
            latLngArrayList = new ArrayList<LatLng>();
            cur = getAllData();
            while (cur.moveToNext()) {
                LatLng latLng = new LatLng(cur.getDouble(INDEX_LAT), cur.getDouble(INDEX_LNG));
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
        Cursor cur = getLastEntry();
        LatLng coord = new LatLng(cur.getDouble(INDEX_LAT), cur.getDouble(INDEX_LNG));
        cur.close();
        return coord;
    }

    public LatLng getRouteLastLng(int sel_route) {
        Log.d(TAG, "getRouteLastLatLng called");
        Cursor cur = getReadableDatabase().query(TABLE_COORD,
                new String[]{COORD_LAT, COORD_LNG},
                COORD_ROUTE + " =?",
                new String[]{String.valueOf(sel_route)},
                null,
                null,
                COORD_TIMESTAMP + " desc",
                null);
        cur.moveToLast();
        LatLng coord = new LatLng(cur.getDouble(cur.getColumnIndex(COORD_LAT)),
                cur.getDouble(cur.getColumnIndex(COORD_LNG)));
        cur.close();
        return coord;
    }

    public List<LatLng> getRouteAllLatLng(int sel_route) {
        Log.d(TAG, "getRouteAllLatLng called");
        List routeLatLngArrayList = new ArrayList<LatLng>();
        Cursor cur = getReadableDatabase().query(TABLE_COORD,
                new String[]{COORD_LAT, COORD_LNG},
                COORD_ROUTE + " =?",
                new String[]{String.valueOf(sel_route)},
                null,
                null,
                COORD_TIMESTAMP + " desc",
                null);
        while(cur.moveToNext()) {
            LatLng latLng = new LatLng(cur.getDouble(cur.getColumnIndex(COORD_LAT)),
                    cur.getDouble(cur.getColumnIndex(COORD_LNG)));
            routeLatLngArrayList.add(latLng);
        }

        cur.close();
        return routeLatLngArrayList;
    }

    public int deleteDataForRoute(int routeNum) {
        Log.d(TAG, "deleteDataForRoute called");
        String[] whereArgs = new String[]{"" + routeNum};
        return mDB.delete(TABLE_COORD, COORD_ROUTE + " = ?", whereArgs);
    }

    public double getLastMPKWH() {
        Cursor cur = getLastEntry();
        if (cur.getCount() > 0) {
            cur.moveToFirst();
            double mpkwh = cur.getDouble(INDEX_MPKWH);
            cur.close();
            return mpkwh;
        }
        cur.close();
        return -1;
    }

    public double getLastDistTotal() {
        Cursor cur = getLastEntry();
        if (cur.getCount() > 0) {
            cur.moveToFirst();
            double mpkwh = cur.getDouble(INDEX_DIST_TOTAL);
            cur.close();
            return mpkwh;
        }
        cur.close();
        return -1;
    }

    public double getLastDistToEmpty() {
        Cursor cur = getLastEntry();
        if (cur.getCount() > 0) {
            cur.moveToFirst();
            double mpkwh = cur.getDouble(INDEX_DIST_TO_EMPTY);
            cur.close();
            return mpkwh;
        }
        cur.close();
        return -1;
    }

    public double getLastVelocity() {
        Cursor cur = getLastEntry();
        if (cur.getCount() > 0) {
            cur.moveToFirst();
            double mpkwh = cur.getDouble(INDEX_VEL);
            cur.close();
            return mpkwh;
        }
        cur.close();
        return -1;
    }

    public double getLastElectricityUsed() {
        Cursor cur = getLastEntry();
        if (cur.getCount() > 0) {
            cur.moveToFirst();
            double mpkwh = cur.getDouble(INDEX_ELETRICITY_USED);
            cur.close();
            return mpkwh;
        }
        cur.close();
        return -1;
    }

    public double getLastTimeElapsed() {
        Cursor cur = getLastEntry();
        if (cur.getCount() > 0) {
            cur.moveToFirst();
            double mpkwh = cur.getDouble(INDEX_TIME_ELPSD);
            cur.close();
            return mpkwh;
        }
        cur.close();
        return -1;
    }

    public boolean isEmpty() {
        Cursor cur = getAllData();
        int count = cur.getCount();
        cur.close();
        if (count > 0)
            return false;
        else
            return true;
    }
}

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
    public static final String COORD_DIST_DIFF = "distance_interval";
    public static final String COORD_DIST_TOTAL = "cumulative_distance_traveled";
    public static final String COORD_BATT = "battery_level";
    public static final String COORD_MPG = "average_mpg";
    public static final String COORD_VEL = "velocity";
    private static final String TABLE_COORD = "coordinates";
    private static final String[] COLUMNS = {COORD_ID,COORD_TIMESTAMP, COORD_ROUTE,COORD_LAT,COORD_LNG,
            COORD_TIME_ELPSD,COORD_DIST_DIFF,COORD_DIST_TOTAL,COORD_BATT,COORD_MPG,COORD_VEL};
    private SQLiteDatabase mDB;

    private final int INDEX_TIMESTAMP = 1;
    private final int INDEX_ROUTE = 2;
    private final int INDEX_LAT = 3;
    private final int INDEX_LNG = 4;
    private final int INDEX_TIME_ELPSD = 5;
    private final int INDEX_DIST_DIFF= 6;
    private final int INDEX_DIST_TOTAL = 7;
    private final int INDEX_BATT = 8;
    private final int INDEX_MPG = 9;
    private final int INDEX_VEL = 10;

    private Cursor mCursor;

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
                COORD_DIST_DIFF + " real , " +
                COORD_DIST_TOTAL + " real ," +
                COORD_BATT + " real , " +
                COORD_MPG + " real, " +
                COORD_VEL + " real " +
                " ) ";
        db.execSQL(sql);
    }

    public long insertCoord(String timestamp, int route, double lat, double lng, double time, double diff, double dist, double batt, double mpg, double vel) {
        Log.d(TAG, "Coord insert " + route + ", " + lat + ", " + lng + ", " + time + ", " + diff + ", " + dist + ", " + batt + ", " + mpg + ", " + vel);
        ContentValues cv = new ContentValues();
        cv.put(COORD_TIMESTAMP, timestamp);
        cv.put(COORD_ROUTE, route);
        cv.put(COORD_LAT, lat);
        cv.put(COORD_LNG, lng);
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

    //TODO FIX THIS
    public Cursor getLastRow() {
        String selectQuery = "SELECT * FROM " + TABLE_COORD + " ORDER BY " + COORD_ROUTE + " DESC, " + COORD_TIME_ELPSD + " DESC LIMIT 1";
        return mDB.rawQuery(selectQuery, null);
    }

    // returns database
    public SQLiteDatabase getDB() {
        return mDB;
    }

    // Converts database to JSON
    public String dataToJSON(int routeNum) {
        Log.d(TAG, "Coord dataToJSON called");
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GeometryAdapterFactory()).create();
        Cursor cursor = getAllData();
        ArrayList points = new ArrayList<DataPoint>();
        cursor.moveToLast();

        while (!cursor.isBeforeFirst() && cursor.getInt(INDEX_ROUTE) >= routeNum) {
            cursor.moveToPrevious();
        }

        int i = 0;
        cursor.moveToNext();
        while (!cursor.isAfterLast() && cursor.getInt(INDEX_ROUTE) == routeNum) {
            DataPoint point = new DataPoint(cursor.getString(INDEX_TIMESTAMP), cursor.getInt(INDEX_ROUTE), cursor.getDouble(INDEX_LAT), cursor.getDouble(INDEX_LNG),
                    cursor.getDouble(INDEX_TIME_ELPSD), cursor.getDouble(INDEX_DIST_DIFF), cursor.getDouble(INDEX_DIST_TOTAL), cursor.getDouble(INDEX_BATT),
                    cursor.getDouble(INDEX_MPG), cursor.getDouble(INDEX_VEL));
            points.add(point);
            cursor.moveToNext();
        }

        String json = gson.toJson(points);
        cursor.close();
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
//        Cursor cur = mDB.query(TABLE_COORD,
//                new String[]{COORD_LAT, COORD_LNG},
//                null, null, null, null, null);
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
        Cursor cur = getAllData();
        cur.moveToLast();
        LatLng coord = new LatLng(cur.getDouble(INDEX_LAT), cur.getDouble(INDEX_LNG));
        cur.close();
        return coord;
    }

    public int getLastRouteId() {
        Cursor cur = getAllData();
        int count = cur.getCount();
        if (count > 0) {
            cur.moveToLast();
            int id = cur.getInt(INDEX_ROUTE);
            cur.close();
            return id;
        }
        cur.close();
        return -1;
    }

    public double getLastBatt() {
        Cursor cur = getAllData();
        int count = cur.getCount();
        if (count > 0) {
            cur.moveToLast();
            double batt = cur.getDouble(INDEX_BATT);
            cur.close();
            return batt;
        }
        cur.close();
        return -1;
    }

    public double getLastMPG() {
        Cursor cur = getAllData();
        int count = cur.getCount();
        if (count > 0) {
            cur.moveToLast();
            double mpg = cur.getDouble(INDEX_MPG);
            cur.close();
            return mpg;
        }
        cur.close();
        return -1;
    }

    public double getLastDistTotal() {
        Cursor cur = getAllData();
        int count = cur.getCount();
        if (count > 0) {
            cur.moveToLast();
            double dist = cur.getDouble(INDEX_DIST_TOTAL);
            cur.close();
            return dist;
        }
        cur.close();
        return -1;
    }

    public double getLastVelocity() {
        Cursor cur = getAllData();
        int count = cur.getCount();
        if (count > 0) {
            cur.moveToLast();
            double vel = cur.getDouble(INDEX_VEL);
            cur.close();
            return vel;
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

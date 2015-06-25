package com.example.epic.testapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
    private static final String TABLE_COORD = "coordinates";
    private static final String[] COLUMNS = {COORD_ID,COORD_ROUTE,COORD_LAT,COORD_LNG,COORD_ALT};
    private SQLiteDatabase mDB;

    public CoordDBHelper(Context context) {
        super(context, DBNAME, null, VERSION);
    }

    //callback invoked when getReadableDatabse()/getWritableDatabase() is called
    //provided the database does not exist
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Coord onCreate called");
        String sql = "create table " + TABLE_COORD + " ( " +
                COORD_ID + " integer primary key autoincrement, " +
                COORD_ROUTE + " integer , " +
                COORD_LAT + " real , " +
                COORD_LNG + " real , " +
                COORD_ALT + " real " +
                " ) ";
        db.execSQL(sql);
    }

    public long insertCoord(int route, double lat, double lng, double alt) {
        Log.d(TAG, "Coord insert called");
        mDB = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COORD_ROUTE, route);
        cv.put(COORD_LAT, lat);
        cv.put(COORD_LNG, lng);
        cv.put(COORD_ALT, alt);
        return mDB.insert(TABLE_COORD, null, cv);
    }

    //deletes all coordinates from table
    public int del() {
        Log.d(TAG, "Coord delete called");
        int cnt = mDB.delete(TABLE_COORD, null, null);
        return cnt;
    }

    //returns all coordinates from table
    public Cursor getAllData() {
        Log.d(TAG, "Coord getAllData called");
        return mDB.query(TABLE_COORD,
                new String[] { COORD_ID,  COORD_ROUTE, COORD_LAT, COORD_LNG, COORD_ALT},
                null, null, null, null, null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //for database upgrades
    }
}

package com.example.epic.testapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;

/**
 * Created by Yicheng on 6/23/2015.
 */
public class RouteDBHelper extends SQLiteOpenHelper {
    private static String DBNAME = "routes.sqlite";
    private static int VERSION = 1;

    public static final String ROUTE_ID = "_id";
    public static final String ROUTE_NUM = "route_number";
    public static final String ROUTE_START_DATE = "start_date";
    public static final String ROUTE_UPLOADED = "uploaded_to_parse";
    public static final String[] COLUMNS = new String[]{ROUTE_ID, ROUTE_NUM, ROUTE_START_DATE, ROUTE_UPLOADED};

    private static final String TABLE_ROUTE = "Routes_Data";

    private SQLiteDatabase mDB;

    public RouteDBHelper(Context context) {
        super(context, DBNAME, null, VERSION);
        mDB = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + TABLE_ROUTE + " ( " +
                ROUTE_ID + " integer primary key autoincrement, " +
                ROUTE_NUM + " integer , " +
                ROUTE_START_DATE + " real , " +
                ROUTE_UPLOADED + " integer " + " ) ";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public long insertRoute(Route route) {
        ContentValues cv = new ContentValues();
        cv.put(ROUTE_NUM, route.getmID());
        cv.put(ROUTE_START_DATE, route.getStartDate().getTime());
        cv.put(ROUTE_UPLOADED, route.getUploaded());
        return getWritableDatabase().insert(TABLE_ROUTE, null, cv);
    }

    //returns all routes from table
    public Cursor getAllData() {
        //Log.d(TAG, "Coord getAllData called");
        return mDB.query(TABLE_ROUTE, COLUMNS, null, null, null, null, null);
    }

    // updates database row to mark as uploaded to parse
    public void updateUploaded(int row) {
        ContentValues cv = new ContentValues();
        cv.put(ROUTE_UPLOADED, 1);
        String sRow = "" + row;
        mDB.update(TABLE_ROUTE, cv, ROUTE_NUM + " = ?", new String[]{sRow});
    }


//    public long insert(ContentValues contentValues) {
//        long rowID = mDB.insert(TABLE_COORD, null, contentValues);
//        return rowID;
//    }

}

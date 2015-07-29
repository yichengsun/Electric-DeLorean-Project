package com.example.epic.testapplication;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 *
 */
public class RouteDBHelper extends SQLiteOpenHelper {
    // TAG for log statements
    private String TAG = "RouteDBHelper";
    private static String DBNAME = "routes.sqlite";
    private static int VERSION = 1;
//todo some of the units
    public static final String ROUTE_ID = "_id";
    public static final String ROUTE_NUM = "route_number";
    public static final String ROUTE_NAME = "route_name";
    public static final String ROUTE_START_DATE = "start_date";
    public static final String ROUTE_END_DATE = "end_date";
    public static final String ROUTE_TIME_ELAPSED_SECONDS = "time_elapsed"; // Length of trip, in seconds
    public static final String ROUTE_AVG_VELOCITY_MPH = "average_velocity"; // Average velocity, in MPH
    public static final String ROUTE_AVG_RPM = "average_rpm";
    public static final String ROUTE_AVG_POWER = "average_power";
    public static final String ROUTE_AVG_EFFICIENCY = "average_efficiency";
    public static final String ROUTE_ELECTRICITY_USED = "electricity_used";
    public static final String ROUTE_TOTAL_DISTANCE = "total_distance";
    public static final String ROUTE_UPLOADED = "uploaded_to_parse";

    public final int INDEX_NUM = 1;
    public final int INDEX_NAME = 2;
    public final int INDEX_START_DATE = 3;
    public final int INDEX_END_DATE = 4;
    public final int INDEX_TIME_ELAPSED = 5;
    public final int INDEX_AVG_VELOCITY = 6;
    public final int INDEX_AVG_RPM = 7;
    public final int INDEX_AVG_POWER = 8;
    public final int INDEX_AVG_EFFICIENCY = 9;
    public final int INDEX_ELECTRICITY_USED = 10;
    public final int INDEX_TOTAL_DISTANCE = 11;
    public final int INDEX_UPLOADED = 12;

    private final Context mContext;


    private static final String TABLE_ROUTE = "Routes_Data";

    private SQLiteDatabase mDB;

    public RouteDBHelper(Context context) {
        super(context, DBNAME, null, VERSION);
        mDB = getWritableDatabase();
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + TABLE_ROUTE + " ( " +
                ROUTE_ID + " integer primary key autoincrement , " +
                ROUTE_NUM + " integer , " +
                ROUTE_NAME + " string , " +
                ROUTE_START_DATE + " string , " +
                ROUTE_END_DATE + " string , " +
                ROUTE_TIME_ELAPSED_SECONDS + " real , " +
                ROUTE_AVG_VELOCITY_MPH + " real , " +
                ROUTE_AVG_RPM + " real , " +
                ROUTE_AVG_POWER + " real , " +
                ROUTE_AVG_EFFICIENCY + " real , " +
                ROUTE_ELECTRICITY_USED + " real , " +
                ROUTE_TOTAL_DISTANCE + " real , " +
                ROUTE_UPLOADED + " integer " + " ) ";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public long insertRoute(Route route) {
        ContentValues cv = new ContentValues();
        cv.put(ROUTE_NUM, route.getmID());
        cv.put(ROUTE_NAME, route.getName());
        cv.put(ROUTE_UPLOADED, route.getUploaded());

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String startDate = sdf.format(route.getStartDate());
        cv.put(ROUTE_START_DATE, startDate);

        return getWritableDatabase().insert(TABLE_ROUTE, null, cv);
    }

    public boolean deleteRoute(int routeNum) {
        return mDB.delete(TABLE_ROUTE, ROUTE_NUM + " = " + routeNum, null) > 0;
    }

    //returns all routes from table
    public Cursor getAllData() {
        //Log.d(TAG, "Coord getAllData called");
        return mDB.query(TABLE_ROUTE, null, null, null, null, null, null);
    }

    public Cursor getLastEntry() {
        return mDB.query(TABLE_ROUTE, null, null, null, null, null, ROUTE_ID + " DESC", "1");
    }

    public Cursor getRowEntry(int routeNum) {
        return mDB.query(TABLE_ROUTE, null, ROUTE_NUM + "=?", new String[]{"" + routeNum}, null, null, null);
    }

    // updates database row to mark as uploaded to parse
    public void updateUploaded(int row) {
        ContentValues cv = new ContentValues();
        cv.put(ROUTE_UPLOADED, 1);
        String sRow = "" + row;
        mDB.update(TABLE_ROUTE, cv, ROUTE_NUM + " = ?", new String[]{sRow});
    }

    // updates route name
    public void updateName(String name, int routeNum) {
        ContentValues cv = new ContentValues();
        cv.put(ROUTE_NAME, name);
        String sRow = "" + routeNum;
        mDB.update(TABLE_ROUTE, cv, ROUTE_NUM + " = ?", new String[]{sRow});
    }

    public void updateEndOfTrip(HashMap<String, Object> endOfTripData, int routeNum) {
        ContentValues cv = new ContentValues();

        double time = (double) endOfTripData.get(mContext.getResources().getString(R.string.hash_map_time));
        cv.put(ROUTE_TIME_ELAPSED_SECONDS, time);

        String endDate = (String) endOfTripData.get(mContext.getResources().getString(R.string.hash_map_end));
        cv.put(ROUTE_END_DATE, endDate);

        double velocityInMPH = (double) endOfTripData.get(mContext.getResources().getString(R.string.hash_map_velocity));
        cv.put(ROUTE_AVG_VELOCITY_MPH, velocityInMPH);

        double rpm = (double) endOfTripData.get(mContext.getResources().getString(R.string.hash_map_rpm));
        cv.put(ROUTE_AVG_RPM, rpm);

        double power = (double) endOfTripData.get(mContext.getResources().getString(R.string.hash_map_power));
        cv.put(ROUTE_AVG_POWER, power);

        double efficiency = (double) endOfTripData.get(mContext.getResources().getString(R.string.hash_map_efficiency));
        cv.put(ROUTE_AVG_EFFICIENCY, efficiency);

        double electricityUsed = (double) endOfTripData.get(mContext.getResources().getString(R.string.hash_map_energy));
        cv.put(ROUTE_ELECTRICITY_USED, electricityUsed);

        double totalDistance = (double) endOfTripData.get(mContext.getResources().getString(R.string.hash_map_distance));
        cv.put(ROUTE_TOTAL_DISTANCE, totalDistance);

        mDB.update(TABLE_ROUTE, cv, ROUTE_NUM + " = ?", new String[]{"" + routeNum});
    }

    public HashMap<String, Object> provideEndOfTripData(int routeNum) {
        HashMap<String, Object> endOfTripData = new HashMap<String, Object>();
        Cursor cur = getRowEntry(routeNum);
        cur.moveToFirst();

        endOfTripData.put(mContext.getResources().getString(R.string.hash_map_start), cur.getString(INDEX_START_DATE));
        endOfTripData.put(mContext.getResources().getString(R.string.hash_map_end), cur.getString(INDEX_END_DATE));
        endOfTripData.put(mContext.getResources().getString(R.string.hash_map_time), cur.getDouble(INDEX_TIME_ELAPSED));
        endOfTripData.put(mContext.getResources().getString(R.string.hash_map_velocity), cur.getDouble(INDEX_AVG_VELOCITY));
        endOfTripData.put(mContext.getResources().getString(R.string.hash_map_rpm), cur.getDouble(INDEX_AVG_RPM));
        endOfTripData.put(mContext.getResources().getString(R.string.hash_map_power), cur.getDouble(INDEX_AVG_POWER));
        endOfTripData.put(mContext.getResources().getString(R.string.hash_map_efficiency), cur.getDouble(INDEX_AVG_EFFICIENCY));
        endOfTripData.put(mContext.getResources().getString(R.string.hash_map_energy), cur.getDouble(INDEX_ELECTRICITY_USED));
        endOfTripData.put(mContext.getResources().getString(R.string.hash_map_distance), cur.getDouble(INDEX_TOTAL_DISTANCE));

        cur.close();
        return endOfTripData;
    }

    // returns whether the route has been uploaded to parse or not
    public boolean isUploaded(int routeNum) {
        Cursor cur = getRowEntry(routeNum);
        Log.d(TAG, "" + cur.getCount());
        cur.moveToFirst();
        int uploaded = cur.getInt(INDEX_UPLOADED);
        cur.close();
        return uploaded == 1;
    }

    public Cursor getUnuploadedRoutes() {
        return mDB.query(TABLE_ROUTE, null, ROUTE_UPLOADED + "=?", new String[]{"0"}, null, null, null);
    }

    public String getRowName(int routeNum) {
        Cursor cur = getRowEntry(routeNum);
        cur.moveToFirst();
        Log.d(TAG, cur.getString(INDEX_NUM));
        String name = cur.getString(INDEX_NAME);
        cur.close();
        return name;
    }

    public int getRouteNum(String name) {
        Cursor cur = mDB.query(TABLE_ROUTE, null, ROUTE_NAME + "=?", new String[]{name}, null, null, null);
        cur.moveToFirst();
        int num = cur.getInt(INDEX_NUM);
        cur.close();
        return num;
    }

    public int getLastRouteId() {
        Cursor cur = getLastEntry();
        if (cur.getCount() > 0) {
            cur.moveToFirst();
            int id = cur.getInt(INDEX_NUM);
            cur.close();
            return id;
        }
        cur.close();
        return -1;
    }

    public String[] getAllRouteNames() {
        Cursor cur = getAllData();
        if (cur.getCount() > 0) {
            String[] names = new String[cur.getCount() + 1];
            names[0] = "Current Data";
            cur.moveToFirst();
            int count = 0;
            while(!cur.isAfterLast()) {
                names[++count] = cur.getString(INDEX_NAME);
                cur.moveToNext();
            }
            cur.close();
            return names;
        } else {
            String[] names = new String[]{"Current Data"};
            return names;
        }
    }

    public String getTableAsString() {
        Log.d(TAG, "getTableAsString called");
        String tableString = String.format("Table %s:\n", TABLE_ROUTE);
        Cursor allRows = mDB.rawQuery("SELECT * FROM " + TABLE_ROUTE, null);
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

}

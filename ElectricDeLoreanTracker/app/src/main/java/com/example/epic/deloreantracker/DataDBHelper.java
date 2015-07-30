package com.example.epic.deloreantracker;

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
 * Database helper for sqlitedatabase that stores DataPoints and relevant information
 */
public class DataDBHelper extends SQLiteOpenHelper{
    // TAG for log statements
    private static final String TAG = "DataDBHelper";

    // Database name
    private static String DBNAME = "DataDB";
    // Database version
    private static int VERSION = 1;
    // Instance of SQLite database
    private SQLiteDatabase mDB;
    // Name of database table
    private static final String TABLE_DATA = "data_points";

    // Names of all columns in the database table
    public static final String DATA_ID = "_id";
    public static final String DATA_TIMESTAMP = "timestamp";
    public static final String DATA_ROUTE = "route";
    public static final String DATA_LAT = "lat";
    public static final String DATA_LNG = "lng";
    public static final String DATA_TIME_ELPSD_SECONDS = "time_elapsed";
    public static final String DATA_DIST_TOTAL_MILES = "cumulative_distance_traveled";
    public static final String DATA_DIST_TO_EMPTY_MILES = "distance_to_empty";
    public static final String DATA_MPKWH = "average_mpkwh";
    public static final String DATA_ELECTRICITY_USED_KWH = "electricity_used";
    public static final String DATA_INSTANTANEOUS_VEL_MPH = "velocity";
    public static final String DATA_CHARGE_STATE = "charge_state";
    public static final String DATA_AMPERAGE = "amperage";
    public static final String DATA_POWER = "power";
    public static final String DATA_VOLTAGE = "voltage";
    public static final String DATA_RPM = "rpm";

    // Index of each category of data in the table
    private final int INDEX_ID = 0;
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

    /**
     * Initializes database and helper
     * @param context the context in which to create the database
     */
    public DataDBHelper(Context context) {
        super(context, DBNAME, null, VERSION);
        mDB = this.getWritableDatabase();
    }

    /**
     * Creates database and table with all categories to be stored
     * @param db database invoking this callback
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Coord onCreate called");
        String sql = "create table " + TABLE_DATA + " ( " +
                DATA_ID + " integer primary key autoincrement, " + // unique id for each piece of data entered
                DATA_TIMESTAMP + " string , " +
                DATA_ROUTE + " integer , " +
                DATA_LAT + " real , " +
                DATA_LNG + " real , " +
                DATA_TIME_ELPSD_SECONDS + " real , " +
                DATA_DIST_TOTAL_MILES + " real ," +
                DATA_DIST_TO_EMPTY_MILES + " real ," +
                DATA_MPKWH + " real, " +
                DATA_ELECTRICITY_USED_KWH + " real, " +
                DATA_INSTANTANEOUS_VEL_MPH + " real, " +
                DATA_CHARGE_STATE + " real, " +
                DATA_AMPERAGE + " real, " +
                DATA_POWER + " real, " +
                DATA_VOLTAGE + " real, " +
                DATA_RPM + " real " +
                " ) ";
        db.execSQL(sql);
    }

    /**
     * Inserts data point into the database (all data is since start of trip, if applicable)
     * @param timestamp Current time
     * @param route Route number
     * @param lat Latitude of current location
     * @param lng Longitude of current location
     * @param time Time elapsed
     * @param dist Distance traveled
     * @param distLeft Estimated remaining range of battery
     * @param mpkwh Average miles per kilowatt hour
     * @param electricityUsed Total electricity used
     * @param vel Instantaneous velocity
     * @param chargeState Most recent charge state reading from BMS
     * @param amperage Most recent instantaneous amperage reading from BMS
     * @param power Most recent power reading from motor controller
     * @param voltage Most recent voltage reading from motor controller
     * @param rpm Most recent RPM reading form motor controller
     * @return row id
     */
    public long insertDataPoint(String timestamp, int route, double lat, double lng, double time,
                                double dist, double distLeft, double mpkwh, double electricityUsed,
                                double vel, double chargeState, double amperage, double power,
                                double voltage, double rpm) {
        ContentValues cv = new ContentValues();
        cv.put(DATA_TIMESTAMP, timestamp);
        cv.put(DATA_ROUTE, route);
        cv.put(DATA_LAT, lat);
        cv.put(DATA_LNG, lng);
        cv.put(DATA_TIME_ELPSD_SECONDS, time);
        cv.put(DATA_DIST_TOTAL_MILES, dist);
        cv.put(DATA_DIST_TO_EMPTY_MILES, distLeft);
        cv.put(DATA_MPKWH, mpkwh);
        cv.put(DATA_ELECTRICITY_USED_KWH, electricityUsed);
        cv.put(DATA_INSTANTANEOUS_VEL_MPH, vel);
        cv.put(DATA_CHARGE_STATE, chargeState);
        cv.put(DATA_AMPERAGE, amperage);
        cv.put(DATA_POWER, power);
        cv.put(DATA_VOLTAGE, voltage);
        cv.put(DATA_RPM, rpm);
        return mDB.insert(TABLE_DATA, null, cv);
    }

    /**
     * Deletes all coordinates from table
     * @return the number of rows affected if a whereClause is passed in, 0 otherwise. To remove all
     * rows and get a count pass "1" as the whereClause.
     */
    public int del() {
        Log.d(TAG, "Coord delete called");
        return mDB.delete(TABLE_DATA, "1", null);
    }

    /**
     * Returns all data in the table
     * @return Cursor covering all data in the table
     */
    public Cursor getAllData() {
        //Log.d(TAG, "Coord getAllData called");
        return mDB.query(TABLE_DATA, null, null, null, null, null, null);
    }

    /**
     * Returns last row/data point entry in the table
     * @return Cursor covering last row of the table
     */
    public Cursor getLastEntry() {
        return mDB.query(TABLE_DATA, null, null, null, null, null, DATA_ID + " DESC", "1");
    }

    /**
     * Returns the database
     * @return This database
     */
    public SQLiteDatabase getDB() {
        return mDB;
    }

    /**
     * Converts all data points for the designated route into a string of JSON format
     * @param routeNum the route to be converted
     * @return String of all data in JSON format
     */
    public String dataToJSON(int routeNum) {
        Log.d(TAG, "Coord dataToJSON called");
        // Object from library used to convert to JSON
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GeometryAdapterFactory()).create();

        // List to store DataPoint objects (one for each row of the table)
        ArrayList points = new ArrayList<DataPoint>();
        // Cursor covering all data points for the given route, sorted by oldest to most recent
        Cursor cursor = mDB.query(TABLE_DATA, null, DATA_ROUTE + "=?", new String[]{"" + routeNum}, null, null, DATA_TIME_ELPSD_SECONDS + " ASC");

        // Iterates through all rows and creates a DataPoint object from each row, adds
        // DataPoint to the list
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DataPoint point = new DataPoint(cursor.getString(INDEX_TIMESTAMP), cursor.getInt(INDEX_ROUTE),
                    cursor.getDouble(INDEX_LAT), cursor.getDouble(INDEX_LNG), cursor.getDouble(INDEX_TIME_ELPSD),
                    cursor.getDouble(INDEX_DIST_TOTAL), cursor.getDouble(INDEX_DIST_TO_EMPTY), cursor.getDouble(INDEX_MPKWH),
                    cursor.getDouble(INDEX_ELETRICITY_USED), cursor.getDouble(INDEX_VEL), cursor.getDouble(INDEX_CHARGE_STATE),
                    cursor.getDouble(INDEX_AMPERAGE), cursor.getDouble(INDEX_POWER),
                    cursor.getDouble(INDEX_VOLTAGE), cursor.getDouble(INDEX_RPM));
            points.add(point);
            cursor.moveToNext();
        }

        // Convert list of DataPoints to JSON
        String json = gson.toJson(points);
        cursor.close();
        return json;
    }

    /**
     * Required implementation, used for database upgrades
     * @param db database to be upgraded
     * @param oldVersion old version
     * @param newVersion new version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // You can put stuff here!
    }

    /**
     * Returns the entire database as a string (can be used for debugging)
     * @return database in string form
     */
    public String getTableAsString() {
        Log.d(TAG, "getTableAsString called");
        String tableString = String.format("Table %s:\n", TABLE_DATA);
        Cursor allRows  = mDB.rawQuery("SELECT * FROM " + TABLE_DATA, null);
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

    /**
     * Method to get all LatLng coordinates as an ArrayList
     * @return all LatLng coordinates in database as an ArrayList
     */
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

    /**
     * Method to access last LatLng coordinate
     * @return the most recent LatLng entry in the database
     */
    public LatLng getLastLatLng() {
        Log.d(TAG, "getLastLatLng called");
        Cursor cur = getLastEntry();
        cur.moveToFirst();
        LatLng coord = new LatLng(cur.getDouble(INDEX_LAT), cur.getDouble(INDEX_LNG));
        cur.close();
        return coord;
    }

    /**
     * Method to access last longitude value for a given route
     * @param sel_route route number of desired route
     * @return last longitude value recorded for given route
     */
    public LatLng getRouteLastLng(int sel_route) {
        Log.d(TAG, "getRouteLastLatLng called");
        Cursor cur = getReadableDatabase().query(TABLE_DATA,
                new String[]{DATA_LAT, DATA_LNG},
                DATA_ROUTE + " =?",
                new String[]{String.valueOf(sel_route)},
                null,
                null,
                DATA_TIMESTAMP + " desc",
                null);
        cur.moveToLast();
        LatLng coord = new LatLng(cur.getDouble(cur.getColumnIndex(DATA_LAT)),
                cur.getDouble(cur.getColumnIndex(DATA_LNG)));
        cur.close();
        return coord;
    }

    /**
     * Method to access last latitude value for a given route
     * @param sel_route route number of desired route
     * @return last latitude value recorded for given route
     */
    public List<LatLng> getRouteAllLatLng(int sel_route) {
        Log.d(TAG, "getRouteAllLatLng called");
        List routeLatLngArrayList = new ArrayList<LatLng>();
        Cursor cur = getReadableDatabase().query(TABLE_DATA,
                new String[]{DATA_LAT, DATA_LNG},
                DATA_ROUTE + " =?",
                new String[]{String.valueOf(sel_route)},
                null,
                null,
                DATA_TIMESTAMP + " desc",
                null);
        while(cur.moveToNext()) {
            LatLng latLng = new LatLng(cur.getDouble(cur.getColumnIndex(DATA_LAT)),
                    cur.getDouble(cur.getColumnIndex(DATA_LNG)));
            routeLatLngArrayList.add(latLng);
        }

        cur.close();
        return routeLatLngArrayList;
    }

    /**
     * Deletes all data points for the designated route
     * @param routeNum the number of the route to be deleted
     * @return the number of rows affected
     */
    public int deleteDataForRoute(int routeNum) {
        Log.d(TAG, "deleteDataForRoute called");
        String[] whereArgs = new String[]{"" + routeNum};
        return mDB.delete(TABLE_DATA, DATA_ROUTE + " = ?", whereArgs);
    }

    /**
     * Checks to see if the database is empty
     * @return whether the database is empty
     */
    public boolean isEmpty() {
        Cursor cur = getAllData();
        int count = cur.getCount();
        cur.close();
        if (count > 0)
            return false;
        else
            return true;
    }

/************************ Accessor methods ***********************/

    /**
     * Accesses MPKwh data
     * @return the most recent calculation of Miles per kilowatt-hour, or -1 if no data exists
     */
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

    /**
     * Accesses total distance data
     * @return the most recent calculation of total distance travelled, or -1 if no data exists
     */
    public double getLastDistTotal() {
        Cursor cur = getLastEntry();
        if (cur.getCount() > 0) {
            cur.moveToFirst();
            double distance = cur.getDouble(INDEX_DIST_TOTAL);
            cur.close();
            return distance;
        }
        cur.close();
        return -1;
    }

    /**
     * Accesses "distance to empty" data
     * @return the most recent calculation of remaining range of battery, or -1 if no data exists
     */
    public double getLastDistToEmpty() {
        Cursor cur = getLastEntry();
        if (cur.getCount() > 0) {
            cur.moveToFirst();
            double range = cur.getDouble(INDEX_DIST_TO_EMPTY);
            cur.close();
            return range;
        }
        cur.close();
        return -1;
    }

    /**
     * Accesses velocity data
     * @return the most recent calculation of instantaneous velocity, or -1 if no data exists
     */
    public double getLastVelocity() {
        Cursor cur = getLastEntry();
        if (cur.getCount() > 0) {
            cur.moveToFirst();
            double vel = cur.getDouble(INDEX_VEL);
            cur.close();
            return vel;
        }
        cur.close();
        return -1;
    }

    /**
     * Accesses energy useage data
     * @return the most recent calculation of total electricity used, or -1 if no data exists
     */
    public double getLastElectricityUsed() {
        Cursor cur = getLastEntry();
        if (cur.getCount() > 0) {
            cur.moveToFirst();
            double energy = cur.getDouble(INDEX_ELETRICITY_USED);
            cur.close();
            return energy;
        }
        cur.close();
        return -1;
    }

    /**
     * Accesses trip time data
     * @return the most recent calculation of time elapsed, or -1 if no data exists
     */
    public double getLastTimeElapsed() {
        Cursor cur = getLastEntry();
        if (cur.getCount() > 0) {
            cur.moveToFirst();
            double time = cur.getDouble(INDEX_TIME_ELPSD);
            cur.close();
            return time;
        }
        cur.close();
        return -1;
    }
}

package com.example.epic.testapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Date;

/**
 * Created by Yicheng on 6/23/2015.
 */
public class RouteDBHelper extends SQLiteOpenHelper {
    private String TAG = "RouteDBHelper";
    private static String DBNAME = "routes.sqlite";
    private static int VERSION = 1;

    public static final String ROUTE_ID = "_id";
    public static final String ROUTE_NUM = "route_number";
    public static final String ROUTE_NAME = "route_name";
    public static final String ROUTE_START_DATE = "start_date";
    public static final String ROUTE_UPLOADED = "uploaded_to_parse";
    public static final String[] COLUMNS = new String[]{ROUTE_ID, ROUTE_NUM, ROUTE_NAME, ROUTE_START_DATE, ROUTE_UPLOADED};

    public final int INDEX_NUM = 1;
    public final int INDEX_NAME = 2;
    public final int INDEX_START_DATE = 3;
    public final int INDEX_UPLOADED = 4;

    private static final String TABLE_ROUTE = "Routes_Data";

    private SQLiteDatabase mDB;

    public RouteDBHelper(Context context) {
        super(context, DBNAME, null, VERSION);
        mDB = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + TABLE_ROUTE + " ( " +
                ROUTE_ID + " integer primary key autoincrement , " +
                ROUTE_NUM + " integer , " +
                ROUTE_NAME + " string , " +
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
        cv.put(ROUTE_NAME, route.getName());
        cv.put(ROUTE_START_DATE, route.getStartDate().getTime());
        cv.put(ROUTE_UPLOADED, route.getUploaded());

        return getWritableDatabase().insert(TABLE_ROUTE, null, cv);
    }

    public void deleteRoute(int routeNum) {
        boolean x = mDB.delete(TABLE_ROUTE, ROUTE_NUM + " = " + routeNum, null) > 0;
    }

    //returns all routes from table
    public Cursor getAllData() {
        //Log.d(TAG, "Coord getAllData called");
        return mDB.query(TABLE_ROUTE, COLUMNS, null, null, null, null, null);
    }

    // updates database row to mark as uploaded to parse
    public void setUploaded(int row) {
        ContentValues cv = new ContentValues();
        cv.put(ROUTE_UPLOADED, 1);
        String sRow = "" + row;
        mDB.update(TABLE_ROUTE, cv, ROUTE_NUM + " = ?", new String[]{sRow});
    }

    // updates database row to change name
    public void updateName(String name, int row) {
        ContentValues cv = new ContentValues();
        cv.put(ROUTE_NAME, name);
        String sRow = "" + row;
        mDB.update(TABLE_ROUTE, cv, ROUTE_NUM + " = ?", new String[]{sRow});
    }

    // returns whether the route has been uploaded to parse or not
    public boolean isUploaded(int i) {
        //TODO check if this is working properly
        Cursor cur = mDB.query(TABLE_ROUTE, null, ROUTE_NUM + "=?", new String[]{"" + i}, null, null, null);
        Log.d(TAG, "" + cur.getCount());
        cur.moveToFirst();
        int uploaded = cur.getInt(INDEX_UPLOADED);
        cur.close();
        return uploaded == 1;
    }

    public Cursor getUnuploadedRoutes() {
        return mDB.query(TABLE_ROUTE, null, ROUTE_UPLOADED + "=?", new String[]{"0"}, null, null, null);
    }

    public String getRowName(int row) {
        //TODO FIX THIS
        Cursor cur = mDB.query(TABLE_ROUTE, null, ROUTE_NUM + "=?", new String[]{"" + row}, null, null, null);
        cur.moveToFirst();
        Log.d(TAG, cur.getString(INDEX_NUM));
        String name = cur.getString(INDEX_NAME);
        cur.close();
        return name;
    }

    public int getRouteNum(String name) {
        //TODO fix this
        Cursor cur = mDB.query(TABLE_ROUTE, null, ROUTE_NAME + "=?", new String[]{name}, null, null, null);
        cur.moveToFirst();
        int num = cur.getInt(INDEX_NUM);
        cur.close();
        return num;
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
        Cursor allRows  = mDB.rawQuery("SELECT * FROM " + TABLE_ROUTE, null);
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

    // TODO THIS METHOD
    public String[] getLifetimeData(int route) {

        return new String[3];
    }
//    public long insert(ContentValues contentValues) {
//        long rowID = mDB.insert(TABLE_COORD, null, contentValues);
//        return rowID;
//    }

}

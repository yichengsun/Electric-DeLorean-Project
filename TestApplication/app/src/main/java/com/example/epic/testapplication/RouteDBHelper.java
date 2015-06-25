//package com.example.epic.testapplication;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//
//import java.util.Date;
//
///**
// * Created by Yicheng on 6/23/2015.
// */
//public class RouteDBHelper extends SQLiteOpenHelper {
//    private static String DBNAME = "routes.sqlite";
//    private static int VERSION = 1;
//
//    public static final String ROUTE_ID = "_id";
//    public static final String ROUTE_START_DATE = "start_date";
//
//    private static final String TABLE_ROUTE = "Routes Data";
//
//    private SQLiteDatabase mDB;
//
//    public RouteDBHelper(Context context) {
//        super(context, DBNAME, null, VERSION);
//        mDB = getWritableDatabase();
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        String sql = "create table " + TABLE_ROUTE + " (" +
//                "_id integer primary key autoincrement, start_date integer)";
//        db.execSQL(sql);
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//    }
//
//    public long insertRoute(Route route) {
//        ContentValues cv = new ContentValues();
//        cv.put(ROUTE_START_DATE, route.getStartDate().getTime());
//        return getWritableDatabase().insert(TABLE_ROUTE, null, cv);
//    }
//
////    public long insert(ContentValues contentValues) {
////        long rowID = mDB.insert(TABLE_COORD, null, contentValues);
////        return rowID;
////    }
//
//}

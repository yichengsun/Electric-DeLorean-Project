package com.example.epic.testapplication;

import java.util.Date;

/**
 * Created by Yicheng on 6/23/2015.
 */
public class Route {
    public Date mStartDate;
    private long mID;
    private int mUploaded;

    public Route() {
        mID = -1;
        mStartDate = new Date();
    }

    public Route(int routeNum) {
        mID = routeNum;
        mStartDate = new Date();
    }

    public long getmID() {
        return mID;
    }

    public void setmID(long ID) {
        this.mID = ID;
    }

    public Date getStartDate() {
        return mStartDate;
    }

    public void setStartDate(Date startDate) {
        this.mStartDate = startDate;
    }

    public int getUploaded() {
        return mUploaded;
    }

    public void setUploaded() {
        mUploaded = 1;
    }

}

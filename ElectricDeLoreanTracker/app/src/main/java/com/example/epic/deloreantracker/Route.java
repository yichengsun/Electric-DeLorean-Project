package com.example.epic.deloreantracker;

import java.util.Date;

/**
 *
 * Note that this class has no setter methods. mStartDate and mID can only be set once, as there
 * is no need to change them after the  * route is created. mUploaded and mName only have accessor
 * methods and no set() methods because  * this data is directly edited in the RouteDB when needed.
 * The route class as a whole simply  * exists to make the insertRoute() function a bit cleaner by
 * packaging everything together into one object.
 */
public class Route {
    // Start time of route
    private final Date mStartDate;
    // id of route
    private final long mID;
    // Whether route has been uploaded (0 = no, 1 = yes), in int because SQLite doesn't take boolean
    private int mUploaded = 0;
    // Default name of route
    private String mName = "unnamed";

    /**
     * Creates a new route with given route number and starting time as current time
     * @param routeNum Designated route number for oute
     */
    public Route(int routeNum) {
        mID = routeNum;
        mStartDate = new Date();
    }

    /**
     * Accesses route id
     * @return route id
     */
    public long getmID() {
        return mID;
    }

    /**
     * Accesses route start date
     * @return start date for route
     */
    public Date getStartDate() {
        return mStartDate;
    }

    /**
     * Accesses whether the route has been uploaded to Parse
     * @return 0 if not uploaded, 1 if uploaded
     */
    public int getUploaded() {
        return mUploaded;
    }

    /**
     * Accesses route name
     * @return name of route
     */
    public String getName() {
        return mName;
    }

}

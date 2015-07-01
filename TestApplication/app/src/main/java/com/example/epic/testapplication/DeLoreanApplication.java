package com.example.epic.testapplication;

import android.app.Application;
import com.parse.Parse;

/**
 * Created by henryshangguan on 7/1/15.
 */
public class DeLoreanApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        //Parse initialization
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "uSrtODrZBDyDwNPUXviACZ2QU3SiMWezzQ9v1Pl9",
                "Ul60j3g3iqTRPAxgWZYGSB85RjPTOZAsaFMtMNhH");
    }
}

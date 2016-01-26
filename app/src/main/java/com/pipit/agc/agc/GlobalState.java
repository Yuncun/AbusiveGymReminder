package com.pipit.agc.agc;

import android.app.Application;

import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.data.MySQLiteHelper;

/**
 * Currently only used to initialize SqlDatabase singleton
 * Created by Eric on 1/10/2016.
 */
public class GlobalState extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DBRecordsSource.initializeInstance(new MySQLiteHelper(this));
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}

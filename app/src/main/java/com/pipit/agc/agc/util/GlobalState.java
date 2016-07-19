package com.pipit.agc.agc.util;

import android.app.Application;

import com.pipit.agc.agc.data.MsgAndDayRecords;
import com.pipit.agc.agc.data.MsgDBHelper;

/**
 * Currently only used to initialize SqlDatabase singleton
 * Created by Eric on 1/10/2016.
 */
public class GlobalState extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MsgAndDayRecords.initializeInstance(new MsgDBHelper(this));
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

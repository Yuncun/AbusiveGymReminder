package com.pipit.agc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pipit.agc.controller.GeofenceController;
import com.pipit.agc.util.Constants;
import com.pipit.agc.util.SharedPrefUtil;

import java.util.Calendar;

/**
 * Created by Eric on 1/30/2016.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent i) {
        Log.d(TAG, "onReceive in BootReceiver");
        SharedPrefUtil.updateMainLog(context, "BOOT RECEIVER");

        /*Initialize geofencecontroller*/
        GeofenceController.getInstance().init(context);

        /*Set the daily update alarm*/
        //AlarmManagerBroadcastReceiver _alarm = new AlarmManagerBroadcastReceiver();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, Constants.DAY_RESET_HOUR);
        calendar.set(Calendar.MINUTE, Constants.DAY_RESET_MINUTE);
        calendar.add(Calendar.DATE, Constants.DAYS_TO_ADD);
        AlarmManagerBroadcastReceiver.setAlarmForDayLog(context.getApplicationContext(), calendar);
    }


}
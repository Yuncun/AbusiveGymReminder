package com.pipit.agc.agc.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.location.Location;
import android.os.SystemClock;
import android.util.Log;

import com.pipit.agc.agc.activity.AllinOneActivity;
import com.pipit.agc.agc.controller.GeofenceController;
import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.util.SharedPrefUtil;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Eric on 1/30/2016.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent i) {
        Log.d(TAG, "onReceive in BootReceiver");
        SharedPrefUtil.updateMainLog(context, "BOOT RECEIVER");
        GeofenceController.getInstance().init(context);

        /*Set alarm*/
        AlarmManagerBroadcastReceiver _alarm = new AlarmManagerBroadcastReceiver();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, Constants.DAY_RESET_HOUR);
        calendar.set(Calendar.MINUTE, Constants.DAY_RESET_MINUTE);
        calendar.add(Calendar.DATE, 1);
        _alarm.setAlarmForDayLog(context, calendar);
    }


}
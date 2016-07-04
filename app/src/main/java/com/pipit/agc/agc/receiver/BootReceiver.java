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


    /*static void addProxAlert(Context context, int i){
        LocationManager lm=(LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        Location gymlocation = AllinOneActivity.getGymLocation(context, i).location;
        if (gymlocation.getLatitude()==Constants.DEFAULT_COORDINATE && gymlocation.getLongitude()==Constants.DEFAULT_COORDINATE){
            return;
        }

        float range = (float) prefs.getInt("range", 100);
        Intent intent = new Intent(Constants.PROX_INTENT_FILTER);
        int alertId= (int) System.currentTimeMillis();
        PendingIntent pi = PendingIntent.getBroadcast(context, alertId, intent, 0);
        Log.d(TAG, "Adding prox alert, ID is " + alertId + " range is " + range);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("proxalert"+i, alertId).commit();
        try{
            lm.addProximityAlert(gymlocation.getLatitude(), gymlocation.getLongitude(), range, -1, pi);
        } catch (SecurityException e){
            Log.e(TAG, e.getMessage());
        }
        //Adding log
        String mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        String lastLocation = prefs.getString("locationlist", "none");
        String body = lastLocation+"\n" + "BOOT LOADER ADDED PROX ALERT at " + mLastUpdateTime;
        editor.putString("locationlist", body);

    } */
    static void scheduleAlarms(Context context) {
        /*AlarmManager mgr=
                (AlarmManager)ctxt.getSystemService(Context.ALARM_SERVICE);
        Intent i=new Intent(ctxt, ScheduledService.class);
        PendingIntent pi=PendingIntent.getService(ctxt, 0, i, 0);

        mgr.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + PERIOD, PERIOD, pi);

        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);*/
    }
}
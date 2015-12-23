package com.pipit.agc.agc;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by Eric on 12/12/2015.
 */
public class AlarmManagerBroadcastReceiver extends BroadcastReceiver
{
    GoogleApiClient mGoogleApiClient;
    static AllinOneActivity _main;
    Context _context;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        _context=context;
        Toast.makeText(context, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show(); // For example
        doLocationCheck(context);

        wl.release();
    }

    public void SetAlarm(Context context)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Constants.timeBetweenLocationChecks, pi);
    }

    public void CancelAlarm(Context context)
    {
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    private void doLocationCheck(Context context){
        /*Request Location*/
        if(_main!=null){
            _main.startLocationUpdates();
        }

        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        /*Record number of times we have done a check*/
        int trackcount = prefs.getInt("trackcount", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("trackcount", trackcount+1);
        editor.commit();

        Log.d("Alarm", "trackcount committed " + trackcount);
    }

    public void setGoogleApiThing(GoogleApiClient api){
        mGoogleApiClient=api;
    }

    public void setMainActivity(AllinOneActivity main){
        _main=main;
    }

}

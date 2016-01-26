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
import com.pipit.agc.agc.data.DayRecord;
import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.data.MySQLiteHelper;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Eric on 12/12/2015.
 */
public class AlarmManagerBroadcastReceiver extends BroadcastReceiver
{
    GoogleApiClient mGoogleApiClient;
    Context _context;
    String TAG = "AlarmManagerBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        Log.d(TAG, "Received broadcast");
        _context=context;
        //doLocationCheck(context);
        doDayLogging(context);
        wl.release();
    }

    public void SetAlarm(Context context, Calendar calendar)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);
    }

    public void CancelAlarm(Context context)
    {
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    private void doLocationCheck(Context context){
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        /*Record number of times we have done a check*/
        int trackcount = prefs.getInt("trackcount", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("trackcount", trackcount+1);
        editor.commit();

        Log.d("Alarm", "trackcount committed " + trackcount);
    }

    /**
     * Executed by Alarm Manager at midnight to add a new day into database
     */
    private void doDayLogging(Context context){
        //Logging
        Log.d(TAG, "Starting dayLogging");
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = prefs.edit();
        String mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        String logUpdate = prefs.getString("locationlist", "none") + "\n" + "Alarm Manager Update at " + mLastUpdateTime;
        editor.putString("locationlist", logUpdate);
        editor.commit();

        //Progress the day
        DBRecordsSource datasource;
        datasource = DBRecordsSource.getInstance();
        if (datasource==null){
            DBRecordsSource.initializeInstance(new MySQLiteHelper(context));
            datasource = DBRecordsSource.getInstance();
        }
        datasource.openDatabase();
        DayRecord dayRecord = datasource.createDayRecord("Did not go to gym ", new Date());
        datasource.closeDatabase();
        Toast.makeText(context, "new day added!", Toast.LENGTH_LONG);

    }

    public void setGoogleApiThing(GoogleApiClient api){
        mGoogleApiClient=api;
    }

}

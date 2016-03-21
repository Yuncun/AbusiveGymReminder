package com.pipit.agc.agc.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.pipit.agc.agc.model.Message;
import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.util.ReminderOracle;
import com.pipit.agc.agc.model.DayRecord;
import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.data.MySQLiteHelper;
import com.pipit.agc.agc.util.Util;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Eric on 12/12/2015.
 */
public class AlarmManagerBroadcastReceiver extends BroadcastReceiver
{
    Context _context;
    String TAG = "AlarmManagerBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        _context=context;

        String purpose = intent.getStringExtra("purpose");
        Log.d(TAG, "Received broadcast for " + purpose);
        switch(purpose){
            case "daylogging" :
                doDayLogging(context);
                break;
            case "leavemessage" :
                String msgJson = intent.getStringExtra("message");
                Log.d(TAG, "RECEIVER " + msgJson);
                Message m = Message.fromJson(msgJson);
                ReminderOracle.leaveMessage(m);
                break;
            default:
                break;
        }
        wl.release();
    }



    public void setAlarmForDayLog(Context context, Calendar calendar)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmManagerBroadcastReceiver.class);
        i.putExtra("purpose", "daylogging");
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);
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
        DayRecord day = new DayRecord();
        day.setComment("You have not been to the gym");
        day.setDate(new Date());
        day.setHasBeenToGym(false);
        day.setIsGymDay(isTheNewDayAGymDay(context));
        DayRecord dayRecord = datasource.createDayRecord(day);
        datasource.closeDatabase();
        Toast.makeText(context, "new day added!", Toast.LENGTH_LONG);

        //Show the gym status card in Newsfeed
        editor.putBoolean("showGymStatus", true);
        //ReminderOracle.leaveMessageAtTime(context, 0, 10); //Makes tomorrow's reminder
        ReminderOracle.doLeaveMessageBasedOnPerformance(context);
    }

    private void doLeaveMessage(Context context, Message m){
        Log.d(TAG, "doLeaveMessage");
        ReminderOracle.leaveMessage(m);
    }

    private boolean isTheNewDayAGymDay(Context context){
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        List<String> plannedDOWstrs = Util.getListFromSharedPref(prefs, Constants.SHAR_PREF_PLANNED_DAYS);
        List<Integer> plannedDOW = Util.listOfStringsToListOfInts(plannedDOWstrs);
        HashSet<Integer> set = new HashSet<Integer>(plannedDOW);
        Calendar cal = Calendar.getInstance();
        int DOW = cal.get(Calendar.DAY_OF_WEEK);
        DOW--;
        if (set.contains(DOW)){
            return true;
        }
        else return false;

    }

}

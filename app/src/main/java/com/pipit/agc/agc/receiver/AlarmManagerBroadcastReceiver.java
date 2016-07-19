package com.pipit.agc.agc.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.util.Log;

import com.pipit.agc.agc.R;
import com.pipit.agc.agc.data.MsgAndDayRecords;
import com.pipit.agc.agc.model.Message;
import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.util.ReminderOracle;
import com.pipit.agc.agc.model.DayRecord;
import com.pipit.agc.agc.data.MsgDBHelper;
import com.pipit.agc.agc.util.SharedPrefUtil;
import com.pipit.agc.agc.util.Util;

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
    private static final String TAG = "AlarmManagerBrdcstRcvr";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        _context=context;

        String purpose = intent.getStringExtra("purpose");
        Log.d(TAG, "Received broadcast for " + purpose);
        SharedPrefUtil.updateMainLog(context, "Alarm woken with a broadcast for " + purpose);
        switch(purpose){
            case "daylogging" :
                doDayLogging(context);
                break;
            case "leavemessage" :
                String msgJson = intent.getStringExtra("message");
                Log.d(TAG, "RECEIVER " + msgJson);
                Message m = Message.fromJson(msgJson);

                //Directly leave message in inbox
                ReminderOracle.leaveMessage(m);
                if (m.getRepoId()>0){
                    Util.putStringIntoListIntoSharedPrefs(context, Constants.TAKEN_MESSAGE_IDS, Long.toString(m.getRepoId()));
                }
                String firstLineBody = "";
                String title = "";
                String secondLineBody = "";
                long msgid = -1;
                int reason = Message.NO_RECORD;
                //Construct notification message and show
                switch (m.getReason()){
                    case Message.MISSED_YESTERDAY:
                        title = m.getHeader();
                        firstLineBody = m.getBody();
                        secondLineBody = "(Missed a gym day yesterday)";
                        msgid = m.getId();
                        reason = Message.MISSED_YESTERDAY;
                        break;
                    case Message.HIT_YESTERDAY:
                    case Message.HIT_TODAY:
                        title = m.getHeader();
                        firstLineBody = m.getBody();
                        secondLineBody = context.getString(R.string.reason_hit_gym_today);
                        reason = Message.HIT_TODAY;
                        msgid = m.getId();
                        break;
                    case Message.NO_RECORD:
                    default:
                        title = m.getHeader();
                        firstLineBody = m.getBody();
                        msgid = m.getId();
                        reason = Message.NEW_MSG;
                }
                ReminderOracle.showNotification(context, title, firstLineBody, secondLineBody, msgid, reason);
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
    public static void doDayLogging(Context context){
        //Logging
        Log.d(TAG, "Starting dayLogging");
        SharedPrefUtil.updateMainLog(context, "Doing day logging, adding a day");

        //Progress current day - open db
        MsgAndDayRecords datasource;
        datasource = MsgAndDayRecords.getInstance();
        if (datasource==null){
            MsgAndDayRecords.initializeInstance(new MsgDBHelper(context));
            datasource = MsgAndDayRecords.getInstance();
        }
        datasource.openDatabase();

        //If the day was already updated by another mechanism, (like in onStart), then
        //we don't need to do this again.
        DayRecord yesterday = datasource.getLastDayRecord();
        if (yesterday.compareToDate(new Date())){
            return;
        }

        //Finish the current visit if we are currently in a gym
        boolean flagStartNewVisit = false;
        if (yesterday.isCurrentlyVisiting()){
            datasource.closeDatabase();
            yesterday.endCurrentVisit();
            datasource.openDatabase();
            flagStartNewVisit = true;
        }

        //Add the new day
        DayRecord day = new DayRecord();
        day.setComment("You have not been to the gym");
        day.setDate(new Date());
        day.setHasBeenToGym(false);
        day.setIsGymDay(isTheNewDayAGymDay(context));
        datasource.createDayRecord(day);
        datasource.closeDatabase();
        if (flagStartNewVisit){
            day.startCurrentVisit();
        }
        ReminderOracle.doLeaveMessageBasedOnPerformance(context, false);
    }

    private static boolean isTheNewDayAGymDay(Context context){
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

package com.pipit.agc.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.util.Log;

import com.pipit.agc.R;
import com.pipit.agc.activity.AllinOneActivity;
import com.pipit.agc.data.MsgAndDayRecords;
import com.pipit.agc.model.Message;
import com.pipit.agc.util.Constants;
import com.pipit.agc.util.ReminderOracle;
import com.pipit.agc.model.DayRecord;
import com.pipit.agc.data.MsgDBHelper;
import com.pipit.agc.util.SharedPrefUtil;
import com.pipit.agc.util.StatsContent;
import com.pipit.agc.util.Util;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * This receiver is responsible for all the scheduled stuff.
 *
 * Right now, this means
 * 1) Updating day at midnight
 * 2) Scheduling messages
 *
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
                //Alarm went off to progress the day
                doDayLogging(context);
                break;
            case "leavemessage" :
                //Alarm went off to show a notification!
                String msgJson = intent.getStringExtra("message");
                Message m = Message.fromJson(msgJson);

                if (m.getRepoId()>0){
                    SharedPrefUtil.putStringIntoListIntoSharedPrefs(context, Constants.TAKEN_MESSAGE_IDS, Long.toString(m.getRepoId()));
                }

                //Directly leave message in inbox
                ReminderOracle.leaveMessage(m);

                //At this point, we can either show the notification directly, or set a flag and wait for the
                //device to wake before showing.
                int showtimepref = SharedPrefUtil.getInt(context, Constants.PREF_NOTIF_TIME, -1);
                Log.d("Yuncun", "showtimepref " + showtimepref + " and reason is " + m.getReason());
                if (showtimepref ==Constants.NOTIFTIME_ON_WAKEUP && m.getReason() != Message.HIT_TODAY){
                    SharedPrefUtil.putInt(context, Constants.FLAG_WAKEUP_SHOW_NOTIF, 1);
                    SharedPrefUtil.putString(context, Constants.CONTENT_WAKEUP_SHOW_NOTIF, m.toJson());
                }else{
                    //Show notification now
                    ReminderOracle.showNotificationFromMessage(context, m);
                }
                break;
            default:
                break;
        }
        wl.release();
    }

    public static void setAlarmForDayLog(Context context, Calendar calendar)
    {
        Log.d(TAG, "Set daily alarm");
        Intent i = new Intent(context, AlarmManagerBroadcastReceiver.class);

        boolean alarmUp = (PendingIntent.getBroadcast(context, 0,
                i,
                PendingIntent.FLAG_NO_CREATE) != null);
        //Avoid setting alarm if it is already set
        //Note - If you are messing around with alarm times, then you will not receive updated alarms unless you reboot
        if (alarmUp)
        {
            Log.d("Eric", "Alarm is already active - Not setting an alarm");
        }
        else{
            Log.d("Eric", "Alarm not active - Setting alarm manager");
            AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            i.putExtra("purpose", "daylogging");
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pi);
        }
    }

    /**
     * Executed by Alarm Manager at midnight to add a new day into database
     * This is not necessarily always called (see AllinOneActivity.updateDate()
     */
    public static void doDayLogging(Context context){
        //Logging
        Log.d(TAG, "Starting dayLogging");
        SharedPrefUtil.updateMainLog(context, "Doing day logging, adding a day");

        //Update the date
        AllinOneActivity.updateDate(context);

        //If the day was already updated by another mechanism, (like in onStart), then
        //we don't need to do this again.
        //DayRecord yesterday = StatsContent.getInstance().getYesterday(true);
        ReminderOracle.doLeaveMessageBasedOnPerformance(context, false);
        return;

        //Progress current day - open db
        /*
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
        if (!yesterday.equalsDate(new Date())){
            Log.d(TAG, "End daylogging - date was already good before doDayLogging");
            datasource.closeDatabase();
            ReminderOracle.doLeaveMessageBasedOnPerformance(context, false);
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
        day.setIsGymDay(isTheNewDayAGymDay(context, day.getDate()));
        datasource.createDayRecord(day);
        datasource.closeDatabase();
        if (flagStartNewVisit){
            day.startCurrentVisit();
        }
        ReminderOracle.doLeaveMessageBasedOnPerformance(context, false);
        */
    }
}

package com.pipit.agc.agc.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.data.MessageRepoAccess;
import com.pipit.agc.agc.data.MessageRepositoryStructure;
import com.pipit.agc.agc.model.DayRecord;
import com.pipit.agc.agc.model.Message;
import com.pipit.agc.agc.receiver.AlarmManagerBroadcastReceiver;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Suite of functions that delivers reminders based on gym behavior
 */
public class ReminderOracle {
    private static final String TAG = "ReminderOracle";
    /**
     * This function chooses a message and figures out a time to post it, based on gym attendance
     * Call this function AFTER moving forward to the new day (it will use yesterday's gym stats).
     */
    public static void doLeaveMessageBasedOnPerformance(Context context, boolean testmode){
        String log = "\nLog:"; //Log used for test mode

        DayRecord yesterday = StatsContent.getInstance().getYesterday(true);
        DayRecord today = StatsContent.getInstance().getToday(false);
        MessageRepoAccess databaseAccess = MessageRepoAccess.getInstance(context);
        databaseAccess.open();
        Message msg = null;
        if (yesterday==null){
            msg = new Message();
            msg.setHeader("Welcome!");
            msg.setBody("In the future you will messages like this calling you fat when you miss gym days");
        }
        else if (yesterday.beenToGym()) {
            msg = databaseAccess.getRandomMessageWithParams(MessageRepositoryStructure.REMINDER_HITYESTERDAY,
                    MessageRepositoryStructure.KINDA_ANNOYED);
            msg.setReason(Message.HIT_YESTERDAY);
            Log.d(TAG, "ReminderOracle leaving message " + msg + " header:" + msg.getHeader() + "body" + msg.getBody() );
        }else if (!yesterday.beenToGym()) {
            msg = databaseAccess.getRandomMessageWithParams(MessageRepositoryStructure.REMINDER_MISSEDYESTERDAY,
                    MessageRepositoryStructure.KINDA_ANNOYED);
            msg.setReason(Message.MISSED_YESTERDAY);
        }
        databaseAccess.close();

        /*Calculate time*/
        Random rand = new Random();
        int  n = rand.nextInt(60) + 1;
        long time_ms = SharedPrefUtil.getLong(context, "lastgymtime", -1);

        Calendar cal = Calendar.getInstance();
        if (time_ms>0){
            cal.setTimeInMillis(time_ms);
            cal.add(Calendar.MINUTE, n);
        }
        else{
            //NEver been to gym yet, or no record
            cal.add(Calendar.HOUR, 16); //16 hours, an arbitrary time
        }

        int hour = cal.get(Calendar.HOUR);
        int minutes = cal.get(Calendar.MINUTE);
        log+="hours:" + hour + " minutes:" + minutes;
        Log.d(TAG, "hours"+hour + " min"+minutes);
        if (testmode && msg!=null){
            //Test mode allows us to receive message instantly for testing purposes.
            msg.setHeader("(Test) " + msg.getHeader());
            msg.setBody(msg.getBody()+log);
            leaveMessageAtTime(context, msg, 0, 0);
        }
        else
        if (msg!=null){
            leaveMessageAtTime(context, msg, hour, minutes);
        }
    }

    public static void doLeaveMessageBasedOnPerformance(Context context){
        doLeaveMessageBasedOnPerformance(context, false);
    }


        /**
         * Uses previous statistics to figure out what message to send to user
         */
    private static void oracle(){

    }

    /**
     * Leave message immediately - used mostly for testing at this point
     * @param msg
     */
    public static void leaveMessage(Message msg) {
        DBRecordsSource datasource;
        //Updates Data
        datasource = DBRecordsSource.getInstance();
        datasource.openDatabase();
        datasource.createMessage(msg, new Date());
        DBRecordsSource.getInstance().closeDatabase();
    }

    /**
     * Call this to leave a message at a given time
     * Note that the message to leave is determined by the ReminderOracle and is not specfied at this time
     * All this does is set an alarm to consult the oracle for a message at a given time
     * @param context
     */
    public static void leaveMessageAtTime(Context context, Message m, Calendar calendar){
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmManagerBroadcastReceiver.class);
        i.putExtra("purpose", "leavemessage");
        i.putExtra("message", m.toJson());
        PendingIntent pi = PendingIntent.getBroadcast(context, 3, i, PendingIntent.FLAG_CANCEL_CURRENT);

        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        Log.d(TAG, "leaveMessageAtTime " + System.currentTimeMillis()
                + " alarm set for " + calendar.getTimeInMillis() + " message " + m.toJson());
    }

    /**
     * Same as leaveMessageAtTime(Context, Calendar) but does the math for you.
     * @param context
     * @param minutes
     */
    public static void leaveMessageAtTime(Context context, Message m, int hours, int minutes){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.HOUR, hours);
        calendar.add(Calendar.MINUTE, minutes);
        calendar.add(Calendar.SECOND, 10);
        leaveMessageAtTime(context, m, calendar);
    }


}

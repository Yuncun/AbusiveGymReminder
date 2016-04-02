package com.pipit.agc.agc.util;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.pipit.agc.agc.R;
import com.pipit.agc.agc.activity.AllinOneActivity;
import com.pipit.agc.agc.activity.MessageBodyActivity;
import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.data.MessageRepoAccess;
import com.pipit.agc.agc.data.MessageRepositoryStructure;
import com.pipit.agc.agc.model.DayRecord;
import com.pipit.agc.agc.model.Message;
import com.pipit.agc.agc.receiver.AlarmManagerBroadcastReceiver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * Suite of functions that delivers reminders based on gym behavior
 */
public class ReminderOracle {
    public static final int POST_TIME_NOISE_HOURS = 2;
    public static final int POST_TIME_NOISE_MINTUES = 60;
    private static final String TAG = "ReminderOracle";
    /**
     * This function chooses a message and figures out a time to post it, based on gym attendance
     * Call this function AFTER moving forward to the new day (it will use yesterday's gym stats).
     */
    public static void doLeaveMessageBasedOnPerformance(Context context, boolean testmode){
        Log.d(TAG, "doLeaveMessageBasedOnPerformance");

        DayRecord yesterday = StatsContent.getInstance().getYesterday(true);
        DayRecord today = StatsContent.getInstance().getToday(false);
        MessageRepoAccess messagerepo = MessageRepoAccess.getInstance(context);
        messagerepo.open();
        Message msg = null;
        if (yesterday==null){
            msg = new Message();
            msg.setHeader("Welcome!");
            msg.setBody("In the future you will messages like this calling you fat when you miss gym days");
        }
        else if (testmode){
            int type = 0;
            int reason = 0;
            if (yesterday.beenToGym()){
                type = MessageRepositoryStructure.REMINDER_HITYESTERDAY;
                reason = Message.HIT_TODAY;
            }else{
                type = MessageRepositoryStructure.REMINDER_MISSEDYESTERDAY;
                reason = Message.MISSED_YESTERDAY;
            }
            try{
                Log.d(TAG, "Attempting to get a new message");
                long id = findANewMessageId(context, type);
                if (!messagerepo.isOpen()) { messagerepo.open(); }
                if (id < 1){
                    Log.d(TAG, "No id's found, getting random message");
                    msg = messagerepo.getRandomMessageWithParams(type,
                            MessageRepositoryStructure.KINDA_ANNOYED);
                    msg.setReason(reason);
                }else{
                    msg = messagerepo.getMessageById(id);
                    msg.setReason(reason);
                }
            } catch (Exception e){
                Log.d(TAG, e.toString());
            }
        }
        else if (yesterday.beenToGym()) {
            // Todo: Figure out if we want to leave a message here or not - am exploring the idea of leaving message immediately
            // Todo: when user goes to the gym, which would make this redundant.
        }
        else if (!yesterday.beenToGym()) {
            //long testid = findANewMessageId(context, 2);
            //Log.d(TAG, "RANDOM GENERATED ID " + testid);
            msg = messagerepo.getRandomMessageWithParams(MessageRepositoryStructure.REMINDER_MISSEDYESTERDAY,
                    MessageRepositoryStructure.KINDA_ANNOYED);
            msg.setReason(Message.MISSED_YESTERDAY);
        }
        messagerepo.close();
        /*Calculate time*/
        Random rand = new Random();
        int hr_noise = rand.nextInt(POST_TIME_NOISE_HOURS);
        int min_noise = rand.nextInt(POST_TIME_NOISE_MINTUES);
        long time_ms = SharedPrefUtil.getLong(context, "lastgymtime", -1);

        Calendar cal = Calendar.getInstance();
        if (time_ms>0){
            cal.setTimeInMillis(time_ms);
            cal.add(Calendar.MINUTE, min_noise);
        }
        else{
            cal.add(Calendar.HOUR, 16); //16 hours, a default (arbitrary) time to wait before showing message
        }
        int hour = cal.get(Calendar.HOUR);
        int minutes = cal.get(Calendar.MINUTE);
        Log.d(TAG, "hours"+hour + " min"+minutes + " from hour_noise"+hr_noise + " and min_noise"+min_noise);
        if (testmode && msg!=null){
            setLeaveMessageAlarm(context, msg, 0, 0);
        }
        else
        if (msg!=null){
            setLeaveMessageAlarm(context, msg, hour, minutes);
        }
    }

    /**
     * This function leaves a message immediately
     * @param msg
     */
    public static void leaveMessage(Message msg) {
        DBRecordsSource datasource;
        datasource = DBRecordsSource.getInstance();
        datasource.openDatabase();
        datasource.createMessage(msg, new Date());
        datasource.closeDatabase();
    }

    /**
     * Call this to leave a message at a given time
     * This function sets an alarm at the given time, with the message included as an intent extra.
     * AlarmManagerBroadcastReceiver will receive the alarm and call ReminderOracle.leaveMessage();
     * @param context
     */
    private static void setLeaveMessageAlarm(Context context, Message m, Calendar calendar){
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmManagerBroadcastReceiver.class);
        i.putExtra("purpose", "leavemessage");
        i.putExtra("message", m.toJson());
        PendingIntent pi = PendingIntent.getBroadcast(context, 3, i, PendingIntent.FLAG_CANCEL_CURRENT);

        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        Log.d(TAG, "setLeaveMessageAlarm " + System.currentTimeMillis()
                + " alarm set for " + calendar.getTimeInMillis() + " message " + m.toJson());
    }

    /**
     * Same as setLeaveMessageAlarm(Context, Calendar) but does the math for you.
     * @param context
     * @param minutes
     */
    private static void setLeaveMessageAlarm(Context context, Message m, int hours, int minutes){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.HOUR, hours);
        calendar.add(Calendar.MINUTE, minutes);
        calendar.add(Calendar.SECOND, 10);
        setLeaveMessageAlarm(context, m, calendar);
    }

    /**
     * This function is analogous to doLeaveMessageBasedOnPerformance, but is used to show a positive message after the
     * user arrives at the gym. The message can be delivered immediately (on gym arrival) or after a delay so the user
     * receives it presumably after his or her workout
     */
    public static void doLeaveOnGymArrivalMessage(Context context, boolean immediate){
        Log.d(TAG, "doLeaveOnGymArrivalMessage(Context, " + immediate + ")");
        MessageRepoAccess databaseAccess = MessageRepoAccess.getInstance(context);
        databaseAccess.open();
        Message msg = null;
        msg = databaseAccess.getRandomMessageWithParams(MessageRepositoryStructure.REMINDER_HITYESTERDAY,
                MessageRepositoryStructure.KINDA_ANNOYED);
        msg.setReason(Message.HIT_TODAY);
        databaseAccess.close();

        if (immediate){
            setLeaveMessageAlarm(context, msg, 0, 0);
        }
        else{
            setLeaveMessageAlarm(context, msg, 1, 0);
        }
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    public static void showNotification(Context context, String header, String body, long messageID, int reason){
        Intent notificationIntent = new Intent(context, AllinOneActivity.class);
        if (messageID>0){
            notificationIntent = new Intent(context, AllinOneActivity.class);
            notificationIntent.putExtra(Constants.MESSAGE_ID, messageID);
        }

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(AllinOneActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        if (reason==Message.MISSED_YESTERDAY){
            builder.setSmallIcon(R.drawable.notification_icon)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.notification_icon))
                .setColor(Color.RED)
                .setContentTitle(header)
                .setContentText(body)
                .setContentIntent(notificationPendingIntent);
        }
        else{
            builder.setSmallIcon(R.drawable.notification_icon)
            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.notification_icon))
            .setColor(Color.RED)
            .setContentTitle(header)
            .setContentText(body)
            .setContentIntent(notificationPendingIntent);
        }
        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    public static long findANewMessageId(Context context, int type){
        /* Get available Ids */
        StatsContent sc = StatsContent.getInstance();
        MessageRepoAccess datasource;
        datasource = MessageRepoAccess.getInstance(context);
        datasource.open();
        List<Long> available_ids = datasource.getListOfIDsForMessageType(type);
        datasource.close();

        /* Get taken Ids */
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        List<Long> taken_ids = new ArrayList<Long>(Util.listOfStringsToListOfLongs(Util.getListFromSharedPref(prefs, Constants.TAKEN_MESSAGE_IDS)));

        /* Combine both lists */
        List<Long> common = new ArrayList<Long>(available_ids);
        common.removeAll(taken_ids);

        if (common.size()<1){
            return -1;
        }
        /*Pick a random element*/
        int index = (int)(Math.random()*(common.size()-1));
        return common.get(index);
    }
}

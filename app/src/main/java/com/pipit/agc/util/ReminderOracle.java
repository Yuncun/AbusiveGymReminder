package com.pipit.agc.util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.pipit.agc.R;
import com.pipit.agc.activity.AllinOneActivity;
import com.pipit.agc.data.MsgAndDayRecords;
import com.pipit.agc.data.InsultsRecords;
import com.pipit.agc.data.InsultRecordsConstants;
import com.pipit.agc.model.DayRecord;
import com.pipit.agc.model.Message;
import com.pipit.agc.receiver.AlarmManagerBroadcastReceiver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Suite of functions that delivers reminders based on gym behavior
 */
public class ReminderOracle {
    public static final int POST_TIME_NOISE_HOURS = 2;
    public static final int POST_TIME_NOISE_MINTUES = 60;
    public static final int MORNING_OFFSET = 9;
    public static final int AFTERNOON_OFFSET = 16;
    public static final int EVENING_OFFSET = 20;
    private static final String TAG = "ReminderOracle";

    /**
     * This function chooses a message and figures out a time to post it, based on gym attendance
     * Call this function AFTER moving forward to the new day (it will use yesterday's gym stats).
     */
    public static void doLeaveMessageBasedOnPerformance(Context context, boolean testmode){
        Log.d(TAG, "doLeaveMessageBasedOnPerformance");
        SharedPrefUtil.updateMainLog(context, "doLeaveMessageBasedOnPerformance");

        DayRecord yesterday = StatsContent.getInstance().getYesterday(true);
        DayRecord today = StatsContent.getInstance().getToday(false);
        InsultsRecords messagerepo = InsultsRecords.getInstance(context);
        messagerepo.open();
        Message msg = null;
        if (yesterday==null){
            //First day
            //msg = new Message();
        }
        else if (testmode){
            int type = 0;
            int reason = 0;
            if (yesterday.beenToGym()){
                type = InsultRecordsConstants.REMINDER_HITYESTERDAY;
                reason = Message.HIT_TODAY;
            }else{
                type = InsultRecordsConstants.REMINDER_MISSEDYESTERDAY;
                reason = Message.MISSED_YESTERDAY;
            }
            try{
                Log.d(TAG, "Attempting to get a new message type=" + type);
                long id = findANewMessageId(context, type);
                if (!messagerepo.isOpen()) { messagerepo.open(); }
                if (id < 1){
                    Log.d(TAG, "No id's found, getting random message");
                    msg = messagerepo.getRandomMessageWithParams(type,
                            InsultRecordsConstants.KINDA_ANNOYED);
                    msg.setReason(reason);
                }else{
                    msg = messagerepo.getMessageById(id);
                    msg.setReason(reason);
                }
            } catch (Exception e){
                Log.d(TAG, e.toString());
                SharedPrefUtil.updateMainLog(context, "Failed to add new message:\n" +
                        "    " + e.toString());
                Toast.makeText(context, "New Message failed",
                        Toast.LENGTH_LONG).show();
            }
        }

        /*Not test mode - Same code as above, but we do nothing for yesterday.beenToGym case, and does the waiting*/
        else{
            if (yesterday.beenToGym()) {
                SharedPrefUtil.updateMainLog(context, "You went to the gym yesterday, so no notification will be shown");
                //Currently do nohting
                // Todo: Figure out if we want to leave a message here or not - am exploring the idea of leaving message immediately
                // Todo: when user goes to the gym, which would make this redundant.
            }
            else if (!yesterday.beenToGym() && yesterday.isGymDay()) {
                int type = InsultRecordsConstants.REMINDER_MISSEDYESTERDAY;
                int reason = Message.MISSED_YESTERDAY;
                msg = messagerepo.getRandomMessageWithParams(InsultRecordsConstants.REMINDER_MISSEDYESTERDAY,
                        InsultRecordsConstants.KINDA_ANNOYED);
                msg.setReason(Message.MISSED_YESTERDAY);
                try{
                    //This whole mechanism prevents us from showing the same message over and over again
                    Log.d(TAG, "Attempting to get a new message");
                    long id = findANewMessageId(context, type);
                    if (!messagerepo.isOpen()) { messagerepo.open(); }
                    if (id < 1){
                        Log.d(TAG, "No id's found, getting random message");
                        msg = messagerepo.getRandomMessageWithParams(type,
                                InsultRecordsConstants.KINDA_ANNOYED);
                        msg.setReason(reason);
                    }else{
                        msg = messagerepo.getMessageById(id);
                        msg.setReason(reason);
                    }
                } catch (Exception e){
                    Log.d(TAG, e.toString());
                    SharedPrefUtil.updateMainLog(context, "Failed to add new message:\n" +
                            "    " + e.toString());
                    Toast.makeText(context, "New Message failed",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
        messagerepo.close();

        /*Calculate some noise to add to the time shown*/
        Random rand = new Random();
        int hr_noise = rand.nextInt(POST_TIME_NOISE_HOURS);
        int min_noise = rand.nextInt(POST_TIME_NOISE_MINTUES);
        if (rand.nextBoolean()){
            hr_noise*=-1;
            min_noise*=-1;
        }

        //Init Calendars
        Calendar cal = Calendar.getInstance();

        //Find a time to display message depending on the notification preferences of the user
        int notifpref =  SharedPrefUtil.getInt(context, Constants.PREF_NOTIF_TIME, Constants.NOTIFTIME_AFTERNOON);
        Log.d(TAG, "Preferred notification time is " + notifpref);

        switch (notifpref){
            case Constants.NOTIFTIME_MORNING:
                cal.add(Calendar.HOUR_OF_DAY, MORNING_OFFSET);
                break;
            case Constants.NOTIFTIME_AFTERNOON:
                cal.add(Calendar.HOUR_OF_DAY, AFTERNOON_OFFSET);
                break;
            case Constants.NOTIFTIME_EVENING:
                cal.add(Calendar.HOUR_OF_DAY, EVENING_OFFSET);
                break;
            case Constants.NOTIFTIME_YOLO:
                long time_ms = SharedPrefUtil.getLong(context, "lastgymtime", -1);

                if (time_ms>0){
                    Calendar c2 = Calendar.getInstance();
                    c2.setTimeInMillis(time_ms);
                    cal.set(Calendar.HOUR_OF_DAY, c2.get(Calendar.HOUR_OF_DAY));
                    cal.set(Calendar.MINUTE, c2.get(Calendar.MINUTE));
                    cal.add(Calendar.MINUTE, min_noise);
                    cal.add(Calendar.HOUR_OF_DAY, hr_noise);
                }
                else{
                    cal.add(Calendar.HOUR_OF_DAY, AFTERNOON_OFFSET); //16 hours, a default (arbitrary) time to wait before showing message
                }
                break;
            default:
                cal.add(Calendar.HOUR_OF_DAY, AFTERNOON_OFFSET);
                break;
        }

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);

        if (testmode && msg!=null){
            setLeaveMessageAlarm(context, msg, 0, 3);
        }
        else
        if (msg!=null){
            DateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
            SharedPrefUtil.updateMainLog(context, "Notification set to show at " + dateFormat.format(cal.getTime()) + " with setting  " + notifpref);
            SharedPrefUtil.putLong(context, "nextnotificationtime", cal.getTimeInMillis());
            setLeaveMessageAlarm(context, msg, hour, minutes);
        }
    }

    /**
     * This function leaves a message immediately
     * @param msg
     */
    public static void leaveMessage(Message msg) {
        MsgAndDayRecords datasource;
        datasource = MsgAndDayRecords.getInstance();
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

        DateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
        String settime = dateFormat.format(calendar.getTime());
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        Log.d(TAG, "setLeaveMessageAlarm " + System.currentTimeMillis()
                + " alarm set for " + settime + " message " + m.getBody());
    }

    /**
     * Same as setLeaveMessageAlarm(Context, Calendar) but does the math for you.
     * @param context
     * @param minutes
     */
    private static void setLeaveMessageAlarm(Context context, Message m, int hours, int minutes){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, hours);
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
        InsultsRecords databaseAccess = InsultsRecords.getInstance(context);
        databaseAccess.open();
        Message msg = null;
        msg = databaseAccess.getRandomMessageWithParams(InsultRecordsConstants.REMINDER_HITYESTERDAY,
                InsultRecordsConstants.KINDA_ANNOYED);
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
     *
     * @param context
     * @param header Title (The first thing that is seen)
     * @param body First line of msg body
     * @param body2 Second line
     * @param messageID
     * @param reason Specifies if this is shown for missing/hitting a gym
     */
    public static void showNotification(Context context, String header, String body, String body2, long messageID, int reason){
        Intent notificationIntent = new Intent(context, AllinOneActivity.class);
        if (messageID>0){
            notificationIntent = new Intent(context, AllinOneActivity.class);
            notificationIntent.putExtra(Constants.MESSAGE_ID, messageID);
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(AllinOneActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context);

        if (reason==Message.MISSED_YESTERDAY){
            builder.setSmallIcon(R.drawable.notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.notification_icon))
                //.setColor(Color.RED)
                .setContentIntent(notificationPendingIntent)
                .setContentTitle(header)
                .setStyle(new Notification.BigTextStyle()
                        .bigText(body))
                .setContentText(body);
        }
        else{
            builder.setSmallIcon(R.drawable.notification_icon)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.notification_icon))
                    //.setColor(Color.GREEN)
                .setContentTitle(header)
                .setContentText(body)
                .setStyle(new Notification.BigTextStyle()
                            .bigText(body + "\n" + body2))
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
        InsultsRecords datasource;
        datasource = InsultsRecords.getInstance(context);
        datasource.open();
        List<Long> available_ids = datasource.getListOfIDsForMessageType(type);
        datasource.close();

        /* Get taken Ids */
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        List<Long> taken_ids = new ArrayList<Long>(Util.listOfStringsToListOfLongs(SharedPrefUtil.getListFromSharedPref(prefs, Constants.TAKEN_MESSAGE_IDS)));

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

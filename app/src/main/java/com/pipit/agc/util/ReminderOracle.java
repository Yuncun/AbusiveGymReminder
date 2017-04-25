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

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Suite of functions that delivers reminders based on gym attendance
 */
public class ReminderOracle {
    private static final String TAG = "ReminderOracle";

    //How many hours ahead to set alarm to show notification
    public static final int WAKUP_OFFSET = 3;  //(Three hours offset then first time user opens phone)
    public static final int MORNING_OFFSET = 9;
    public static final int AFTERNOON_OFFSET = 16;
    public static final int EVENING_OFFSET = 20;

    /**
     * This function chooses a message and figures out a time to post it, based on gym attendance
     * The intended use of this method is for setting daily messages.
     * Call this function AFTER moving forward to the new day (it will use yesterday's gym stats).
     */
    public static void doLeaveMessageBasedOnPerformance(Context context, boolean testmode){
        Log.d(TAG, "doLeaveMessageBasedOnPerformance");
        SharedPrefUtil.updateMainLog(context, "doLeaveMessageBasedOnPerformance");

        DayRecord yesterday = StatsContent.getInstance().getYesterday(true);
        InsultsRecords messagerepo = InsultsRecords.getInstance(context);
        messagerepo.open();
        Message msg = null;

        long id = -1;
        //Test mode shows a negative notification immediately. It is designed for testing, but is also used for
        //showing sample messages
        if (testmode){
            int type = InsultRecordsConstants.REMINDER_MISSEDYESTERDAY;
            int reason = Message.MISSED_YESTERDAY;

            try{
                Log.d(TAG, "Attempting to get a new message type=" + type);
                id = findANewMessageId(context, type);
                if (!messagerepo.isOpen()) { messagerepo.open(); }
                if (id < 1){
                    Log.d(TAG, "No id's found, getting random message");
                    msg = messagerepo.getRandomMessageWithParams(type,
                            SharedPrefUtil.getInt(context, Constants.MATURITY_LEVEL, InsultRecordsConstants.MED_MATURITY));
                    msg.setReason(reason);
                }else{
                    msg = messagerepo.getMessageById(id);
                    msg.setReason(reason);
                }
            } catch (Exception e){
                Log.d(TAG, e.toString());
                SharedPrefUtil.updateMainLog(context, "Failed to add new message:\n" +
                        "    " + e.toString());
                Toast.makeText(context, "Failed to show notification",
                        Toast.LENGTH_LONG).show();
            }
        }
        else if (yesterday==null){
            //First day
            //Do nothing for now
        }

        /*Not test mode - Same code as above, but we do nothing for yesterday.beenToGym case, and does the waiting*/
        else{
            if (yesterday.beenToGym()) {
                SharedPrefUtil.updateMainLog(context, "You went to the gym yesterday, so no notification will be shown");
            }
            else if (!yesterday.beenToGym() && yesterday.isGymDay()) {
                int type = InsultRecordsConstants.REMINDER_MISSEDYESTERDAY;
                int reason = Message.MISSED_YESTERDAY;
                msg = messagerepo.getRandomMessageWithParams(InsultRecordsConstants.REMINDER_MISSEDYESTERDAY,
                        SharedPrefUtil.getInt(context, Constants.MATURITY_LEVEL, InsultRecordsConstants.MED_MATURITY));
                msg.setReason(Message.MISSED_YESTERDAY);
                try{
                    //This whole mechanism prevents us from showing the same message over and over again
                    Log.d(TAG, "Attempting to get a new message");
                    id = findANewMessageId(context, type);
                    if (!messagerepo.isOpen()) { messagerepo.open(); }
                    if (id < 1){
                        Log.d(TAG, "No id's found, getting random message");
                        msg = messagerepo.getRandomMessageWithParams(type,
                                SharedPrefUtil.getInt(context, Constants.MATURITY_LEVEL, InsultRecordsConstants.MED_MATURITY));
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

        //Init Calendars
        DateTime dt = new DateTime();

        //Find a time to display message depending on the notification preferences of the user
        int notifpref =  SharedPrefUtil.getInt(context, Constants.PREF_NOTIF_TIME, Constants.NOTIFTIME_ON_WAKEUP);

        switch (notifpref){
            case Constants.NOTIFTIME_MORNING:
                dt = dt.plusHours(MORNING_OFFSET);
                break;
            case Constants.NOTIFTIME_AFTERNOON:
                dt = dt.plusHours(AFTERNOON_OFFSET);
                break;
            case Constants.NOTIFTIME_EVENING:
                dt = dt.plusHours(EVENING_OFFSET);
                break;
            case Constants.NOTIFTIME_ON_WAKEUP:
            default:
                dt = dt.plusHours(WAKUP_OFFSET);
                break;
        }

        Log.d(TAG, "Preferred notification time is " + dt.toString("MM-dd HH:mm:ss"));

        if (testmode && msg!=null){
            NotificationUtil.showNotification(context, msg.getHeader(), msg.getBody(), "Simulating a missed gym day", "", Message.MISSED_YESTERDAY, id);
            ReminderOracle.leaveMessage(msg); //Comment this out if test messages shouldn't leave in inbox
        }
        else
        if (msg!=null){
            SharedPrefUtil.updateMainLog(context, "Notification set to show at " + dt.toString("MM-dd HH:mm:ss")
                    + " with notif pref  " + notifpref);
            SharedPrefUtil.putLong(context, "nextnotificationtime", dt.getMillis());
            setLeaveMessageAlarm(context, msg, dt);
        }
    }

    /**
     * Finds a new message and retrieves it from database.
     * A wrapper for findANewMessageId, that includes the action of retrieving the message
     * @param context
     * @param type - Info for database retrieval ex: InsultRecordsConstants.REMINDER_MISSEDYESTERDAY etc
     * @param reason - Info for message classifying ex: Message.MISSED_YESTERDAY etc
     * @return A message selected by findANewMessage , or null if unsuccessful
     */
    public static synchronized Message getANewMessage(Context context, int type, int reason){
        InsultsRecords messagerepo = InsultsRecords.getInstance(context);
        messagerepo.open();
        Message msg = null;
        long id = -1;
        try{
            Log.d(TAG, "Attempting to get a new message type=" + type);
            id = findANewMessageId(context, type);
            if (!messagerepo.isOpen()) { messagerepo.open(); }
            if (id < 1){
                Log.d(TAG, "No id's found, getting random message");
                msg = messagerepo.getRandomMessageWithParams(type,
                        SharedPrefUtil.getInt(context, Constants.MATURITY_LEVEL, InsultRecordsConstants.MED_MATURITY));
                msg.setReason(reason);
            }else{
                msg = messagerepo.getMessageById(id);
                msg.setReason(reason);
            }
        } catch (Exception e){
            Log.d(TAG, e.toString());
            SharedPrefUtil.updateMainLog(context, "Failed to add new message:\n" +
                    "    " + e.toString());
            Toast.makeText(context, "Failed to show notification",
                    Toast.LENGTH_LONG).show();
        }
        return msg;
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
    private static void setLeaveMessageAlarm(Context context, Message m, DateTime calendar){
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmManagerBroadcastReceiver.class);
        i.putExtra("purpose", "leavemessage");
        i.putExtra("message", m.toJson());
        PendingIntent pi = PendingIntent.getBroadcast(context, 3, i, PendingIntent.FLAG_CANCEL_CURRENT);

        String settime =calendar.toString("MM-dd HH:mm:ss");
        am.set(AlarmManager.RTC_WAKEUP, calendar.getMillis(), pi);
        Log.d(TAG, "setLeaveMessageAlarm " + System.currentTimeMillis()
                + " alarm set for " + settime + " message " + m.getBody());
    }

    /**
     * Same as setLeaveMessageAlarm(Context, Calendar) but does the math for you.
     * @param context
     * @param m
     * @param hours
     * @param minutes
     */
    private static void setLeaveMessageAlarm(Context context, Message m, int hours, int minutes){
        DateTime dt = new DateTime();
        dt.plusHours(hours);
        dt.plusMinutes(minutes);
        setLeaveMessageAlarm(context, m, dt);
    }

    /**
     * This function is analogous to doLeaveMessageBasedOnPerformance, but is used to show a positive message after the
     * user arrives at the gym. The message can be delivered immediately (on gym arrival) or after a delay so the user
     * receives it presumably after his or her workout
     */
    public static void doLeaveOnGymArrivalMessage(Context context, boolean immediate){
        Log.d(TAG, "doLeaveOnGymArrivalMessage(Context, " + immediate + ")");
        int maturity = SharedPrefUtil.getInt(context, Constants.MATURITY_LEVEL, InsultRecordsConstants.MED_MATURITY);
        InsultsRecords databaseAccess = InsultsRecords.getInstance(context);
        databaseAccess.open();
        Message msg = null;
        msg = databaseAccess.getRandomMessageWithParams(InsultRecordsConstants.REMINDER_HITYESTERDAY, maturity);
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
     * Format the notification and call shownotification.
     * This is subject to styling changes
     *
     * If this is for a Missed Yesterday, it should be called on the day after the missed day
     * @param context
     * @param m
     */
    public static void showNotificationFromMessage(Context context, Message m){
        String firstLineBody = "";
        String title = "";
        String reason_line = "";
        long msgid = -1;
        int reason = Message.NO_RECORD;
        String attribution = "";
        LocalDate today = new LocalDate();
        //Construct notification message and show
        switch (m.getReason()){
            case Message.MISSED_YESTERDAY:
                title = m.getHeader();
                firstLineBody = m.getBody();
                msgid = m.getId();
                reason = Message.MISSED_YESTERDAY;

                //Get yesterday's date string
                LocalDate yesterday = today.minusDays(1);
                reason_line = yesterday.toString("E, MMM d") + " was a gym day";
                break;
            case Message.HIT_YESTERDAY:
            case Message.HIT_TODAY:
                if (m.getBody()==null || m.getBody().isEmpty()){
                    title = context.getString(R.string.recordingvisit);
                    //Following line is commented out as long as we are using onGoing notifications
                    //title = context.getString(R.string.reason_hit_gym);
                    firstLineBody = m.getHeader();
                }else{
                    title = m.getHeader();
                    firstLineBody = m.getBody();
                }
                reason = Message.HIT_TODAY;
                DateFormat df = new SimpleDateFormat("HH:mm, MMM d ");
                reason_line = context.getString(R.string.gym_register_prefix) + " " + df.format(Calendar.getInstance().getTime());
                //For hit gym, use the custom "you are at the gym" notification
                NotificationUtil.showGymVisitingNotification(context, title, firstLineBody);
                return;
            case Message.NO_RECORD:
            default:
                title = m.getHeader();
                firstLineBody = m.getBody();
                msgid = m.getId();
                reason = Message.NEW_MSG;
        }
        NotificationUtil.showNotification(context, title, firstLineBody, reason_line, attribution, reason, msgid);
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

    /**
     * This is called to leave a message when we hit the gym. It is essentially a wrapper function for
     * @ReminderOracle.doLeaveOnGymArrivalMessage with additional checks for settings and filtering.
     * @param context
     * @return
     */
    public static boolean conditionalLeaveVisitingMessage(Context context){
        SharedPrefUtil.updateMainLog(context, "Received a gym visit event");

        //Check if gym visit notifications are enabled
        if (!SharedPrefUtil.getBoolean(context, Constants.PREF_SHOW_NOTIF_ON_GYMHITS, true)){
            Log.d(TAG, "Gym Visit received. Not sending message - gym arrival msgs disabled ");
            SharedPrefUtil.updateMainLog(context, "Gym Visit received. Not sending message - gym arrival msgs disabled ");
            return false;
        }

        //In order to prevent spamming the user with "Gym Registered" notifications, check if our last was
        //less than some given time ago. If this is so, don't make a new message
        long lastVisitInMillis = SharedPrefUtil.getLong(context, Constants.PREF_GET_LAST_EXIT_TIME, 0);
        long currentTimeInMillis = Calendar.getInstance().getTimeInMillis();
        long diffInMinutes = (currentTimeInMillis  - lastVisitInMillis) / 60000;
        Log.d(TAG, "Last gym visit was " + diffInMinutes + " minutes ago ");
        if (lastVisitInMillis != 0 && diffInMinutes < ((long) Constants.MIN_TIME_BETWEEN_VISITS) ){
            NotificationUtil.showGymVisitingNotification(context, context.getString(R.string.youareatthegym), "", false);
            Log.d(TAG, "Gym Visit received, not sending a message because we sent one "
                    + diffInMinutes + " minutes ago");
            SharedPrefUtil.updateMainLog(context, "Gym Visit received, not sending a message because we sent one "
                    + diffInMinutes + " minutes ago");
            return false;
        }

        //If all conditions are met, send the message
        ReminderOracle.doLeaveOnGymArrivalMessage(context, true);
        //Remember this
        SharedPrefUtil.putLong(context, Constants.PREF_GET_LAST_EXIT_TIME, Calendar.getInstance().getTimeInMillis());
        return true;
    }
}

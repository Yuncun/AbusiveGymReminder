package com.pipit.agc.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.pipit.agc.activity.AllinOneActivity;
import com.pipit.agc.data.MsgAndDayRecords;
import com.pipit.agc.model.DayRecord;
import com.pipit.agc.R;
import com.pipit.agc.util.ReminderOracle;
import com.pipit.agc.util.SharedPrefUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Eric on 2/21/2016.
 */
public class GeoFenceTransitionsIntentReceiver extends BroadcastReceiver {
    protected static final String TAG = "GeofenceReceiver";
    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG, "Received a geofence event");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = Integer.toString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        /* GYM DAY REGISTERED */
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL)
        {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    context,
                    geofenceTransition,
                    triggeringGeofences
            );

            //Log the transition details.
            SharedPrefUtil.updateMainLog(context, "Geofence dwell") ;

            //Update gym status today
            updateLastDayRecord(context);
            rememberGymHabits(context);
            ReminderOracle.doLeaveOnGymArrivalMessage(context, true);
            //sendNotification("GEO FENCE FROM SERVICE", context);

            //Update last visited time
            long time= System.currentTimeMillis();
            SharedPrefUtil.updateLastVisitTime(context, time);

            //Open a visiting counter on this day
            MsgAndDayRecords datasource;
            datasource = MsgAndDayRecords.getInstance();
            datasource.openDatabase();
            DayRecord today = datasource.getLastDayRecord();
            today.startCurrentVisit();
            datasource.updateLatestDayRecordVisits(today.getSerializedVisitsList());
            datasource.closeDatabase();
        }
        else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT){
            //Open a visiting counter on this day
            MsgAndDayRecords datasource;
            datasource = MsgAndDayRecords.getInstance();
            datasource.openDatabase();
            DayRecord today = datasource.getLastDayRecord();
            today.endCurrentVisit();
            datasource.updateLatestDayRecordVisits(today.getSerializedVisitsList());
            datasource.closeDatabase();
            SharedPrefUtil.updateMainLog(context, "Geofence exit");
        }
        else {
            Log.e(TAG, "geofenceTransition error");
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param context               The app context.
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
    private String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);
        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private void sendNotification(String notificationDetails, Context context) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, AllinOneActivity.class);

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

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.notification_icon)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.notification_icon))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText("GEO FENCE")
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "Geofence entered";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "Geofence exited";
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return "Geofence dwell";
            default:
                return "Unknown geofence transition";
        }
    }

    private void updateLastDayRecord(Context context){
        synchronized (this){
            MsgAndDayRecords datasource = MsgAndDayRecords.getInstance();
            AllinOneActivity.updateDate(context);
            datasource.openDatabase();
            if (!datasource.getLastDayRecord().beenToGym()){
                datasource.updateLatestDayRecordBeenToGym(true);
            }
            MsgAndDayRecords.getInstance().closeDatabase();
        }
    }

    /**
     * Call this function to remember what time the user went to the gym
     * Todo: Make this function more intelligent, perhaps by looking up stats in DayRecords
     */
    public static void rememberGymHabits(Context context){
        SharedPrefUtil.putLong(context, "lastgymtime", Calendar.getInstance().getTimeInMillis());
    }
}
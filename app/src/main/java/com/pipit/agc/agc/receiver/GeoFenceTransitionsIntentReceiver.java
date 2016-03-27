package com.pipit.agc.agc.receiver;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.R;
import com.pipit.agc.agc.activity.AllinOneActivity;
import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.util.ReminderOracle;
import com.pipit.agc.agc.util.SharedPrefUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
        Log.d(TAG, "onRECEIVE");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = Integer.toString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL)
        {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    context,
                    geofenceTransition,
                    triggeringGeofences
            );

            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails, context);
            Log.i(TAG, geofenceTransitionDetails);
            //Update logs
            SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
            SharedPreferences.Editor editor = prefs.edit();
            String lastLocation = prefs.getString("locationlist", "none");
            String mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            String body = lastLocation+"\n" + geofenceTransitionDetails + " " + mLastUpdateTime;
            editor.putString("locationlist", body).commit();

            //Update gym status today
            updateLastDayRecord();
            rememberGymHabits(context);
            ReminderOracle.doLeaveOnGymArrivalMessage(context, true);
            sendNotification("GEO FENCE FROM SERVICE", context);
        } else {
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

    private void updateLastDayRecord(){
        synchronized (this){
            DBRecordsSource datasource = DBRecordsSource.getInstance();
            datasource.openDatabase();
            if (!datasource.getLastDayRecord().beenToGym()){
                datasource.updateLatestDayRecordBeenToGym(true);
            }
            DBRecordsSource.getInstance().closeDatabase();
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
package com.pipit.agc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.pipit.agc.activity.AllinOneActivity;
import com.pipit.agc.data.MsgAndDayRecords;
import com.pipit.agc.model.DayRecord;
import com.pipit.agc.util.Constants;
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
            SharedPrefUtil.updateMainLog(context, "Error in geofence transition - errorcode " + errorMessage);
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        Location loc = geofencingEvent.getTriggeringLocation();

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
            Log.d(TAG, "geofence dwell, geofenceTransitionDetails: " + geofenceTransitionDetails);
            Log.d(TAG, "geofence dwell at \n Lat: " + loc.getLatitude() + "\n Lng: " + loc.getLongitude());
            SharedPrefUtil.updateMainLog(context, "Geofence dwell at \n" +
                    " Lat: " + loc.getLatitude() + "\n Lng: " + loc.getLongitude());

            //Update gym status today
            updateLastDayRecord(context);
            rememberGymHabits(context);

            //In order to prevent spamming the user with "Gym Registered" notifications, we check a flag to see
            //if we've recently sent one. This may happen because if geofences are unreliable (user will bounce in and out).
            if (SharedPrefUtil.getBoolean(context, Constants.PREF_SHOW_HIT_NOTIFS_TODAY, true)){
                ReminderOracle.doLeaveOnGymArrivalMessage(context, true);
                SharedPrefUtil.putBoolean(context, Constants.PREF_SHOW_HIT_NOTIFS_TODAY, false); //Show no more registers today
            }else{
                SharedPrefUtil.updateMainLog(context, "Received a gym visit event but not sending another notification");
            }

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
            Log.d(TAG, "geofence exit at \n Lat: " + loc.getLatitude() + "\n Lng: " + loc.getLongitude());
            SharedPrefUtil.updateMainLog(context, "Geofence exit at \n" +
                    " Lat: " + loc.getLatitude() + "\n Lng: " + loc.getLongitude());
        }
        else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
            //This is only done for logging purposes.
            Log.d(TAG, "geofence enter at \n Lat: " + loc.getLatitude() + "\n Lng: " + loc.getLongitude());
            SharedPrefUtil.updateMainLog(context, "Geofence enter at \n" +
                    " Lat: " + loc.getLatitude() + "\n Lng: " + loc.getLongitude());
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
package com.pipit.agc.agc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.PowerManager;
import android.util.Log;

import com.pipit.agc.agc.data.DBRecordsSource;

import java.text.DateFormat;
import java.util.Date;

public class ProximityReceiver extends BroadcastReceiver {
    public static final String EVENT_ID_INTENT_EXTRA = "EventIDIntentExtraKey";
    private static final String TAG = "ProximityReceiver";

    @Override
    public void onReceive(Context context, Intent arg1) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        String k = LocationManager.KEY_PROXIMITY_ENTERING;
        boolean state = arg1.getBooleanExtra(k, false);

        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = prefs.edit();

        String enterOrLeaveStr;
        if (state) {
            enterOrLeaveStr="entered";
            AlarmManagerBroadcastReceiver alarm = new AlarmManagerBroadcastReceiver();
            markEnterSharedPref(context, true);
            alarm.setAlarmForLocationLog(context);
        } else {
            enterOrLeaveStr="exited";
            markEnterSharedPref(context, false);
        }
        Log.d(TAG, "onReceive of PROXIMITY RECEIVER " + enterOrLeaveStr);

        String mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        String lastLocation = prefs.getString("locationlist", "none");
        String body = lastLocation+"\n" + enterOrLeaveStr + " at " +
                mLastUpdateTime;
        editor.putString("locationlist", body);
        Log.d(TAG, "Proximity Alert just entered last location into sharedprefs");

        editor.commit();
        wl.release();
    }

    /**
     * If jsutEntered is marked as "entered" when the prox alert is checked after the two minute wait,
     * then we will accept the alert (see AlarmManagerBroadcastREceiver)
     *
     * Otherwise, we see that the GPS has corrected a false "enter" and do not take action.
     * @param context
     * @param entered
     */
    public static void markEnterSharedPref(Context context, boolean entered){
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("justEntered", entered).commit();
    }
}
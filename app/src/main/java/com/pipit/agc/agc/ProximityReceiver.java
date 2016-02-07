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

        String enterOrLeaveStr;
        boolean enteringFlag;
        if (state) {
            enterOrLeaveStr="entered";
            markEnterSharedPref(context, true);
            AlarmManagerBroadcastReceiver alarm = new AlarmManagerBroadcastReceiver();
            alarm.setAlarmForLocationLog(context);
            enteringFlag=true;
        } else {
            enterOrLeaveStr="exited";
            markEnterSharedPref(context, false);
            enteringFlag=false;
        }
        Log.d(TAG, "onReceive of PROXIMITY RECEIVER " + enterOrLeaveStr);

        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = prefs.edit();

        String mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        String lastLocation = prefs.getString("locationlist", "none");
        String body = lastLocation+"\n" + enterOrLeaveStr + " at " +
                mLastUpdateTime;
        editor.putString("locationlist", body);
        Log.d(TAG, "Proximity Alert just entered last location into sharedprefs");

        editor.commit();
        wl.release();
    }

    private Location getCurrentLocation(Context context){
        LocationManager locationManager;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(criteria, true);
        String gpsMsg = "";
        Location lc = new Location("");

        if (null != provider)
        {
            Location location = locationManager.getLastKnownLocation(provider);
            if (null != location) {
                double currentLat = location.getLatitude();
                double currentLon = location.getLongitude();
                gpsMsg = "curr location is " + currentLat + " " + currentLon;
                lc.setLongitude(currentLon);
                lc.setLatitude(currentLat);
            }
            else
            {
                gpsMsg="Current Location can not be resolved!";
            }
        }
        else
        {
            gpsMsg="Provider is not available!";
        }
        Log.d(TAG, "getCurrentLocation: " + gpsMsg);
        return lc;
    }

    private boolean sanitycheck(int i, int range){
        if (i>(range*2)){
            return false;
        }else return true;
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
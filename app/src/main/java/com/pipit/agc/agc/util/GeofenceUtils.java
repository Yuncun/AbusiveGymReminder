package com.pipit.agc.agc.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.pipit.agc.agc.model.Gym;

/**
 * Created by Eric on 2/27/2016.
 */
public class GeofenceUtils {
    private static final String TAG = "GeofenceUtils";
    public static void addGeofenceToSharedPrefs(Context context, Gym gym){
        int id = gym.proxid;
        //Add geofence to sharedprefs - Todo: Move this to a sane location
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        float range = (float) prefs.getInt("range", -1);
        if (range < 0){
            prefs.edit().putFloat("range", (float) Constants.DEFAULT_RADIUS);
            range = Constants.DEFAULT_RADIUS;
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("proxalert"+id, gym.proxid).commit();
        Util.putDouble(editor, Constants.DEST_LAT + id, gym.location.getLatitude());
        Util.putDouble(editor, Constants.DEST_LNG+id, gym.location.getLongitude());
        editor.putString("address"+id, gym.address).commit();
        editor.putString("name"+id, gym.name).commit();

        Log.d(TAG, "Adding prox alert, ID is " + gym.proxid + " range is " + range);
        editor.commit();
    }

}

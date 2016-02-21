package com.pipit.agc.agc;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * Created by Eric on 2/21/2016.
 */
public class LocationUtils {
    private static final String TAG = "LocationUtils";

    public static Location getCurrentLocation(Context context){
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

    public static boolean sanitycheck(int i, int range){
        if (i>(range*2)){
            return false;
        }else return true;
    }
}

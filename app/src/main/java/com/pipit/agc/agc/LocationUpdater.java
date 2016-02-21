package com.pipit.agc.agc;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Eric on 2/20/2016.
 */
public class LocationUpdater implements LocationListener {
    private static final String TAG = "LocationUpdater";
    LocationManager mLocationManager;
    Context context;

    public void requestLocation(Context context){
        this.context=context;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        /*
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
            // Do something with the recent location fix
            //  otherwise wait for the update below
        }
        else {*/
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        //}
        Log.d(TAG, "Started a request for location update");
    }
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.v("Location Changed", location.getLatitude() + " and " + location.getLongitude());
            mLocationManager.removeUpdates(this);
            Util.sendNotification(context, "Location Update", "Entered proximity at " + new Date());

        }
    }

    // Required functions
    public void onProviderDisabled(String arg0) {}
    public void onProviderEnabled(String arg0) {}
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        Log.d(TAG, "Update location " + arg0 + arg1);
    }
}

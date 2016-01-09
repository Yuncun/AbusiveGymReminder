package com.pipit.agc.agc;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;


public class LocationActivity extends Activity  implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public boolean active=false;
    AllinOneActivity _allinOneActivity;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    String TAG = "LocationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        /*Handle Google stuff*/
        initGoogleApiClient();

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        //Not sure what to do here
    }

    @Override
    public void onConnectionFailed(ConnectionResult c) {
        //Not sure what to do here
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    private void initGoogleApiClient(){
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            createLocationRequest();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged" + location);
        mLastLocation = location;
        double currlat = location.getLatitude();
        double currlng = location.getLongitude();

        /*Check lat/lng*/
        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        double lat = Util.getDouble(prefs, "lat", currlat);
        double lng = Util.getDouble(prefs, "lng", currlng);

        Location gymLocation = new Location("");
        gymLocation.setLatitude(lat);
        gymLocation.setLongitude(lng);


        float distance = gymLocation.distanceTo(location);

        String mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

/*
        ((LandingFragment)AllinOneActivity._fragments.get(0)).updateLastLocation(
                (getLandingFragment().getLastLocationText())+ "\n" + mLastUpdateTime
                        + " Lat:" + currlat + " Lng:" + currlng + " Dist:" + distance);
*/
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }


    protected void onStart() {
        mGoogleApiClient.connect();
        active=true;
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        active=false;
        super.onStop();
    }

}

package com.pipit.agc.agc.controller;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.pipit.agc.agc.R;
import com.pipit.agc.agc.model.Gym;
import com.pipit.agc.agc.receiver.GeoFenceTransitionsIntentReceiver;
import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.util.GeofenceUtils;
import com.pipit.agc.agc.util.SharedPrefUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Eric on 6/13/2016.
 */
public class GeofenceController {
    private final String TAG = "GeofenceController";

    private Context context;

    private ArrayList<Geofence> mGeofenceList;
    private Geofence mNextGeofenceToAdd;
    private ArrayList<String> mRemoveList;
    private HashMap<Integer, Geofence> mGeofenceMap;
    private PendingIntent mGeofencePendingIntent;
    private SharedPreferences prefs;
    private boolean updateGeofencesWhenReadyFlag = false;
    private GeofenceControllerListener mListener;

    protected GoogleApiClient mGoogleApiClient;

    private static GeofenceController INSTANCE;

    public static GeofenceController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GeofenceController();
        }
        return INSTANCE;
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
        mGeofenceList = new ArrayList<Geofence>();
        prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);

        removeAllGeofences(mRestartListsListener);
    }


    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
     */
    private void connectWithCallback(GoogleApiClient.ConnectionCallbacks callbacks) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(callbacks)
                .addOnConnectionFailedListener(mConnectionFailedListener)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void populategeofencelist() {
        Log.d(TAG, "PopulateGeofenceList");
        if (mGeofenceMap != null && mGeofenceList != null) {
            return;
        }
        mGeofenceList = new ArrayList<Geofence>();
        mGeofenceMap = new HashMap<Integer, Geofence>();
        for (int i = 1; i < Constants.GYM_LIMIT; i++) {
            addGeofenceByGym(getGymLocation(context, i), null);
        }
    }

    public void addGeofenceByGym(Gym gym, GeofenceControllerListener listener) {
        Log.d(TAG, "addGeoFenceFromListPosition n=" + gym.proxid + " : " + gym.name);
        if (mGeofenceList==null){
            this.mGeofenceList = new ArrayList<Geofence>();
            this.mGeofenceMap = new HashMap<Integer, Geofence>();
        }
        if (gym == null || gym.location==null || gym.location.getLatitude() == Constants.DEFAULT_COORDINATE
                || gym.location.getLongitude() == Constants.DEFAULT_COORDINATE){
            Log.d(TAG, "addGeoFenceFromListPosition gym retrieved from " + gym.proxid + " is null");
            return;
        }
        Geofence g = new Geofence.Builder()
                .setRequestId(Integer.toString(gym.proxid))
                .setCircularRegion(
                        gym.location.getLatitude(),
                        gym.location.getLongitude(),
                        Constants.DEFAULT_RADIUS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(1000 * 60 * 1)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT)
                        //.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        //        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
        mGeofenceList.add(g);
        mGeofenceMap.put(gym.proxid, g);
        mNextGeofenceToAdd = g;
        GeofenceUtils.addGeofenceToSharedPrefs(context, gym);
        if (listener!=null){
            mListener = listener;
        }
        connectWithCallback(mAddConnectionCallback);
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }

        Intent intent = new Intent(context, GeoFenceTransitionsIntentReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    /**
     * Removes ALL geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
    public void removeAllGeofences( GoogleApiClient.ConnectionCallbacks callback) {
        Log.d(TAG, "Remove all geofences");
        mRemoveList = new ArrayList<String>();

        for (int i = 1; i < Constants.GYM_LIMIT; i++ ){
            mRemoveList.add(Integer.toString(i));
        }

        connectWithCallback(callback);
    }

    public void removeGeofenceSharedPrefs(int i){
        int proxid = prefs.getInt("proxalert" + i, 0); //Todo: Remember individual IDs
        Log.d(TAG, "Attempting to remove prox alert, id is " + proxid);
        try{
            SharedPreferences.Editor edit = prefs.edit();
            edit.remove("lat"+i);
            edit.remove("lng"+i);
            edit.remove("proxalert"+i);
            edit.remove("address" + i);
            edit.remove("name" + i);
            edit.commit();
        } catch (SecurityException e){
            Log.e(TAG, "No permission");
        }
    }

    /**
     * Used for removing individual geofences
     * @param n: id of the alert to remove. (For three max gyms, the id will be 1, 2, or 3.
     */
    public void removeGeofencesById(int n, GeofenceControllerListener listener){
        if (n < 0 || n>Constants.MAX_NUMBER_OF_GYMS){
            Log.e(TAG, "No geofence of given id to remove");
            return;
        }
        if (!mGeofenceMap.containsKey(n)){
            Toast.makeText(context, "No geofence of id " + n + " exists", Toast.LENGTH_SHORT);
        }

        mRemoveList = new ArrayList<>();
        mRemoveList.add(Integer.toString(n));
        mListener = listener;
        connectWithCallback(mRemoveConnectionListener);
    }

    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }


    /**
     * Utility function for getting the location of the gym
     * @return Gym if a gym is found, and null if gym is not found
     */
    public static Gym getGymLocation(Context context, int i){
        /*Check lat/lng*/
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        double lat = SharedPrefUtil.getDouble(prefs, "lat" + i, Constants.DEFAULT_COORDINATE);
        double lng = SharedPrefUtil.getDouble(prefs, "lng" + i, Constants.DEFAULT_COORDINATE);

        Location gymLocation = new Location("");
        gymLocation.setLatitude(lat);
        gymLocation.setLongitude(lng);
        Gym gym = new Gym();
        gym.location = gymLocation;
        gym.address = prefs.getString("address"+i, context.getResources().getString(R.string.no_address_default));
        gym.name = prefs.getString("name"+i, "");
        gym.proxid = prefs.getInt("proxalert"+i, 0);
        return gym;
    }

    /**
     * Builds and returns a GeofencingRequest. This just adds the current geofence in escrow
     * It can also accept an entire list, but we aren't using that now
     */
    private GeofencingRequest getGeofencingRequestForAdd() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        //builder.addGeofences(mGeofenceList);
        builder.addGeofence(mNextGeofenceToAdd);
        return builder.build();
    }

    private GoogleApiClient.ConnectionCallbacks mAddConnectionCallback = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle connectionHint) {
            Log.d(TAG, "mAddConnectionCallback Attempting");
            if (!mGoogleApiClient.isConnected()) {
                updateGeofencesWhenReadyFlag = true; //GoogleApiClient takes a bit to initialize
                return;
            }
            try {
                LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                        getGeofencingRequestForAdd(),
                        getGeofencePendingIntent()
                ).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            saveGeofence();
                            //Log.d(TAG, "mAddConnectionCallback success " + mNextGeofenceToAdd.getRequestId());
                            mNextGeofenceToAdd=null;
                            //Toast.makeText(this, "Add/Removed Geofence", Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMessage = "Some error in onResult";
                            sendError();
                            Log.e(TAG, errorMessage);
                        }
                    }
                });
            } catch (SecurityException securityException) {
                logSecurityException(securityException);
            }
        }

        public void onConnectionSuspended(int cause) {
            // The connection to Google Play services was lost for some reason.
            Log.i(TAG, "Connection suspended");
        }
    };


    private GoogleApiClient.ConnectionCallbacks mRemoveConnectionListener = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            if (mRemoveList!=null && mRemoveList.size() > 0) {
                PendingResult<Status> result = LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, mRemoveList);
                result.setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.d(TAG, "cleared geofence list");
                        if (status.isSuccess()) {
                            for (int k = 0; k < mRemoveList.size(); k++) {
                                int i = Integer.parseInt(mRemoveList.get(k));
                                removeGeofenceSharedPrefs(i);
                                saveGeofence();
                            }
                        } else {
                            sendError();
                            Log.e(TAG, "Removing geofence failed: " + status.getStatusMessage());
                        }
                    }
                });
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.e(TAG, "Connecting to GoogleApiClient suspended.");
        }
    };

    private GoogleApiClient.ConnectionCallbacks mRestartListsListener = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            if (mRemoveList!=null && mRemoveList.size() > 0) {
                PendingResult<Status> result = LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, mRemoveList);
                result.setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.d(TAG, "cleared geofence list");
                        if (status.isSuccess()) {
                            for (int k = 0; k < mRemoveList.size(); k++) {
                                int i = Integer.parseInt(mRemoveList.get(k));
                                //removeGeofenceSharedPrefs(i);
                            }
                            populategeofencelist();

                        } else {
                            sendError();
                            Log.e(TAG, "Removing geofence failed: " + status.getStatusMessage());
                        }
                    }
                });
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.e(TAG, "Connecting to GoogleApiClient suspended.");
        }
    };



    private GoogleApiClient.OnConnectionFailedListener mConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {

        public void onConnectionFailed(ConnectionResult result) {
            // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
            // onConnectionFailed.
            Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
        }
    };

    public interface GeofenceControllerListener {
        void onGeofencesUpdated();
        void onError();
    }

    private void sendError() {
        if (mListener != null) {
            mListener.onError();
        }
    }

    private void saveGeofence() {
        Log.d(TAG, "saveGeofence attempt ");
        if (mListener != null) {
            Log.d(TAG, "saveGeofence success ");
            mListener.onGeofencesUpdated();
        }
    }

}

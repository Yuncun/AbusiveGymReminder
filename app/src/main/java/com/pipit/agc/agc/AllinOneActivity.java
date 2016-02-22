package com.pipit.agc.agc;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.data.DayRecord;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AllinOneActivity extends AppCompatActivity implements StatisticsFragment.OnListFragmentInteractionListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    private static String TAG = "AllinOneActivity";

    SectionsPagerAdapter mSectionsPagerAdapter;
    private AlarmManagerBroadcastReceiver _alarm;
    ViewPager mViewPager;

    protected ArrayList<Geofence> mGeofenceList;
    private boolean mGeofencesAdded;
    private PendingIntent mGeofencePendingIntent;
    protected GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main_layout);

        /*Launch Intro Activity*/
        //Intent intent = new Intent(this, IntroductionActivity.class);
        //startActivity(intent);

        /*Paging for landing screen*/
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1);

        Log.d(TAG, "remaking _alarmmanager " + _alarm);
        _alarm = new AlarmManagerBroadcastReceiver();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, Constants.DAY_RESET_HOUR);
        calendar.set(Calendar.MINUTE, Constants.DAY_RESET_MINUTE);
        calendar.add(Calendar.DATE, 1);
        _alarm.setAlarmForDayLog(getApplicationContext(), calendar);


        buildGoogleApiClient();
        mGeofenceList = new ArrayList<Geofence>();
        populategeofencelist();
        //addGeofenceFromListposition(2);
        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivityForResult(i, 0);
            return true;
        }
        else if (id == R.id.action_showcalendar){
            Intent i = new Intent(this, IndividualSettingActivity.class);
            i.putExtra("fragment", "DayPickerFragment");
            startActivityForResult(i, 1);
        }
        else if (id == R.id.action_showgymstatus){
            SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
            prefs.edit().putBoolean("showGymStatus", true).commit();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return StatisticsFragment.newInstance(0);
                case 1:
                    return NewsfeedFragment.newInstance();
                case 2:
                    return DayOfWeekPickerFragment.newInstance(2);
                case 3:
                    return PlacePickerFragment.newInstance();
                case 4:
                    return LocationListFragment.newInstance(1);
                default:
                    return NewsfeedFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }

    protected void onStart() {
        /*Make sure that we are up to date*/
        try {
            DBRecordsSource datasource = DBRecordsSource.getInstance();
            datasource.openDatabase();
            synchronized (datasource) {
                DayRecord lastDate = datasource.getLastDayRecord();

                DayRecord todaysDate = new DayRecord();
                todaysDate.setDate(new Date());

                if (lastDate == null) {
                    //No days on record - Create today
                    DayRecord day = new DayRecord();
                    day.setComment(getResources().getString(R.string.has_not_been));
                    day.checkAndSetIfGymDay(getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS));
                    datasource.createDayRecord(day);
                } else if (!lastDate.compareToDate(todaysDate.getDate())) {
                    if (lastDate.getDate().before(todaysDate.getDate())) {
                        //We skipped a day somehow
                        while (!lastDate.compareToDate(todaysDate.getDate())) {
                            //Add days until we match up
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(lastDate.getDate());
                            cal.add(Calendar.DATE, 1); //minus number would decrement the days
                            lastDate.setDate(cal.getTime());
                            DayRecord day = new DayRecord();
                            day.setComment(getResources().getString(R.string.no_record));
                            day.setIsGymDay(false);
                            datasource.createDayRecord(day);
                            Log.d(TAG, "Incremented a day, lastDate is now" + lastDate.getDateString()
                                    + "and today's date is " + todaysDate.getDateString());
                        }
                    } else if (lastDate.getDate().after(todaysDate.getDate())) {
                        //A more nefarious case when we have dates ahead of system time
                        //Maybe this happened because of travelling between time zones
                        //Or because the user's phone is messed up. We will go back to original date
                        //Todo: Send a message to user informing of this change
                        //Todo: Save information - If correct day reappears, reapply old day records
                        Log.d(TAG, "Current system date is " + todaysDate.getDateString() + " but last day on record "
                                + " is " + lastDate.getDateString());
                    }
                }
            }
        }catch (Exception e){
            Log.e(TAG, "Unable to use Database on mainactivity start " + e);
        } finally{
            DBRecordsSource.getInstance().closeDatabase();
        }

        super.onStart();
        mGoogleApiClient.connect();

    }

    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();

    }
    /**
     * Adds proximity alert to given coordinates; removes old proximity alerts.
     * Also saves coordinates into sharedpref
     * @param lat
     * @param lng
     */
    public void addProximityAlert(double lat, double lng, int n){
        LocationManager lm=(LocationManager) getSystemService(LOCATION_SERVICE);
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        removeProxAlert(n);
        float range = (float) prefs.getInt("range", -1);
        if (range < 0){
            prefs.edit().putFloat("range", (float) Constants.DEFAULT_RADIUS);
            range = Constants.DEFAULT_RADIUS;
        }
        Intent intent = new Intent(Constants.PROX_INTENT_FILTER);
        int alertId = (int) System.currentTimeMillis();
        PendingIntent pi = PendingIntent.getBroadcast(this, alertId , intent, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("proxalert"+n, alertId).commit();
        Util.putDouble(editor, Constants.DEST_LAT+n, lat);
        Util.putDouble(editor, Constants.DEST_LNG+n, lng);
        Log.d(TAG, "Adding prox alert, ID is " + alertId + " range is " + range);

        editor.commit();
        try{
            lm.addProximityAlert(lat, lng, range, -1, pi);
        } catch (SecurityException e){
            Log.e(TAG, "No permission");
        }
    }

    public void removeAllProximityAlerts() {
        for (int i=1 ; i<Constants.GYM_LIMIT ; i++){
            removeProxAlert(i);
        }
    }

    public void removeProxAlert(int i){
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        int proxid = prefs.getInt("proxalert" + i, 0); //Todo: Remember individual IDs
        Log.d(TAG, "Attempting to remove prox alert, id is " + proxid);
        LocationManager lm=(LocationManager) getSystemService(LOCATION_SERVICE);
        Intent intent = new Intent(Constants.PROX_INTENT_FILTER);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this , proxid, intent, 0);
        try{
            lm.removeProximityAlert(pendingIntent);
            SharedPreferences.Editor edit = prefs.edit();
            edit.remove("lat"+i);
            edit.remove("lng"+i);
            edit.remove("proxalert"+i);
            edit.remove("address"+i);
            edit.remove("name"+i);
            edit.commit();
        } catch (SecurityException e){
            Log.e(TAG, "No permission");
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void populategeofencelist(){
        mGeofenceList = new ArrayList<Geofence>();
        for (int i = 1; i<Constants.GYM_LIMIT; i++){
            addGeofenceFromListposition(i);
        }
    }

    public void addGeofenceFromListposition(int n){
        Log.d(TAG, "addGeoFenceFromListPosition n=" + n);
        if (mGeofenceList==null){
            this.mGeofenceList = new ArrayList<Geofence>();
        }
        Gym gym = getGymLocation(this, n);
        if (gym == null || gym.location==null){
            Log.d(TAG, "addGeoFenceFromListPosition gym retrieved from " + n + " is null");
            return;
        }
        mGeofenceList.add(new Geofence.Builder()
                .setRequestId(Integer.toString(gym.proxid))
                .setCircularRegion(
                        gym.location.getLatitude(),
                        gym.location.getLongitude(),
                        Constants.DEFAULT_RADIUS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());

    }
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getGeofencePendingIntentById(int proxid){
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, proxid, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
    public void addGeofencesButtonHandler() {
        Log.d(TAG, "Adding geoFencesButtonHandler click");
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, "not connected in addgeofence", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
    public void removeGeofencesButtonHandler() {
        Log.d(TAG, "Removing geoFencesButtonHandler click");
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, "google api not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }

    public void removeGeofencesById(int proxid){
        Log.d(TAG, "Removing geoFencesButtonHandler click");
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, "google api not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntentById(proxid)
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
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
        double lat = Util.getDouble(prefs, "lat"+i, Constants.DEFAULT_COORDINATE);
        double lng = Util.getDouble(prefs, "lng"+i, Constants.DEFAULT_COORDINATE);

        Location gymLocation = new Location("");
        gymLocation.setLatitude(lat);
        gymLocation.setLongitude(lng);
        Gym gym = new Gym();
        gym.location = gymLocation;
        gym.address = prefs.getString("address"+i, "No Address");
        gym.name = prefs.getString("name"+i, "");
        gym.proxid = prefs.getInt("proxalert"+i, 0);
        return gym;
    }

    public Fragment getFragmentByKey(String key){
        switch (key){
            case Constants.DAYPICKER_FRAG:
                return mSectionsPagerAdapter.getRegisteredFragment(0);
            case Constants.PLACEPICKER_FRAG:
                return mSectionsPagerAdapter.getRegisteredFragment(3);
            case Constants.DAYOFWEEK_FRAG:
                return mSectionsPagerAdapter.getRegisteredFragment(2);
            case Constants.NEWSFEED_FRAG:
                return mSectionsPagerAdapter.getRegisteredFragment(1);
            default:
                return null;
        }
    }

    public void onListFragmentInteraction(StatsContent.Stat item){
        Log.d(TAG, "onListFragmentInteraction");
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        Log.i(TAG, "Connection suspended");

        // onConnected() will be called again automatically when the service reconnects
    }

    /**
     * Runs when the result of calling addGeofences() and removeGeofences() becomes available.
     * Either method can complete successfully or with an error.
     *
     * Since this activity implements the {@link ResultCallback} interface, we are required to
     * define this method.
     *
     * @param status The Status returned through a PendingIntent when addGeofences() or
     *               removeGeofences() get called.
     */
    public void onResult(Status status) {
        if (status.isSuccess()) {
            // Update state and save in shared preferences.
            mGeofencesAdded = !mGeofencesAdded;
            SharedPreferences.Editor editor =  getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS).edit();
            editor.putBoolean(Constants.GEOFENCES_ADDED_KEY, mGeofencesAdded);
            editor.apply();

            // Update the UI. Adding geofences enables the Remove Geofences button, and removing
            // geofences enables the Add Geofences button.
            Toast.makeText(
                    this,
                    mGeofencesAdded ? "Added geofence" :
                            "Removed geofence",
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = "Some error in onResult";
            Log.e(TAG, errorMessage);
        }
    }

}


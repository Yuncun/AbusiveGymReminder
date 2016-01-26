package com.pipit.agc.agc;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.data.DayRecord;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class AllinOneActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    LocationManager lm;
    GoogleApiClient mGoogleApiClient;
    SectionsPagerAdapter mSectionsPagerAdapter;
    private AlarmManagerBroadcastReceiver _alarm;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private static String TAG = "AllinOneActivity";
    ViewPager mViewPager;
    private List<Fragment> _fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main_layout);

        /*Launch Intro Activity*/
        //Intent intent = new Intent(this, IntroductionActivity.class);
        //startActivity(intent);

        /*Paging for landing screen*/
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        initialisePaging();
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1);

        /*Handle Google stuff*/
        initGoogleApiClient();

        Log.d(TAG, "remaking _alarmmanager " + _alarm);
        _alarm = new AlarmManagerBroadcastReceiver();
        _alarm.setGoogleApiThing(mGoogleApiClient);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, Constants.DAY_RESET_HOUR);
        calendar.set(Calendar.MINUTE, Constants.DAY_RESET_MINUTE);
        calendar.add(Calendar.DATE, 1);
        _alarm.CancelAlarm(getApplicationContext());
        _alarm.SetAlarm(getApplicationContext(), calendar);

        /*Set Proximity Alert*/
        //setProximityLocationManager();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return _fragments.get(position);
            //Todo: Used a civilized data structure
        }

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
                case 3:
                    return "Page four";
                case 4:
                    return "Page Five";
                case 5:
                    return "Page Six";
            }
            return null;
        }
    }

    private void initialisePaging() {
        //Do not willy nilly change order of this list; List position may be used to find fragments
        _fragments = new ArrayList<Fragment>();
        _fragments.add(LandingFragment.newInstance(1));
        _fragments.add(DayPickerFragment.newInstance(2));
        _fragments.add(NewsfeedFragment.newInstance());
        _fragments.add(TestDBFragmentDays.newInstance(4));
        _fragments.add(TestDBFragmentMessages.newInstance(5));
        _fragments.add(PlacePickerFragment.newInstance());
    }


    protected void onStart() {
        mGoogleApiClient.connect();
        /*Make sure that we are up to date*/
        DBRecordsSource datasource = DBRecordsSource.getInstance();
        datasource.openDatabase();
        DayRecord lastDate = datasource.getLastDayRecord();
        DayRecord todaysDate = new DayRecord();
        todaysDate.setDate(new Date());

        if (lastDate==null){
            //No days on record - Create today
            DayRecord dayRecord = datasource.createDayRecord("Did not go to gym ", new Date());
        }else
        if (!lastDate.compareToDate(todaysDate.getDate())){
            if(lastDate.getDate().before(todaysDate.getDate())){
                //We skipped a day somehow
                while(!lastDate.compareToDate(todaysDate.getDate())){
                    //Add days until we match up
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(lastDate.getDate());
                    cal.add(Calendar.DATE, 1); //minus number would decrement the days
                    lastDate.setDate(cal.getTime());
                    datasource.createDayRecord("No Record", lastDate.getDate());
                    Log.d(TAG, "Incremented a day, lastDate is now" + lastDate.getDateString()
                        + "and today's date is " + todaysDate.getDateString());
                }
            }
            else if (lastDate.getDate().after(todaysDate.getDate())){
                //A more nefarious case when we have dates ahead of system time
                //Maybe this happened because of travelling between time zones
                //Or because the user's phone is messed up. We will go back to original date
                //Todo: Send a message to user informing of this change
                //Todo: Save information - If correct day reappears, reapply old day records
                Log.d(TAG, "Current system date is " + todaysDate.getDateString() + " but last day on record "
                        + " is " + lastDate.getDateString());
            }
        }
        DBRecordsSource.getInstance().closeDatabase();

        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /*          LOCATION STUFF          */

    @Override
    public void onConnected(Bundle connectionHint) {
        //startLocationUpdates();
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

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged" + location);
        Log.d("Alarm", "onLocationChanged ");

        mLastLocation = location;
        double currlat = location.getLatitude();
        double currlng = location.getLongitude();

        /*Check lat/lng*/
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        double lat = Util.getDouble(prefs, "lat", currlat);
        double lng = Util.getDouble(prefs, "lng", currlng);

        Location gymLocation = new Location("");
        gymLocation.setLatitude(lat);
        gymLocation.setLongitude(lng);

        float distance = gymLocation.distanceTo(location);

        String mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        ((LandingFragment)_fragments.get(0)).updateLastLocation(
                prefs.getString("locationlist", "none") + "\n" + mLastUpdateTime
        + " Lat:" + currlat + " Lng:" + currlng + " Dist:" + distance);

        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    public void addProximityAlert(double lat, double lng){
        lm=(LocationManager) getSystemService(LOCATION_SERVICE);
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        int maxAlertId = prefs.getInt("maxAlertId", 0); //Todo: Remember individual IDs
        Log.d(TAG, "Adding prox alert, previous id was " + 0);
        removeAllProximityAlerts(this);
        float range = (float) prefs.getInt("range", -1);
        if (range < 0){
            prefs.edit().putFloat("range", (float) 50);
            range = 50;
        }
        Intent intent = new Intent(Constants.PROXIMITY_INTENT_ACTION);
        maxAlertId++;
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("maxAlertId", maxAlertId);
        Util.putDouble(editor, "lat", lat);
        Util.putDouble(editor, "lng", lng);
        editor.commit();

        //Log.d(TAG, "Finished adding prox alert, maxAlertId is now " + prefs.getInt("maxAlertId", 0));
        lm.addProximityAlert(lat, lng, range, -1, pi);
    }

    public void removeAllProximityAlerts(Context context) {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        int maxAlertId = prefs.getInt("maxAlertId", 0); //Todo: Remember individual IDs

        if (lm==null){
            lm=(LocationManager) getSystemService(LOCATION_SERVICE);
        }
        for (int i=0; i<=maxAlertId; i++){
            Intent intent = new Intent(Constants.PROXIMITY_INTENT_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context ,i, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            lm.removeProximityAlert(pendingIntent);
            pendingIntent.cancel();
        }
        //For good measure

    }
}


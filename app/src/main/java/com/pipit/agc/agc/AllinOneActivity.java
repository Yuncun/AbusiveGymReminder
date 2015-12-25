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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class AllinOneActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static final String PROXIMITY_INTENT_ACTION = new String("com.pipit.agc.agc.action.PROXIMITY_ALERT");
    private IntentFilter mIntentFilter;
    LocationManager lm;
    GoogleApiClient mGoogleApiClient;
    SectionsPagerAdapter mSectionsPagerAdapter;
    private AlarmManagerBroadcastReceiver _alarm;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private static String TAG = "AllinOneActivity";
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    private List<Fragment> _fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main_layout);

        /*Launch Intro Activity*/
        Intent intent = new Intent(this, IntroductionActivity.class);
        startActivity(intent);

        /*Paging for landing screen*/
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        initialisePaging();
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1);

        /*Handle Google stuff*/
        initGoogleApiClient();

        /*Initialize Alarm*//*
        _alarm = new AlarmManagerBroadcastReceiver();
        _alarm.setGoogleApiThing(mGoogleApiClient);
        _alarm.setMainActivity(this);
        _alarm.SetAlarm(getApplicationContext());
*/
        /*Set Proximity Alert*/
        mIntentFilter = new IntentFilter(PROXIMITY_INTENT_ACTION);
        setProximityLocationManager();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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
            //return PlaceholderFragment.newInstance(position+1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
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
            }
            return null;
        }
    }

    private void initialisePaging() {
        _fragments = new ArrayList<Fragment>();
        _fragments.add(LandingFragment.newInstance(1));
        _fragments.add(PlaceholderFragment.newInstance(2));
        _fragments.add(PlacePickerFragment.newInstance());
    }


    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
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

    public LandingFragment getLandingFragment(){
        return (LandingFragment) _fragments.get(0);
    }

    public void setProximityLocationManager(){

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        double lat = Util.getDouble(prefs, "lat", 0);
        double lng = Util.getDouble(prefs, "lng", 0);

        lm=(LocationManager) getSystemService(LOCATION_SERVICE);
        //Intent i= new Intent(this, ProximityReceiver.class);
        //PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), -1, i, 0);

        Intent intent = new Intent(PROXIMITY_INTENT_ACTION);
        intent.putExtra(ProximityReceiver.EVENT_ID_INTENT_EXTRA, 0);
        PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), -1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        lm.addProximityAlert(lat, lng, 100f, 10000L, pi);
    }

}

package com.pipit.agc.agc;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.data.DayRecord;

import java.util.Calendar;
import java.util.Date;

public class AllinOneActivity extends AppCompatActivity implements StatisticsFragment.OnListFragmentInteractionListener{
    private static String TAG = "AllinOneActivity";

    SectionsPagerAdapter mSectionsPagerAdapter;
    private AlarmManagerBroadcastReceiver _alarm;
    ViewPager mViewPager;

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
        _alarm.CancelAlarm(getApplicationContext(), "locationlogging", 0);
        _alarm.setAlarmForDayLog(getApplicationContext(), calendar);
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
    }

    protected void onStop() {
        super.onStop();
    }
    /**
     * Adds proximity alert to given coordinates; removes old proximity alerts.
     * Also saves coordinates into sharedpref
     * @param lat
     * @param lng
     */
    public void addProximityAlert(double lat, double lng){
        LocationManager lm=(LocationManager) getSystemService(LOCATION_SERVICE);
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        removeAllProximityAlerts(this);
        float range = (float) prefs.getInt("range", -1);
        if (range < 0){
            prefs.edit().putFloat("range", (float) 100);
            range = 100;
        }
        Intent intent = new Intent(Constants.PROX_INTENT_FILTER);
        int alertId= (int) System.currentTimeMillis();
        PendingIntent pi = PendingIntent.getBroadcast(this, alertId , intent, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("maxAlertId", alertId).commit();
        Util.putDouble(editor, Constants.DEST_LAT, lat);
        Util.putDouble(editor, Constants.DEST_LNG, lng);
        Log.d(TAG, "Adding prox alert, ID is " + alertId + " range is " + range);

        editor.commit();
        try{
            lm.addProximityAlert(lat, lng, range, -1, pi);
        } catch (SecurityException e){
            Log.e(TAG, "No permission");
        }
    }

    public void removeAllProximityAlerts(Context context) {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        int maxAlertId = prefs.getInt("maxAlertId", 0); //Todo: Remember individual IDs
        Log.d(TAG, "Attempting to remove prox alert, id is " + maxAlertId);
        LocationManager lm=(LocationManager) getSystemService(LOCATION_SERVICE);

        Intent intent = new Intent(Constants.PROX_INTENT_FILTER);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this , maxAlertId, intent, 0);
        try{
            lm.removeProximityAlert(pendingIntent);
        } catch (SecurityException e){
            Log.e(TAG, "No permission");
        }
    }

    /**
     * Utility function for getting the location of the gym
     * @return Location object, with the gym coordinates
     */
    public static Location getGymLocation(Context context){
        /*Check lat/lng*/
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        double lat = Util.getDouble(prefs, "lat", Constants.DEFAULT_COORDINATE);
        double lng = Util.getDouble(prefs, "lng", Constants.DEFAULT_COORDINATE);

        Location gymLocation = new Location("");
        gymLocation.setLatitude(lat);
        gymLocation.setLongitude(lng);
        return gymLocation;
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


}


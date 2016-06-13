package com.pipit.agc.agc.activity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.pipit.agc.agc.controller.GeofenceController;
import com.pipit.agc.agc.model.Message;
import com.pipit.agc.agc.receiver.AlarmManagerBroadcastReceiver;
import com.pipit.agc.agc.receiver.GeoFenceTransitionsIntentReceiver;
import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.fragment.DayOfWeekPickerFragment;
import com.pipit.agc.agc.util.GeofenceUtils;
import com.pipit.agc.agc.model.Gym;
import com.pipit.agc.agc.fragment.LocationListFragment;
import com.pipit.agc.agc.fragment.NewsfeedFragment;
import com.pipit.agc.agc.R;
import com.pipit.agc.agc.fragment.StatisticsFragment;
import com.pipit.agc.agc.util.ReminderOracle;
import com.pipit.agc.agc.util.SharedPrefUtil;
import com.pipit.agc.agc.util.StatsContent;
import com.pipit.agc.agc.util.Util;
import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.model.DayRecord;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AllinOneActivity extends AppCompatActivity {
    private static String TAG = "AllinOneActivity";

    SectionsPagerAdapter mSectionsPagerAdapter;
    private AlarmManagerBroadcastReceiver _alarm;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main_layout);

        /*If opened from a notification, redirect to correct page*/
        if (getIntent().hasExtra(Constants.MESSAGE_ID)){
            long id = getIntent().getLongExtra(Constants.MESSAGE_ID, -1);
            Log.d(TAG, "Received intent with message id " + id);
            if (id>0){
                DBRecordsSource datasource = DBRecordsSource.getInstance();
                datasource.openDatabase();
                List<Message> msgs = datasource.getAllMessages();
                long index = msgs.get(msgs.size()-1).getId(); //This is a bit hacky, but we need to direct user to last msg
                datasource.closeDatabase();
                Intent intent = new Intent(this, MessageBodyActivity.class);
                intent.putExtra(Constants.MESSAGE_ID, index);
                startActivityForResult(intent, 0);
            }
        }

        /*Launch Intro Activity*/
        if (SharedPrefUtil.getIsFirstTime(this)){
            Intent intent = new Intent(this, IntroductionActivity.class);
            //intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // Adds the FLAG_ACTIVITY_NO_HISTORY flag
            startActivity(intent);
            SharedPrefUtil.setFirstTime(this, false);
            Message f = new Message();
            f.setReason(Message.WELCOME);
            //f.setHeader("Welcome!");
            f.setHeader("In the future you will abusive messages here when you miss gym days");
            f.setBody("");
            ReminderOracle.leaveMessage(f);
        }

        /*Paging for landing screen*/
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.addTab(mTabLayout.newTab().setText("Stats"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Inbox"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Days"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Gyms"));
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.setupWithViewPager(mViewPager);


        Log.d(TAG, "remaking _alarmmanager " + _alarm);
        _alarm = new AlarmManagerBroadcastReceiver();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, Constants.DAY_RESET_HOUR);
        calendar.set(Calendar.MINUTE, Constants.DAY_RESET_MINUTE);
        calendar.add(Calendar.DATE, 1);
        _alarm.setAlarmForDayLog(getApplicationContext(), calendar);
        GeofenceController.getInstance().init(this);
        checkPermissions();
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
        else if (id == R.id.action_forget){
            /* This action forgets the key sharedpreferences except for gyms */
            SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
            //prefs.edit().putBoolean("showGymStatus", true).commit();
            SharedPrefUtil.setFirstTime(this, true);
            Util.putListToSharedPref(this, Constants.TAKEN_MESSAGE_IDS, new ArrayList<String>());
        }
        else if (id == R.id.action_remove_messages){
            DBRecordsSource datasource = DBRecordsSource.getInstance();
            datasource.openDatabase();
            datasource.deleteAllMessages();
            datasource.closeDatabase();
        }
        else if (id == R.id.action_remove_geofences){
            //GeofenceController.getInstance().removeAllGeofences(null);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
        String[] title = {"Stats", "Inbox", "Days", "Gyms"};

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
                    return LocationListFragment.newInstance(1);
                default:
                    return NewsfeedFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 4;
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

        @Override
        public CharSequence getPageTitle(int position) {
            // return your title there
            return title[position];
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
                            day.setIsGymDay(SharedPrefUtil.getGymStatusFromDayOfWeek(this, cal.get(Calendar.DAY_OF_WEEK)));
                            datasource.createDayRecord(day);
                            SharedPrefUtil.updateMainLog(this, "Updated day from onStart");

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

    public Fragment getFragmentByKey(String key){
        switch (key){
            case Constants.STATS_FRAGMENT:
                return mSectionsPagerAdapter.getRegisteredFragment(0);
            case Constants.LOCATION_FRAG:
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

    private void checkPermissions(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        Constants.GRANTED_LOCATION_PERMISSIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.GRANTED_LOCATION_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0) {
            Fragment f = mSectionsPagerAdapter.getRegisteredFragment(0);
            if (f instanceof StatisticsFragment){
                ((StatisticsFragment) f).update();
            }
        }
    }
}


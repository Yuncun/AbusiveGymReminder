package com.pipit.agc.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.MaterialDialog;
import com.pipit.agc.controller.GeofenceController;
import com.pipit.agc.data.MsgAndDayRecords;
import com.pipit.agc.fragment.DayPickerFragmentTwo;
import com.pipit.agc.model.Message;
import com.pipit.agc.receiver.AlarmManagerBroadcastReceiver;
import com.pipit.agc.util.Constants;
import com.pipit.agc.fragment.LocationListFragment;
import com.pipit.agc.fragment.NewsfeedFragment;
import com.pipit.agc.R;
import com.pipit.agc.fragment.StatisticsFragment;
import com.pipit.agc.util.ReminderOracle;
import com.pipit.agc.util.SharedPrefUtil;
import com.pipit.agc.util.StatsContent;
import com.pipit.agc.model.DayRecord;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Main Activity
 *
 * Holds the main view pager, status bar, etc.
 */
public class AllinOneActivity extends AppCompatActivity {
    private static String TAG = "AllinOneActivity";

    SectionsPagerAdapter mSectionsPagerAdapter;
    private AlarmManagerBroadcastReceiver _alarm;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private MaterialCab cab;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main_layout);

        /*If opened from a notification, redirect to correct page*/
        if (getIntent().hasExtra(Constants.MESSAGE_ID)){
            long id = getIntent().getLongExtra(Constants.MESSAGE_ID, -1);
            Log.d(TAG, "Received intent with message id " + id);
            if (id>0){
                MsgAndDayRecords datasource = MsgAndDayRecords.getInstance();
                datasource.openDatabase();
                List<Message> msgs = datasource.getAllMessages();
                long index = msgs.get(msgs.size()-1).getId(); //This is a bit hacky, but we need to direct user to last msg
                datasource.closeDatabase();
                Intent intent = new Intent(this, MessageBodyActivity.class);
                intent.putExtra(Constants.MESSAGE_ID, index);
                startActivityForResult(intent, 0);
            }
        }

        /*Launch Intro Activity if required*/
        if (SharedPrefUtil.getIsFirstTime(this)){
            Intent intent = new Intent(this, IntroductionActivity.class);
            //intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // Adds the FLAG_ACTIVITY_NO_HISTORY flag
            startActivity(intent);
            SharedPrefUtil.setFirstTime(this, false);
            Message f = new Message();
            f.setReason(Message.WELCOME);
            f.setHeader("In the future you will abusive messages here when you miss gym days");
            f.setBody("");
            ReminderOracle.leaveMessage(f);
            new MaterialDialog.Builder(this)
                    .title(R.string.firsttime_title)
                    .content(R.string.firsttime_body)
                    .positiveText(R.string.gotit)
                    .show();

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

        /*Tab layout stuff*/
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.addTab(mTabLayout.newTab().setText("Stats"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Inbox"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Days"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Gyms"));
        mTabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.basewhite));
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.setupWithViewPager(mViewPager);

        //Deal with floating action button used in locationlist fragment. This must be done here because it syncs up
        //with the collapsing toolbar. We just choose to hide it when the wrong fragment is shown.
        mFab = (FloatingActionButton) findViewById(R.id.locationsFab);
        mFab.setBackgroundColor(ContextCompat.getColor(this, R.color.schemethree_darkerteal));
        mFab.hide();
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 3) {
                    mFab.show();
                } else {
                    mFab.hide();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        cab = new MaterialCab(this, R.id.cab_stub);

        //Log.d(TAG, "remaking _alarmmanager " + _alarm);
        /*
        _alarm = new AlarmManagerBroadcastReceiver();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, Constants.DAY_RESET_HOUR);
        calendar.set(Calendar.MINUTE, Constants.DAY_RESET_MINUTE);
        calendar.add(Calendar.DATE, 1);
        _alarm.setAlarmForDayLog(getApplicationContext(), calendar);
        GeofenceController.getInstance().init(this);*/
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
            final Intent i = new Intent(this, IndividualSettingActivity.class);
            i.putExtra("fragment", "PreferencesFragment");
            startActivityForResult(i, 0);
            return true;
        }
        else if (id == R.id.action_dev){
            Intent i = new Intent(this, SettingsActivity.class);
            startActivityForResult(i, 1);
        }
        else if (id == R.id.action_forget){
            /* This action forgets the key sharedpreferences except for gyms */
            SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
            SharedPrefUtil.setFirstTime(this, true);
            SharedPrefUtil.putListToSharedPref(this, Constants.TAKEN_MESSAGE_IDS, new ArrayList<String>());
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
                    return DayPickerFragmentTwo.newInstance(2);
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
        /*Do stats */
        StatsContent stats = StatsContent.getInstance();
        stats.refreshDayRecords();
        stats.refreshMessageRecords();
        stats.calculateStats();
        updateDate(this);
        super.onStart();
    }

    /**
     * Used to update AbusiveGymReminder date. (Very important!)
     *
     * AbusiveGymReminder will periodically update it's date at midnight. However,
     * there are various cases where this is not fully reliable. This function exists
     * to make sure our latest dayrecords are up-to-date.
     *
     * This should be used before every user session.
     *
     * * @param context
     */
    public static void updateDate(Context context){
        /*Make sure that we are up to date*/
        try {
            MsgAndDayRecords datasource = MsgAndDayRecords.getInstance();
            datasource.openDatabase();
            synchronized (datasource) {
                DayRecord lastDate = datasource.getLastDayRecord();

                DayRecord todaysDate = new DayRecord();
                todaysDate.setDate(new Date());

                if (lastDate == null) {
                    //No days on record - Create today
                    DayRecord day = new DayRecord();
                    day.setComment(context.getResources().getString(R.string.has_not_been));
                    day.checkAndSetIfGymDay(context.getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS));
                    datasource.createDayRecord(day);
                } else if (!lastDate.equalsDate(todaysDate.getDate())) {
                    if (lastDate.getDate().before(todaysDate.getDate())) {
                        //We skipped a day somehow
                        while (!lastDate.equalsDate(todaysDate.getDate())) {
                            //If there are any visits on that day, we need to end them
                            boolean  flagStartNewVisit = false;
                            if (lastDate.isCurrentlyVisiting()){
                                datasource.closeDatabase();
                                lastDate.endCurrentVisit();
                                datasource.openDatabase();
                                flagStartNewVisit = true;
                            }

                            //Add days until we match up
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(lastDate.getDate());
                            cal.add(Calendar.DATE, 1); //minus number would decrement the days
                            lastDate.setDate(cal.getTime());
                            DayRecord day = new DayRecord();
                            day.setComment(context.getResources().getString(R.string.no_record));
                            day.setIsGymDay(SharedPrefUtil.getGymStatusFromDayOfWeek(context, cal.get(Calendar.DAY_OF_WEEK)));
                            datasource.createDayRecord(day);
                            if (flagStartNewVisit){
                                day.startCurrentVisit();
                            }
                            SharedPrefUtil.updateMainLog(context, "Updated day from onStart");

                        }
                    } else if (lastDate.getDate().after(todaysDate.getDate())) {
                        //A more nefarious case when we have dates ahead of system time
                        //Maybe this happened because of travelling between time zones
                        //Or because the user's phone is messed up. We will go back to original date
                        //Todo: Send a message to user informing of this change
                        //Todo: Save information - If correct day reappears, reapply old day records
                        Log.d(TAG, "Current system date is " + todaysDate.getDateString() + " but last day on record "
                                + " is " + lastDate.getDateString());
                        SharedPrefUtil.updateMainLog(context, "While doing a routine date update, noticed user has gone back in time (Did not update)");
                    }
                }
            }
        }catch (Exception e){
            Log.e(TAG, "Unable to use Database on mainactivity start " + e);
        } finally{
            MsgAndDayRecords.getInstance().closeDatabase();
        }
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

    public MaterialCab getCab(){
        return cab;
    }

    public FloatingActionButton getFab(){
        return mFab;
    }
}



package com.pipit.agc.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.MaterialDialog;
import com.pipit.agc.controller.GeofenceController;
import com.pipit.agc.data.MsgAndDayRecords;
import com.pipit.agc.fragment.GymDayPickerFragment;
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
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private MaterialCab cab; //Contextual action bar - Used in removing messages in newsfeed
    private FloatingActionButton mFab; //Floating action button - Used in place picker frag to add gym
    private AlarmManagerBroadcastReceiver _alarm = new AlarmManagerBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main_layout);

        //If opened from a notification, redirect to correct page
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

        //Launch Intro Activity if required
        if (SharedPrefUtil.getIsFirstTime(this)){
            Intent intent = new Intent(this, IntroductionActivity.class);
            //intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // Adds the FLAG_ACTIVITY_NO_HISTORY flag
            startActivity(intent);
            SharedPrefUtil.setFirstTime(this, false);
            Message f = new Message();
            f.setReason(Message.WELCOME);
            f.setHeader(getString(R.string.firsttime_message));
            f.setBody("");
            ReminderOracle.leaveMessage(f);
            final MaterialDialog d = new MaterialDialog.Builder(this)
                    .title(R.string.firsttime_title)
                    .customView(R.layout.welcome_dialog_layout, true)
                    .positiveText(R.string.close)
                    .dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            /*NotificationUtil.showNotification(context, getResources().getString(R.string.firsttime_message),
                                    "", //Second line
                                    "", //Reason of message
                                    "", //Attribution
                                    Message.WELCOME);*/
                            //TODO: Make a design decision whether to show the welcome notif. It may be redundant with the "sample" notif
                        }
                    })
                    .show();
            View dv = d.getCustomView();
            Button sampleabuse = (Button) dv.findViewById(R.id.welcome_try_abuse_button);
            sampleabuse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReminderOracle.doLeaveMessageBasedOnPerformance(v.getContext(), true);
                    d.dismiss();
                }
            });
        }

        /*Paging for landing screen*/
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(Constants.NEWFEED_FRAG_POS);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        /*Tab layout stuff*/
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.stats)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.inbox)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.days)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.gyms)));
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
                if (position == Constants.LOCATION_FRAG_POS) {
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
        GeofenceController.getInstance().init(this);
        checkPermissions();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, Constants.DAY_RESET_HOUR);
        calendar.set(Calendar.MINUTE, Constants.DAY_RESET_MINUTE);
        calendar.add(Calendar.DATE, Constants.DAYS_TO_ADD);
        AlarmManagerBroadcastReceiver.setAlarmForDayLog(getApplicationContext(), calendar);
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
        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
        String[] title = {getString(R.string.stats),
                getString(R.string.inbox),
                getString(R.string.days),
                getString(R.string.gyms)};

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case Constants.STATS_FRAG_POS:
                    return StatisticsFragment.newInstance(0);
                case Constants.NEWFEED_FRAG_POS:
                    return NewsfeedFragment.newInstance();
                case Constants.DAYPICKER_FRAG_POS:
                    return GymDayPickerFragment.newInstance(2);
                case Constants.LOCATION_FRAG_POS:
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
        //Update stats
        StatsContent stats = StatsContent.getInstance();
        stats.refreshDayRecords();
        stats.refreshMessageRecords();
        stats.calculateStats();
        updateDate(this);
        super.onStart();
    }

    /**
     * Used to update AbusiveGymReminder date if necessary
     *
     * AbusiveGymReminder will periodically update it's date at midnight. However,
     * there are various cases where this is not fully reliable. This function exists
     * to make sure our latest dayrecords are up-to-date.
     *
     * This should be used before every user session.
     *
     * * @param context
     */
    public static synchronized void updateDate(Context context){
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
                        Log.d(TAG, "LastDate == " + lastDate.getDateString() + " todaysDate == " + todaysDate.getDateString());
                        SharedPrefUtil.updateMainLog(context, "LastDate == " + lastDate.getDateString() + " todaysDate == " + todaysDate.getDateString());
                        //We skipped a day somehow
                        while (!lastDate.equalsDate(todaysDate.getDate())) {
                            //If there are any visits on that day, we need to end them
                            boolean flagStartNewVisit = false;
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
                            day.setHasBeenToGym(false); //If the user went to the gym, it would've been updated
                            day.setDate(cal.getTime());
                            day.setIsGymDay(SharedPrefUtil.getGymStatusFromDayOfWeek(context, cal.get(Calendar.DAY_OF_WEEK)));
                            datasource.createDayRecord(day);
                            if (flagStartNewVisit){
                                day.startCurrentVisit();
                            }
                            SharedPrefUtil.updateMainLog(context, "Updated day from onStart " + day.getDateString() + " wasGymDay==" + day.isGymDay());

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
            Fragment f = mSectionsPagerAdapter.getRegisteredFragment(Constants.STATS_FRAG_POS);
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
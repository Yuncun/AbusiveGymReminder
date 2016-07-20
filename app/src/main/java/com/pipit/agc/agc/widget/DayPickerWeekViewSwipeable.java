package com.pipit.agc.agc.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.pipit.agc.agc.R;
import com.pipit.agc.agc.adapter.WeekViewAdapter;
import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.util.SharedPrefUtil;
import com.pipit.agc.agc.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eric on 7/9/2016.
 */
public class DayPickerWeekViewSwipeable extends WeekViewSwipeable {
    private String TAG = "DayPickerWV";

    public DayPickerWeekViewSwipeable(Context context, AttributeSet attrs) {
        super(context, attrs);

        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        List<String> plannedDOWstrs = SharedPrefUtil.getListFromSharedPref(prefs, Constants.SHAR_PREF_PLANNED_DAYS);
        List<Integer> plannedDOW = Util.listOfStringsToListOfInts(plannedDOWstrs);
        //TODO:
        //I know this is a roundabout way of doing it, but it's some old code that I will change later
        List<Boolean> k = new ArrayList<>();
        for (int i = 0 ; i < 7 ; i++){
            if (plannedDOW.contains(i)) {
                k.add(true);
            }else{
                k.add(false);
            }
        }

        setAdapter(new MyWeekViewAdapter(context, k, this));
        setNavEnabled(false);
    }

    class MyWeekViewAdapter extends WeekViewAdapter<Boolean> {
        SharedPreferences prefs;
        public MyWeekViewAdapter(Context context, List<Boolean> allDayRecords, WeekViewSwipeable layout) {
            super(context, allDayRecords, layout);
            prefs = context.getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        }

        @Override
        protected void styleFromDayrecordsData(final Context context, final List<Boolean> _allDayRecords, int page, View root){
            Resources r = root.getContext().getResources();
            String name = context.getPackageName();
            for (int i = 0 ; i < 7 ; i++) {
                int j = i+1; //I'm as confused as you are
                int viewid = r.getIdentifier("day_" + j, "id", name);
                View weekitem = root.findViewById(viewid);
                CircleView cv = (CircleView) weekitem.findViewById(R.id.calendar_day_info);
                TextView rfd = (TextView) weekitem.findViewById(R.id.record_for_day);

                drawCircleDays(context, cv, rfd, weekitem, i);
            }
        }

        protected void drawCircleDays(final Context context, final CircleView cv, TextView rfd, View weekitem, final int index) {
            cv.setShowSubtitle(false);
            final String gymDay = context.getResources().getString(R.string.gym_day);
            final String restDay = context.getResources().getString(R.string.rest_day);
            View cdn = weekitem.findViewById(R.id.calendar_day_name);
            cdn.setVisibility(View.GONE);
            rfd.setVisibility(View.GONE);
            cv.setTitleText(getDayOfWeekText(index+1));
            weekitem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Keep our sharedprefs synced with adapter data
                    List<String> dates = SharedPrefUtil.getListFromSharedPref(prefs, Constants.SHAR_PREF_PLANNED_DAYS);

                    if (_allDayRecords.get(index)) {
                        //The clicked date was previously a Gym Day, and we need to toggle it off
                        _allDayRecords.set(index, false);
                        dates.remove(Integer.toString(index)); //This is used to keep our sharedprefs records straight
                        cv.setTitleText(restDay);
                        cv.setBackgroundColor(ContextCompat.getColor(context, R.color.basewhite));
                        /*
                        if (index == Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1) {
                            if (mFrag instanceof DayOfWeekPickerFragment){
                                ((DayOfWeekPickerFragment) mFrag).toggleCurrentGymDayData(false);
                            }
                        }*/
                    } else {
                        _allDayRecords.set(index, false);
                        dates.add(Integer.toString(index));
                        cv.setTitleText(gymDay);
                        cv.setBackgroundColor(ContextCompat.getColor(context, R.color.schemethree_darkerteal));
                        /*
                        if (position == Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1) {
                            if (mFrag instanceof DayOfWeekPickerFragment) {
                                ((DayOfWeekPickerFragment) mFrag).toggleCurrentGymDayData(true);
                            }
                        }*/

                    }
                    SharedPrefUtil.putListToSharedPref(prefs.edit(), Constants.SHAR_PREF_PLANNED_DAYS, dates);
                }
            });


            if (index == _allDayRecords.size() - 1) {
                cv.setStrokeColor(ContextCompat.getColor(context, R.color.schemefour_yellow));
                rfd.setTextColor(ContextCompat.getColor(context, R.color.schemefour_yellow));
                rfd.setText("Today");
            }
        }

        @Override
        public int getStartPosition(){
            return 0;
        }

        @Override
        public int getCount(){
            return 1;
        }
    }
}

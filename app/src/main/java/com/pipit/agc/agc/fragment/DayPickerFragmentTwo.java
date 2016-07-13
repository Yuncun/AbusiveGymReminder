package com.pipit.agc.agc.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pipit.agc.agc.adapter.WeekViewAdapter;
import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.R;
import com.pipit.agc.agc.util.Util;
import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.widget.CircleView;
import com.pipit.agc.agc.widget.WeekViewSwipeable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Eric on 2/3/2016.
 */
public class DayPickerFragmentTwo extends android.support.v4.app.Fragment{
    private static final String TAG = "DayPickerFragmentTwo";
    private final static String ARG_SECTION_NUMBER = "section_number";

    private SharedPreferences prefs;

    private List<Boolean> _plannedDays;
    private LinearLayout wv;

    //This is used to update the "GYM DAY" cardview in the Newsfeed
    public interface UpdateGymDayToday{
        void todayIsGymDay(boolean isGymDay);
    }

    public static DayPickerFragmentTwo newInstance(int sectionNumber) {
        DayPickerFragmentTwo fragment = new DayPickerFragmentTwo();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public DayPickerFragmentTwo() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.day_picker_fragment_two, container, false);
        TextView tv = (TextView) rootView.findViewById(R.id.pickdaysmsg);
        tv.setText(getText(R.string.pickdaysmsg));
        wv = (LinearLayout) rootView.findViewById(R.id.sevendays);
        wv.setPadding(8,0,8,0);
        prefs = getContext().getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        List<String> plannedDOWstrs = Util.getListFromSharedPref(prefs, Constants.SHAR_PREF_PLANNED_DAYS);
        List<Integer> plannedDOW = Util.listOfStringsToListOfInts(plannedDOWstrs);
        //TODO:
        //I know this is a roundabout way of doing it, but it's some old code that I will change later
        _plannedDays = new ArrayList<>();
        for (int i = 0 ; i < 7 ; i++){
            if (plannedDOW.contains(i)) {
                _plannedDays.add(true);
            }else{
                _plannedDays.add(false);
            }
        }

        ViewTreeObserver observer = wv.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // TODO Auto-generated method stub
                styleFromDayrecordsData(wv);
                wv.getViewTreeObserver().removeOnGlobalLayoutListener(
                        this);
            }
        });
        return rootView;
    }

    protected void styleFromDayrecordsData( View root) {
        Resources r = root.getContext().getResources();
        String name = getContext().getPackageName();

        //Calculate the width
        int cvparam = (root.getWidth() - root.getPaddingLeft() - root.getPaddingRight())/7
                -(WeekViewSwipeable.circleMarginDefaultLeft + WeekViewSwipeable.circleMarginDefaultRight)
                - 16;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(cvparam, cvparam);
        layoutParams.setMargins(WeekViewSwipeable.circleMarginDefaultLeft,
                WeekViewSwipeable.circleMarginDefaultTop,
                WeekViewSwipeable.circleMarginDefaultRight,
                WeekViewSwipeable.circleMarginDefaultBottom);

        for (int i = 0 ; i < 7 ; i++) {
            int j = i+1; //I'm as confused as you are
            int viewid = r.getIdentifier("day_" + j, "id", name);
            View weekitem = root.findViewById(viewid);
            CircleView cv = (CircleView) weekitem.findViewById(R.id.calendar_day_info);
            cv.setLayoutParams(layoutParams);
            TextView rfd = (TextView) weekitem.findViewById(R.id.record_for_day);
            drawCircleDays(getContext(), cv, rfd, weekitem, i);
        }
    }

    protected void drawCircleDays(final Context context, final CircleView cv, TextView rfd, final View weekitem, final int index) {
        cv.setShowSubtitle(false);
        View cdn = weekitem.findViewById(R.id.calendar_day_name);
        cdn.setVisibility(View.GONE);
        rfd.setVisibility(View.GONE);
        cv.setTitleText(WeekViewAdapter.getDayOfWeekText(index + 1));
        cv.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
        cv.setFillColor(Color.GRAY);

        if (_plannedDays.get(index)){
            cv.setStrokeColor(ContextCompat.getColor(context, R.color.schemethree_red));

        }else{
            cv.setStrokeColor(ContextCompat.getColor(context, R.color.transparent));
            cv.setTitleColor(Color.WHITE);
        }
        weekitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Keep our sharedprefs synced with adapter data TODO: Some kind of binding library would be nice here
                HashSet<String> dates = new HashSet(Util.getListFromSharedPref(prefs, Constants.SHAR_PREF_PLANNED_DAYS));

                if (_plannedDays.get(index)) {
                    //The clicked date was previously a Gym Day, and we need to toggle it off
                    _plannedDays.set(index, false);
                    dates.remove(Integer.toString(index)); //This is used to keep our sharedprefs records straight
                    //cv.setTitleText(restDay);
                    cv.setStrokeColor(ContextCompat.getColor(context, R.color.transparent));

                        /*
                        if (index == Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1) {
                            if (mFrag instanceof DayOfWeekPickerFragment){
                                ((DayOfWeekPickerFragment) mFrag).toggleCurrentGymDayData(false);
                            }
                        }*/
                } else {
                    _plannedDays.set(index, true);
                    dates.add(Integer.toString(index));
                    //cv.setTitleText(gymDay);
                    cv.setStrokeColor(ContextCompat.getColor(context, R.color.schemethree_red));
                        /*
                        if (position == Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1) {
                            if (mFrag instanceof DayOfWeekPickerFragment) {
                                ((DayOfWeekPickerFragment) mFrag).toggleCurrentGymDayData(true);
                            }
                        }*/
                }

                Util.putListToSharedPref(prefs.edit(), Constants.SHAR_PREF_PLANNED_DAYS, new ArrayList(dates));
                weekitem.invalidate();
            }
        });

        /*
        if (index == _allDayRecords.size() - 1) {
            cv.setStrokeColor(ContextCompat.getColor(context, R.color.schemefour_yellow));
            rfd.setTextColor(ContextCompat.getColor(context, R.color.schemefour_yellow));
            rfd.setText("Today");
        }*/
    }


    /* ******************************************************* */
    public static List<Integer> datesToDaysOfWeek(List<Date> dates){
        List<Integer> dow = new ArrayList<Integer>();
        for (Date d : dates){
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            dow.add(new Integer(dayOfWeek));
        }
        return dow;
    }

    private Calendar getCalFromListPosition(int pos){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, pos);
        return cal;
    }

    //This was originally written to show some sort of notice in newsfeed if no days are selected
    //TODO: Currently commented out
    //TODO: Decide if necessary from a design standpoint
    private void executeUpdateCallback(boolean isGymDay) {
        /*
        Log.d("Eric", "Execute callback");
        Fragment registeredFrag = ((AllinOneActivity) getActivity()).getFragmentByKey(Constants.NEWSFEED_FRAG);
        if (registeredFrag!=null){
            UpdateGymDayToday update = (UpdateGymDayToday) registeredFrag;
            update.todayIsGymDay(isGymDay);
        }
        */
    }

    public void toggleCurrentGymDayData(boolean gymDay){
        DBRecordsSource datasource = DBRecordsSource.getInstance();

        datasource.openDatabase();
        datasource.updateLatestDayRecordIsGymDay(gymDay);
        DBRecordsSource.getInstance().closeDatabase();
        executeUpdateCallback(false); //Update the newsfeed fragment
    }

}
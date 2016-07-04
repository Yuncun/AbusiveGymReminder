package com.pipit.agc.agc.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pipit.agc.agc.adapter.DayOfWeekAdapterTwo;
import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.adapter.DayOfWeekAdapter;
import com.pipit.agc.agc.R;
import com.pipit.agc.agc.util.Util;
import com.pipit.agc.agc.activity.AllinOneActivity;
import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.model.DayRecord;

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
    DayOfWeekAdapterTwo _adapter;
    private final static String ARG_SECTION_NUMBER = "section_number";
    private DBRecordsSource datasource;
    private List<DayRecord> _allPreviousDays;


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
        synchronized (this){
            datasource = DBRecordsSource.getInstance();
            datasource.openDatabase();
            _allPreviousDays = datasource.getAllDayRecords();
            DBRecordsSource.getInstance().closeDatabase();
        }

        SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        List<String> plannedDOWstrs = Util.getListFromSharedPref(prefs, Constants.SHAR_PREF_PLANNED_DAYS);
        List<Integer> plannedDOW = Util.listOfStringsToListOfInts(plannedDOWstrs);

        return rootView;
    }


    @Override
    public void onPause() {
        super.onPause();
    }

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
        if (datasource==null){
            datasource = DBRecordsSource.getInstance();
        }
        datasource.openDatabase();
        datasource.updateLatestDayRecordIsGymDay(gymDay);
        DBRecordsSource.getInstance().closeDatabase();
        executeUpdateCallback(false); //Update the newsfeed fragment
    }
}
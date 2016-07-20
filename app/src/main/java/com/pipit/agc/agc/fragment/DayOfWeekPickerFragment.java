package com.pipit.agc.agc.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pipit.agc.agc.data.MsgAndDayRecords;
import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.adapter.DayOfWeekAdapter;
import com.pipit.agc.agc.R;
import com.pipit.agc.agc.util.SharedPrefUtil;
import com.pipit.agc.agc.util.Util;
import com.pipit.agc.agc.model.DayRecord;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Eric on 2/3/2016.
 */
public class DayOfWeekPickerFragment extends android.support.v4.app.Fragment{
    private static final String TAG = "DayPickerFragment";
    DayOfWeekAdapter _adapter;
    private final static String ARG_SECTION_NUMBER = "section_number";
    private MsgAndDayRecords datasource;
    private List<DayRecord> _allPreviousDays;

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;

    //This is used to update the "GYM DAY" cardview in the Newsfeed
    public interface UpdateGymDayToday{
        void todayIsGymDay(boolean isGymDay);
    }

    public static DayOfWeekPickerFragment newInstance(int sectionNumber) {
        DayOfWeekPickerFragment fragment = new DayOfWeekPickerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public DayOfWeekPickerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.day_picker_fragment, container, false);
        synchronized (this){
            datasource = MsgAndDayRecords.getInstance();
            datasource.openDatabase();
            _allPreviousDays = datasource.getAllDayRecords();
            MsgAndDayRecords.getInstance().closeDatabase();
        }

        SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        List<String> plannedDOWstrs = SharedPrefUtil.getListFromSharedPref(prefs, Constants.SHAR_PREF_PLANNED_DAYS);
        List<Integer> plannedDOW = Util.listOfStringsToListOfInts(plannedDOWstrs);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        int totalheight = Math.round(Util.getScreenHeightMinusStatusBar(getContext()));
        mAdapter = new DayOfWeekAdapter( new HashSet<Integer>(plannedDOW), this, totalheight );
        mRecyclerView.setAdapter(mAdapter);

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
            datasource = MsgAndDayRecords.getInstance();
        }
        datasource.openDatabase();
        datasource.updateLatestDayRecordIsGymDay(gymDay);
        MsgAndDayRecords.getInstance().closeDatabase();
        executeUpdateCallback(false); //Update the newsfeed fragment
    }
}
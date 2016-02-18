package com.pipit.agc.agc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pipit.agc.agc.data.DayRecord;
import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.data.Message;

import java.util.Date;
import java.util.List;

/**
 * Created by Eric on 1/21/2016.
 */
public class NewsfeedFragment extends android.support.v4.app.Fragment implements DayOfWeekPickerFragment.UpdateGymDayToday{
    private DBRecordsSource datasource;
    private List<Message> _allMessages;
    private List<DayRecord> _allDayRecords;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private LinearLayout _gymstatusLayout;
    private CardView _gymstatus_cv;
    private TextView _gymstatus_header;
    private TextView _gymstatus_body;

    public static NewsfeedFragment newInstance() {
        NewsfeedFragment fragment = new NewsfeedFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    public NewsfeedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.newsfeed, container, false);
        _gymstatusLayout = (LinearLayout) rootView.findViewById(R.id.gymstatus_layout);
        _gymstatusLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
                prefs.edit().putBoolean("showGymStatus", false).commit();
                _gymstatusLayout.setVisibility(View.GONE);
            }
        });
        _gymstatus_cv = (CardView) rootView.findViewById(R.id.gymstatus_cv);
        _gymstatus_cv.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.primary_material_dark));
        _gymstatus_body = (TextView) _gymstatus_cv.findViewById(R.id.gymstatus_body);
        _gymstatus_header = (TextView) _gymstatus_cv.findViewById(R.id.gymstatus_header);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        datasource = DBRecordsSource.getInstance();
        datasource.openDatabase();
        _allMessages = datasource.getAllMessages();
        _allDayRecords = datasource.getAllDayRecords();

        mAdapter = new NewsfeedAdapter(_allMessages, _allDayRecords, getActivity());
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        datasource.openDatabase();

        //Logic for showing Gym Status card
        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        boolean showStatus=prefs.getBoolean("showGymStatus", true);
        if (showStatus && _gymstatus_header != null && _gymstatus_body!=null){
            _gymstatus_header.setText(getGymDay());
            _gymstatus_body.setText(getDayComments());
            _gymstatusLayout.setVisibility(View.VISIBLE);
        }else{
            _gymstatusLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        DBRecordsSource.getInstance().closeDatabase();
        super.onPause();
    }
    @Override
    public void todayIsGymDay(boolean isGymDay){
        Log.d("Eric", "todayIsGymDay");

        if (_allDayRecords==null) return;
        _allDayRecords.get(_allDayRecords.size()-1).setIsGymDay(isGymDay);
        if (isGymDay){
            _gymstatus_header.setText("GYM DAY");
        }else{
            _gymstatus_header.setText("REST DAY");
        }
    }

    private String getDayComments(){
        if (_allDayRecords==null || _allDayRecords.size()<1){
            return "No available dayrecords";
        }
        DayRecord latestDate = _allDayRecords.get(_allDayRecords.size()-1); //Todo: Right function to find or sort dayrecord by date
        Date systemDate = new Date();
        if (latestDate.compareToDate(systemDate)){
            if (latestDate.beenToGym()){
                return "You've been to the gym today";
            }
            else return "You have not been to the gym today";
        }
        return "Could not retrieve today's status";
    }

    private String getGymDay(){
        DayRecord latestDate = _allDayRecords.get(_allDayRecords.size()-1);
        if (latestDate.isGymDay()){
            return getResources().getString(R.string.gym_day);
        }else{
            return getResources().getString(R.string.rest_day);
        }
    }

    public void updateGymStatusVisibile(){
        _gymstatusLayout.setVisibility(View.VISIBLE);
    }
}

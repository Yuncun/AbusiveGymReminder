package com.pipit.agc.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pipit.agc.R;
import com.pipit.agc.adapter.DayOfWeekAdapter;
import com.pipit.agc.data.MsgAndDayRecords;
import com.pipit.agc.model.DayRecord;
import com.pipit.agc.util.Constants;
import com.pipit.agc.util.SharedPrefUtil;
import com.pipit.agc.util.Util;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Eric on 4/27/2016.
 */
public class IntroDayPickerFragment  extends android.support.v4.app.Fragment {
    private static final String TAG = "DayPickerFragment";
    DayOfWeekAdapter _adapter;
    private MsgAndDayRecords datasource;
    private List<DayRecord> _allPreviousDays;

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private Button continuebutton;
    private TextView instructions_one;
    private LinearLayout finishbuttonlayout;

    public static IntroDayPickerFragment newInstance() {
        IntroDayPickerFragment fragment = new IntroDayPickerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public IntroDayPickerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.intro_daypicker_layout, container, false);
        synchronized (this) {
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
        instructions_one = (TextView) rootView.findViewById(R.id.daypicker_instructions);
        continuebutton = (Button) rootView.findViewById(R.id.finishbutton_daypicker);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        /*Calculate the height of the daypicker buttons*/
        LinearLayout bodylayout = (LinearLayout) rootView.findViewById(R.id.bodylayout);
        finishbuttonlayout = (LinearLayout) rootView.findViewById(R.id.intro_bottombar);
        float bottombarweight = ((LinearLayout.LayoutParams)finishbuttonlayout.getLayoutParams()).weight;
        float recyclervieweight = ((LinearLayout.LayoutParams) bodylayout.getLayoutParams()).weight;
        float instructionsweight = ((LinearLayout.LayoutParams)instructions_one.getLayoutParams()).weight;
        float screenheight = Math.round(Util.getScreenHeightMinusStatusBar(getContext()));
        float allocatedheight = screenheight*(recyclervieweight / (recyclervieweight+instructionsweight+bottombarweight));

        Log.d(TAG, "recyclervieweight  " + recyclervieweight + " insructionsheight " + instructionsweight
                + " bottombarweight " + bottombarweight + " allocatedheight " + allocatedheight);
        mAdapter = new DayOfWeekAdapter(new HashSet<Integer>(plannedDOW), this, (int) allocatedheight);
        mRecyclerView.setAdapter(mAdapter);

        instructions_one.setText("Pick your gym days");
        continuebutton.setText("Finish");
        continuebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        return rootView;
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    private Calendar getCalFromListPosition(int pos) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, pos);
        return cal;
    }

}
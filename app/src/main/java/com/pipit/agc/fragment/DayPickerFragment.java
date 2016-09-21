package com.pipit.agc.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.pipit.agc.adapter.DayPickerAdapter;
import com.pipit.agc.data.MsgAndDayRecords;
import com.pipit.agc.util.Constants;
import com.pipit.agc.R;
import com.pipit.agc.util.SharedPrefUtil;
import com.pipit.agc.util.Util;
import com.pipit.agc.model.DayRecord;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * @Deprecated - ONLY USE AS A RESOURCE
 * This was version one
 */
public class DayPickerFragment extends ListFragment implements AbsListView.OnScrollListener {
    private static final String TAG = "DayPickerFragment";
    DayPickerAdapter _adapter;
    private final static String ARG_SECTION_NUMBER = "section_number";
    private MsgAndDayRecords datasource;
    private List<DayRecord> _allPreviousDays;

    public static DayPickerFragment newInstance(int sectionNumber) {
        DayPickerFragment fragment = new DayPickerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public DayPickerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.day_picker_fragment, container, false);
        datasource = MsgAndDayRecords.getInstance();
        datasource.openDatabase();
        _allPreviousDays = datasource.getAllDayRecords();

        SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        List<String> exceptionDaysList = SharedPrefUtil.getListFromSharedPref(prefs, Constants.SHAR_PREF_EXCEPT_DAYS);
        List<String> plannedDOWstrs = SharedPrefUtil.getListFromSharedPref(prefs, Constants.SHAR_PREF_PLANNED_DAYS);
        List<Integer> plannedDOW = Util.listOfStringsToListOfInts(plannedDOWstrs);

        _adapter = new DayPickerAdapter(getActivity(), _allPreviousDays, new HashSet<String>(exceptionDaysList),
                new HashSet<Integer>(plannedDOW));
        setListAdapter(_adapter);

        return rootView;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        getListView().setOnScrollListener(this);
        getListView().setVerticalScrollBarEnabled(false);
        getListView().setSelectionFromTop(_allPreviousDays.size() - 1, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getListView().setNestedScrollingEnabled(true);
        }
    }
    @Override
    public void onScroll(AbsListView view,
                         int firstVisible, int visibleCount, int totalCount) {

        boolean loadMore = /* maybe add a padding */
                firstVisible + visibleCount >= totalCount;

        if(loadMore) {
            _adapter.count += visibleCount; // or any other amount
            Log.d(TAG, "Loaded more adapter count " + _adapter.getCount());
            _adapter.notifyDataSetChanged();
        }
    }

    public void onScrollStateChanged(AbsListView v, int s) { }

    @Override
    public void onResume() {
        super.onResume();
        datasource.openDatabase();
    }

    @Override
    public void onPause() {
        MsgAndDayRecords.getInstance().closeDatabase();
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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Calendar cal = getCalFromListPosition(position);
        String datestr = (Integer.toString(cal.get(Calendar.DAY_OF_WEEK)));

        /*Remove or Add the date to the list*/
        SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        List<String> dates = (SharedPrefUtil.getListFromSharedPref(prefs, Constants.SHAR_PREF_PLANNED_DAYS));
        if (dates.contains(datestr)){
            //The clicked date was previously a Gym Day, and we need to toggle it off
            dates.remove(datestr);
            Log.d(TAG, "Removed day " + datestr + " from weekly gym days");
            ((TextView) v.findViewById(R.id.comment)).setText(getActivity().getResources().getText(R.string.rest_day));
            v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.basewhite));
        }else{
            dates.add(datestr);
            Log.d(TAG, "Added day " + datestr + " to weekly gym days");
            ((TextView) v.findViewById(R.id.comment)).setText(getActivity().getResources().getText(R.string.gym_day));
            v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lightgreen));
        }
        SharedPrefUtil.putListToSharedPref(prefs.edit(), Constants.SHAR_PREF_PLANNED_DAYS, dates);
        _adapter.updateData(null, null, new HashSet<Integer>(Util.listOfStringsToListOfInts(dates)));
    }

    private Calendar getCalFromListPosition(int pos){
        int diff = pos-(_allPreviousDays.size()-1);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, diff);
        return cal;
    }


}


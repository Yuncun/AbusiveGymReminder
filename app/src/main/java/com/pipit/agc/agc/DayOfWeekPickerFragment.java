package com.pipit.agc.agc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.data.DayRecord;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Eric on 2/3/2016.
 */
public class DayOfWeekPickerFragment extends ListFragment{
    private static final String TAG = "DayPickerFragment";
    DayOfWeekAdapter _adapter;
    private final static String ARG_SECTION_NUMBER = "section_number";
    private DBRecordsSource datasource;
    private List<DayRecord> _allPreviousDays;

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
        datasource = DBRecordsSource.getInstance();
        datasource.openDatabase();
        _allPreviousDays = datasource.getAllDayRecords();

        SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        List<String> plannedDOWstrs = Util.getListFromSharedPref(prefs, Constants.SHAR_PREF_PLANNED_DAYS);
        List<Integer> plannedDOW = Util.listOfStringsToListOfInts(plannedDOWstrs);

        _adapter = new DayOfWeekAdapter(getActivity(), new HashSet<Integer>(plannedDOW));
        setListAdapter(_adapter);
        return rootView;
    }


    @Override
    public void onPause() {
        DBRecordsSource.getInstance().closeDatabase();
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
        String datestr = (Integer.toString(position));

        /*Remove or Add the date to the list*/
        SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        List<String> dates = (Util.getListFromSharedPref(prefs, Constants.SHAR_PREF_PLANNED_DAYS));
        if (dates.contains(datestr)){
            //The clicked date was previously a Gym Day, and we need to toggle it off
            dates.remove(datestr);
            Log.d(TAG, "Removed day " + datestr + " from weekly gym days");
            ((TextView) v.findViewById(R.id.comment)).setText(getActivity().getResources().getText(R.string.rest_day));
            ((Switch) v.findViewById(R.id.switch1)).setChecked(false);
            v.setBackgroundColor(getActivity().getResources().getColor(R.color.basewhite));
            if (position==Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1){
                datasource.updateLatestDayRecordIsGymDay(false);
                executeUpdateCallback(false); //Update the newsfeed fragment
            }
        }else{
            dates.add(datestr);
            Log.d(TAG, "Added day " + datestr + " to weekly gym days");
            ((TextView) v.findViewById(R.id.comment)).setText(getActivity().getResources().getText(R.string.gym_day));
            ((Switch) v.findViewById(R.id.switch1)).setChecked(true);
            v.setBackgroundColor(getActivity().getResources().getColor(R.color.lightgreen));
            if (position==Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1){
                datasource.updateLatestDayRecordIsGymDay(true);
                executeUpdateCallback(true);
            }
        }
        Util.putListToSharedPref(prefs.edit(), Constants.SHAR_PREF_PLANNED_DAYS, dates);
        _adapter.updateData(null, null, new HashSet<Integer>(Util.listOfStringsToListOfInts(dates)));

    }

    private Calendar getCalFromListPosition(int pos){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, pos);
        return cal;
    }

    private void executeUpdateCallback(boolean isGymDay) {
        Log.d("Eric", "Execute callback");
        Fragment registeredFrag = ((AllinOneActivity) getActivity()).getFragmentByKey(Constants.NEWSFEED_FRAG);
        if (registeredFrag!=null){
            UpdateGymDayToday update = (UpdateGymDayToday) registeredFrag;
            update.todayIsGymDay(isGymDay);
        }
    }
}
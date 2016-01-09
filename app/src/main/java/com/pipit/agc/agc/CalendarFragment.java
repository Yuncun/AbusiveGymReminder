package com.pipit.agc.agc;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

public class CalendarFragment extends Fragment {

    private CalendarView _calendar;
    private CardView _dayCard;
    private TextView _dayCardTextView;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "CalendarFragment";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static CalendarFragment newInstance(int sectionNumber) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public CalendarFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);
        ((TextView) rootView.findViewById(R.id.section_label)).setText(ARG_SECTION_NUMBER);
        _calendar = (CalendarView) rootView.findViewById(R.id.calendar);
        _dayCard = (CardView) rootView.findViewById(R.id.cv);
        _dayCardTextView = (TextView) rootView.findViewById(R.id.info_text);
        initializeCalendar();
        return rootView;
    }


    public void initializeCalendar() {
        if(_calendar==null){
            return;
        }
        // sets whether to show the week number.
        _calendar.setShowWeekNumber(false);

        // sets the first day of week according to Calendar.
        // here we set Monday as the first day of the Calendar
        _calendar.setFirstDayOfWeek(2);

        //The background color for the selected week.
        _calendar.setSelectedWeekBackgroundColor(getResources().getColor(R.color.green));

        //sets the color for the dates of an unfocused month.
        _calendar.setUnfocusedMonthDateColor(getResources().getColor(R.color.transparent));

        //sets the color for the separator line between weeks.
        _calendar.setWeekSeparatorLineColor(getResources().getColor(R.color.transparent));

        //sets the color for the vertical bar shown at the beginning and at the end of the selected date.
        _calendar.setSelectedDateVerticalBar(R.color.darkgreen);

        //sets the listener to be notified upon selected date change.
       _calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            //show the selected date as a toast
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int day) {
                Log.d(TAG, "onSelectDayChange + " + day + "/" + month + "/" + year);
                Toast.makeText(getActivity(), day + "/" + month + "/" + year, Toast.LENGTH_LONG).show();
                _dayCardTextView.setText("DAY: "  + day + "/" + month + "/" + year );
            }
        });



    }
}

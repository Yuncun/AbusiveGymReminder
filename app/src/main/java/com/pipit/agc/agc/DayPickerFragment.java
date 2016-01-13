package com.pipit.agc.agc;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.pipit.agc.agc.data.DayRecord;
import com.pipit.agc.agc.data.DayRecordsSource;

import java.util.List;

import javax.sql.DataSource;

public class DayPickerFragment extends ListFragment implements AbsListView.OnScrollListener {
    private static final String TAG = "DayPickerFragment";
    DayPickerAdapter _adapter;
    private final static String ARG_SECTION_NUMBER = "section_number";
    private DayRecordsSource datasource;
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
        String[] values = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday"};
        datasource = DayRecordsSource.getInstance();
        datasource.openDatabase();
        _allPreviousDays = datasource.getAllDayRecords();
        _adapter = new DayPickerAdapter(getActivity(), values, _allPreviousDays);
        setListAdapter(_adapter);

        return rootView;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        getListView().setOnScrollListener(this);
        getListView().setVerticalScrollBarEnabled(false);
        getListView().setSelectionFromTop(_allPreviousDays.size(), 0);
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
        DayRecordsSource.getInstance().closeDatabase();
        super.onPause();
    }

}


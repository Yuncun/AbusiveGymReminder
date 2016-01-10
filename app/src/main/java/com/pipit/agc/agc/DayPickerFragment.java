package com.pipit.agc.agc;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

public class DayPickerFragment extends ListFragment implements AbsListView.OnScrollListener {
    private static final String TAG = "DayPickerFragment";
    DayPickerAdapter _adapter;
    private final static String ARG_SECTION_NUMBER = "section_number";
    int daysActive=7;

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
        String[] values = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday"};
        _adapter = new DayPickerAdapter(getActivity(), values, daysActive);
        setListAdapter(_adapter);
        getListView().setOnScrollListener(this);
        getListView().setVerticalScrollBarEnabled(false);
        getListView().setSelectionFromTop(daysActive, 0);
    }

    public DayPickerFragment() {
    }
/*
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_allin_one, container, false);
        ((TextView) rootView.findViewById(R.id.section_label)).setText(ARG_SECTION_NUMBER);
        return rootView;
    }*/
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

}


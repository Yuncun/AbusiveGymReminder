package com.pipit.agc.agc;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pipit.agc.agc.data.DayRecord;
import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.data.Message;

import java.util.List;

/**
 * Created by Eric on 1/21/2016.
 */
public class NewsfeedFragment extends android.support.v4.app.Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DBRecordsSource datasource;
    private List<Message> _allMessages;
    private List<DayRecord> _allDayRecords;

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
    }

    @Override
    public void onPause() {
        DBRecordsSource.getInstance().closeDatabase();
        super.onPause();
    }

}

package com.pipit.agc.agc.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pipit.agc.agc.R;
import com.pipit.agc.agc.adapter.NewsfeedAdapter;
import com.pipit.agc.agc.adapter.PreferencesAdapter;
import com.pipit.agc.agc.adapter.StatisticsRecyclerViewAdapter;
import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.util.StatsContent;

import java.util.Collections;

/**
 * Created by Eric on 5/8/2016.
 */
public class PreferencesFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private static final String TAG = "NewsfeedFragment";

    public static PreferencesFragment newInstance() {
        PreferencesFragment fragment = new PreferencesFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    public PreferencesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_preferences_list, container, false);


        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new PreferencesAdapter(getContext());
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

}

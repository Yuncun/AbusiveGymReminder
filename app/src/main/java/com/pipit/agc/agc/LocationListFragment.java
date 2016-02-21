package com.pipit.agc.agc;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;

/**
 * Holds list of gym locations
 */
public class LocationListFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    public LocationListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static LocationListFragment newInstance(int columnCount) {
        LocationListFragment fragment = new LocationListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.locationlist_layout, container, false);
        RecyclerView recyclerlist = (RecyclerView) view.findViewById(R.id.recyclerlist);
        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) recyclerlist;
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        recyclerView.setAdapter(new LocationListAdapter(getGymLocations(), mListener));

        return view;
    }

    public List<Gym> getGymLocations(){
        SharedPreferences prefs = getActivity().getApplicationContext()
                .getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        List<Gym> gymlocations = new ArrayList<Gym>();
        for (int i=1; i<4; i++){
            double lat = Util.getDouble(prefs, "lat" + i, Constants.DEFAULT_COORDINATE);
            double lng = Util.getDouble(prefs, "lng" + i, Constants.DEFAULT_COORDINATE);
            Gym gym = new Gym();
            gym.location = new Location("");
            gym.location.setLongitude(lng);
            gym.location.setLatitude(lat);
            gym.address = prefs.getString("address" + i, "No address");
            gym.proxid =  prefs.getInt("proxalert"+i, 0);
            gym.name = prefs.getString("name"+i, "No name");
            gymlocations.add(gym);
        }
        return gymlocations;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);/*
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction();
    }
}

package com.pipit.agc.agc.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.pipit.agc.agc.controller.GeofenceController;
import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.adapter.LocationListAdapter;
import com.pipit.agc.agc.R;
import com.pipit.agc.agc.util.SharedPrefUtil;
import com.pipit.agc.agc.model.Gym;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds list of gym locations
 */
public class LocationListFragment extends Fragment {

    private static final String TAG = "LocationListFragment";
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    public int mFlag;
    public int mHighestProxId;
    private RecyclerView mRecyclerView;
    FloatingActionButton mFab;

    private GeofenceController.GeofenceControllerListener mListener  = new GeofenceController.GeofenceControllerListener() {
        @Override
        public void onGeofencesUpdated() {
            Log.d(TAG, "updating layout in onGeofenceUpdated");
            refresh();
        }

        @Override
        public void onError() {
            Toast.makeText(getContext(), "Error adding geofence", Toast.LENGTH_SHORT);
        }
    };

    public LocationListFragment() {
    }

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
        mHighestProxId = SharedPrefUtil.getInt(getContext(), Constants.HIGHEST_PROXID, 0);
        mFlag=0;
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.locationlist_layout, container, false);
        RecyclerView recyclerlist = (RecyclerView) view.findViewById(R.id.recyclerlist);
        mFab = (FloatingActionButton) view.findViewById(R.id.locationsFab);
        mFab.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.schemethree_darkerteal));
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlacePicker(mHighestProxId+1);
            }
        });

        // Set the adapter
        Context context = view.getContext();
        mRecyclerView = (RecyclerView) recyclerlist;
        if (mColumnCount <= 1) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        List<Gym> gymlocations = SharedPrefUtil.getGeofenceList(getContext());
        mRecyclerView.setAdapter(new LocationListAdapter(gymlocations, mListener, this));
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void startPlacePicker(int i){
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(getActivity());
            mFlag = i;

            // Start the Intent by requesting a result, identified by a request code.
            startActivityForResult(intent, Constants.REQUEST_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException e) {
            GooglePlayServicesUtil
                    .getErrorDialog(e.getConnectionStatusCode(), getActivity(), 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(getActivity(), "Google Play Services is not available.",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_PLACE_PICKER) {

            if (resultCode == Activity.RESULT_OK) {
                /* User has picked a place, extract data.
                   Data is extracted from the returned intent by retrieving a Place object from
                   the PlacePicker.
                 */
                final Place place = PlacePicker.getPlace(data, getActivity());

                /* A Place object contains details about that place, such as its name, address
                and phone number. Extract the name, address, phone number, place ID and place types.
                 */
                final CharSequence name = place.getName();
                final CharSequence address = place.getAddress();
                final CharSequence phone = place.getPhoneNumber();
                final String placeId = place.getId();
                final LatLng location = place.getLatLng();
                String attribution = PlacePicker.getAttributions(data);
                if(attribution == null){
                    attribution = "";
                }
                if (mFlag < 0){
                    Log.e(TAG, "no proxID given for location picker");
                    return;
                }
                int id = mFlag;
                mFlag=-1;
                Log.d(TAG, "Just picked a location, id=" + id);
                if (id > mHighestProxId) {
                    mHighestProxId = id;
                }
                SharedPrefUtil.putInt(getContext(), Constants.HIGHEST_PROXID, mHighestProxId);

                Gym gym = new Gym();
                gym.location = new Location("");
                gym.location.setLongitude(location.longitude);
                gym.location.setLatitude(location.latitude);
                gym.address = "" + place.getAddress();
                gym.name = "" + place.getName();
                gym.proxid = id;

                GeofenceController.getInstance().addGeofenceByGym(gym, mListener, true);
                SharedPrefUtil.addGeofenceToSharedPrefs(getContext(), gym);
            } else {
                Log.d(TAG, "resultCode is wrong " + resultCode);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void refresh(){
        mRecyclerView.setAdapter(new LocationListAdapter(SharedPrefUtil.getGeofenceList(getContext()), mListener, this));
    }
}

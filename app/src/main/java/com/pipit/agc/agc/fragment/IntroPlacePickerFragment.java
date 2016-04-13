package com.pipit.agc.agc.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.pipit.agc.agc.R;
import com.pipit.agc.agc.activity.IntroductionActivity;
import com.pipit.agc.agc.model.Gym;
import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.util.GeofenceUtils;

/**
 * Created by Eric on 4/11/2016.
 */
public class IntroPlacePickerFragment extends Fragment {
    public final static String TAG = "IntroPlacePickerFragment";
    private TextView _finishButton;
    private TextView _instructions_tv;
    private TextView _launchcircle;
    private LinearLayout _gobuttonlayout;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.intro_place_picker,
                container, false);
        _launchcircle = (TextView) view.findViewById(R.id.placepicker_card);
        _launchcircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlacePicker();
            }
        });
        _launchcircle.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.lime), PorterDuff.Mode.SRC_ATOP);
        _launchcircle.setTextColor(ContextCompat.getColor(getContext(), R.color.basewhite));

        _finishButton = (TextView) view.findViewById(R.id.placepicker_done);
        _gobuttonlayout = (LinearLayout) view.findViewById(R.id.continue_layout);

        _instructions_tv = (TextView) view.findViewById(R.id.placepicker_instructions);
        _instructions_tv.setText("Where is your gym?");

        _finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //((IntroductionActivity) getActivity()).selectFrag(2);
                getActivity().finish();
            }
        });

        /*Set the "Continue" area to default mode when no gym is selected */
        _finishButton.setText("Skip for now");
        _finishButton.setTextColor((ContextCompat.getColor(getContext(), R.color.basewhite)));
        _gobuttonlayout.setBackgroundColor((ContextCompat.getColor(getContext(), R.color.nice_purple)));
        _finishButton.setTextSize(16);

        return view;
    }

    public void startPlacePicker(){
        int i = 1;
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(getActivity());
            intent.putExtra("proxid", i);

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
            int id = 1; //1 is the default id for initial gym

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

                Gym gym = new Gym();
                gym.location = new Location("");
                gym.location.setLongitude(location.longitude);
                gym.location.setLatitude(location.latitude);
                gym.address = "" + place.getAddress();
                gym.name = "" + place.getName();
                gym.proxid = id;

                addGeofenceFromListposition(gym);

                /*Set the "Continue" area to default mode when no gym is selected */
                _finishButton.setText("Finish");
                _finishButton.setTextSize(30);
                _finishButton.setTextColor(getResources().getColor(android.R.color.primary_text_light));
                _gobuttonlayout.setBackgroundColor((ContextCompat.getColor(getContext(), R.color.basewhite)));
                String k = "";
                if (gym.name!=null && !gym.name.isEmpty()){
                    k+=gym.name + "\n";
                }
                if (gym.address!=null && !gym.address.isEmpty()){
                    k+=gym.address;
                }
                if (k=="" || k.isEmpty()){
                    k+=location.toString();
                }
                _launchcircle.setTextSize(20);
                _launchcircle.setText(k);
                getView();
            } else {
                Log.d(TAG, "resultCode is wrong " + resultCode);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void addGeofenceFromListposition(Gym gym){
        SharedPreferences prefs = getContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        Log.d(TAG, "addGeoFenceFromListPosition n=" + gym.proxid);

        if (gym == null || gym.location==null || gym.location.getLatitude() == Constants.DEFAULT_COORDINATE
                || gym.location.getLongitude() == Constants.DEFAULT_COORDINATE){
            Log.d(TAG, "addGeoFenceFromListPosition gym retrieved from " + gym.proxid + " is null");
            return;
        }
        Geofence g = new Geofence.Builder()
                .setRequestId(Integer.toString(gym.proxid))
                .setCircularRegion(
                        gym.location.getLatitude(),
                        gym.location.getLongitude(),
                        Constants.DEFAULT_RADIUS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(1000 * 60 * 1)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                        //.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        //        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        GeofenceUtils.addGeofenceToSharedPrefs(getContext(), gym);
    }

}


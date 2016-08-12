package com.pipit.agc.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.pipit.agc.R;
import com.pipit.agc.activity.IntroductionActivity;
import com.pipit.agc.model.Gym;
import com.pipit.agc.util.Constants;
import com.pipit.agc.util.SharedPrefUtil;

/**
 * Created by Eric on 4/11/2016.
 */
public class IntroPlacePickerFragment extends Fragment {
    public final static String TAG = "IntroPlacePickerFrag";
    private Button continuebutton;
    private TextView instructions_one;
    private LinearLayout pickerlauncher;
    private TextView pickercardtext;
    private TextView pickersubtitle;
    private TextView skipbutton;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.intro_place_picker,
                container, false);
        pickerlauncher = (LinearLayout) view.findViewById(R.id.placepicker_card);
        pickerlauncher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlacePicker();
            }
        });
        //pickerlauncher.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.schemefour_darkerteal));
        pickercardtext = (TextView) pickerlauncher.findViewById(R.id.gym_name);
        pickercardtext.setText("Touch here to find your gym");
        TextView removebutton = (TextView) pickerlauncher.findViewById(R.id.removeButton);
        removebutton.setVisibility(View.GONE);

        pickersubtitle = (TextView) pickerlauncher.findViewById(R.id.content);

        /*pickerlauncher.setImageDrawable(new TextDrawable(getContext(), "Touch to ", ColorStateList.valueOf(Color.WHITE),
                30, TextDrawable.VerticalAlignment.BASELINE));*/
        continuebutton = (Button) view.findViewById(R.id.finishbutton_placepicker);

        instructions_one = (TextView) view.findViewById(R.id.placepicker_instructions);
        instructions_one.setText("Gym visits are automatically registered when you are near your gym");

        continuebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((IntroductionActivity) getActivity()).selectFrag(3);
            }
        });

        //continuebutton.setVisibility(View.GONE);

        skipbutton = (TextView) view.findViewById(R.id.skipbutton);
        skipbutton.setText("Skip for now");
        skipbutton.setVisibility(View.GONE);

        LinearLayout bottomBar = (LinearLayout) view.findViewById(R.id.intro_bottombar);
        bottomBar.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.basewhite));

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
                continuebutton.setText("Pick your gym days");
                String k = "";
                if (gym.name!=null && !gym.name.isEmpty()){
                    k+=gym.name + "\n";
                }

                continuebutton.setVisibility(View.VISIBLE);
                skipbutton.setVisibility(View.GONE);
                pickercardtext.setText(k);
                pickersubtitle.setText(gym.address);
                //pickerlauncher.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.basewhite));
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

        SharedPrefUtil.addGeofenceToSharedPrefs(getContext(), gym);
    }

}


package com.pipit.agc.agc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

public class PlacePickerFragment extends Fragment {
    private String TAG = "PlacePickerFragment";
    private TextView _address;
    private String _addressDefault="No address";
    private Button _launchPlacePicker;
    private TextView _rangeDescription;
    private EditText _rangeEditText;
    private Button _submitButton;
    private Button _removeProxAlertsButton;

    public static PlacePickerFragment newInstance() {
        PlacePickerFragment fragment = new PlacePickerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public PlacePickerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        final SharedPreferences.Editor editor = prefs.edit();
        _addressDefault = prefs.getString("address", "no address") + "lat" +
                Util.getDouble(prefs, "lat", 0) + " lng" + Util.getDouble(prefs, "lng", 0) ;

        View rootView = inflater.inflate(R.layout.fragment_place_picker, container, false);
        _launchPlacePicker=(Button) rootView.findViewById(R.id.launch_placepicker);
        _launchPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPlacePicker();
            }
        });
        _address=(TextView) rootView.findViewById(R.id.addressText);
        _address.setText(_addressDefault);
        _rangeDescription = (TextView) rootView.findViewById(R.id.range_description);
        _rangeDescription.setText(getResources().getText(R.string.range_picker_description) + " Currently "
                + prefs.getInt("range", -1));
        _rangeEditText=(EditText) rootView.findViewById(R.id.rangesetting);
        //_rangeEditText.setText(prefs.getInt("range", -1));
        _submitButton = (Button) rootView.findViewById(R.id.submit_button);
        _submitButton.setText(getResources().getText(R.string.submit));
        _submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String str = _rangeEditText.getText().toString();
                try {
                    int range = Integer.parseInt(str);
                    Toast.makeText(getActivity(), "range set to " + str, Toast.LENGTH_SHORT).show();
                    editor.putInt("range", range);
                    editor.commit();
                }
                catch(Exception e) {
                    Log.e("logtag", "Exception: " + e.toString());
                    Toast.makeText(getActivity(), "Invalid range " + str, Toast.LENGTH_SHORT).show();
                }
            }
        });

        _removeProxAlertsButton = (Button) rootView.findViewById(R.id.remove_prox_button);
        _removeProxAlertsButton.setText("Remove All Locations");
        _removeProxAlertsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Removing all saved locations", Toast.LENGTH_SHORT).show();
                ((AllinOneActivity) getActivity()).removeAllProximityAlerts(getActivity());
                SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
                SharedPreferences.Editor editor = prefs.edit();
                Util.putDouble(editor, "lat",  0).commit();
                Util.putDouble(editor, "lng", 0).commit();
                editor.putString("address", "none").commit();
            }
        });

        return rootView;
    }

    private void startPlacePicker(){
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(getActivity());
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
        // BEGIN_INCLUDE(activity_result)
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

                Log.d(TAG, "Place selected: " + placeId + " (" + name.toString() + ")");
                _addressDefault = "Your gym is " + name.toString() + " \nat " + address.toString() + "\nCoordinates at " + location.toString();
                _address.setText(_addressDefault);

                SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("address", address.toString()).commit();
                ((AllinOneActivity) getActivity()).addProximityAlert(location.latitude, location.longitude);

            } else {
                Log.d(TAG, "resultCode is wrong " + "resultCode");

            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        // END_INCLUDE(activity_result)
    }



}

package com.pipit.agc.agc.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.pipit.agc.agc.adapter.IntroAdapter;
import com.pipit.agc.agc.adapter.IntroPageTransformer;
import com.pipit.agc.agc.model.Gym;
import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.util.GeofenceUtils;
import com.viewpagerindicator.CirclePageIndicator;

/**
 * Created by Eric on 12/13/2015.
 */
public class IntroFragment extends Fragment {
    public static final String TAG = "IntroFragment";
    private TextView _finishButton;
    private ViewPager mViewPager;
    private CirclePageIndicator _indicator;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.intro_opening_message,
                container, false);

        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);

        // Set an Adapter on the ViewPager
        mViewPager.setAdapter(new IntroAdapter(getActivity().getSupportFragmentManager()));

        // Set a PageTransformer
        mViewPager.setPageTransformer(false, new IntroPageTransformer());

        _indicator  = (CirclePageIndicator) view.findViewById(R.id.titles);
        _indicator.setViewPager(mViewPager);
       // _indicator.setStrokeColor(ContextCompat.getColor(getContext(), R.color.schemefour_yellow));
       //         _indicator.setPageColor(ContextCompat.getColor(getContext(), R.color.schemefour_teal));

        _finishButton = (TextView) view.findViewById(R.id.finishbutton);
        _finishButton.setText("Get Started!");
        _finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((IntroductionActivity)getActivity()).selectFrag(1);
            }
        });

        return view;
    }
}

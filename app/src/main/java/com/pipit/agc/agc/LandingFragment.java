package com.pipit.agc.agc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Eric on 12/8/2015.
 */
public class LandingFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static TextView _locationCounter;
    private Button _button;


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static LandingFragment newInstance(int sectionNumber) {
        LandingFragment fragment = new LandingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public LandingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.landing_fragment, container, false);
        _locationCounter = (TextView) rootView.findViewById(R.id.landing_text);
        final SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        _button = (Button) rootView.findViewById(R.id.button);
        _button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("trackcount", 0);
                editor.commit();
                refresh();
            }
        });

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        refresh();
    }

    private void refresh(){
        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS, getActivity().MODE_MULTI_PROCESS);
        int trackcount = prefs.getInt("trackcount", 0);
        _locationCounter.setText("Alarm has gone off " + trackcount + " times");
        Log.d("LandingFragment", "refresh, count is " + trackcount);
        ViewGroup vg = (ViewGroup) getActivity().findViewById(R.id.landing_layout);
        vg.invalidate();
    }

}

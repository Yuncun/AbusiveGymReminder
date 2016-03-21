package com.pipit.agc.agc.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.pipit.agc.agc.R;
import com.pipit.agc.agc.activity.IntroductionActivity;

/**
 * Created by Eric on 12/13/2015.
 */
public class IntroFragment extends Fragment {

   private TextView _finishButton;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.intro_opening_message,
                container, false);

        _finishButton = (TextView) view.findViewById(R.id.finishbutton);
        _finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((IntroductionActivity)getActivity()).selectFrag(1);
            }
        });
        return view;
    }


}

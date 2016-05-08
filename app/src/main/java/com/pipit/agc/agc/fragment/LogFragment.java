package com.pipit.agc.agc.fragment;

import android.app.Activity;
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

import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.R;
import com.pipit.agc.agc.util.SharedPrefUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Eric on 12/8/2015.
 */
public class LogFragment extends Fragment {

    private static final String TAG = "LogFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private TextView _lastLocationTxt;
    private Button _resetCountButton;
    private Context _context;
    private TextView _infoOneKey;
    private TextView _infoTwoKey;
    private TextView _infoOneVal;
    private TextView _infoTwoVal;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static LogFragment newInstance(int sectionNumber) {
        LogFragment fragment = new LogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public LogFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.landing_fragment, container, false);

        final SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        String lastLocation=prefs.getString("locationlist", "");
        _lastLocationTxt = (TextView) rootView.findViewById(R.id.lastLocation);
        _lastLocationTxt.setText(lastLocation);
        _resetCountButton = (Button) rootView.findViewById(R.id.button);

        _resetCountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("trackcount", 0);
                editor.putString("locationlist", "");
                editor.commit();
            }
        });

        _infoOneKey = (TextView) rootView.findViewById(R.id.info_one_key);
        _infoTwoKey = (TextView) rootView.findViewById(R.id.info_two_key);

        DateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");

        /*Last gym time*/
        _infoOneKey.setText("Last gym time");
        _infoOneVal = (TextView) rootView.findViewById(R.id.info_one_val);
        Calendar cal = Calendar.getInstance();
        long lastvisit = SharedPrefUtil.getLong(getContext(), "lastgymtime", -1);
        cal.setTimeInMillis(lastvisit);
        if (lastvisit > 0){
            _infoOneVal.setText(dateFormat.format(cal.getTime()));
        }else{
            _infoOneVal.setText("No Record");
        }

        /*Next notification time*/
        _infoTwoVal = (TextView) rootView.findViewById(R.id.info_two_val);
        _infoTwoKey.setText("Next notification time");
        cal = Calendar.getInstance();
        long nextnotif = SharedPrefUtil.getLong(getContext(), "nextnotificationtime", -1);
        cal.setTimeInMillis(nextnotif);
        if (nextnotif > 0){
            _infoTwoVal.setText(dateFormat.format(cal.getTime()));
        }else{
            _infoTwoVal.setText("No Record");
        }

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        if(_context==null){
            _context=getActivity();
        }
    }

    public void updateLastLocation(String txt){
        _lastLocationTxt.setText(txt);
        SharedPreferences prefs = _context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("locationlist", txt);
        editor.commit();
    }

    public void addLineToLog(String txt){
        SharedPreferences prefs = _context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        String prev = prefs.getString("locationlist", " ");
        updateLastLocation(prev+"\n"+txt);
    }
}



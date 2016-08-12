package com.pipit.agc.fragment;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.pipit.agc.adapter.NewsfeedAdapter;
import com.pipit.agc.activity.AllinOneActivity;
import com.pipit.agc.R;
import com.pipit.agc.model.DayRecord;
import com.pipit.agc.model.Message;
import com.pipit.agc.util.StatsContent;

import java.util.Date;
import java.util.List;

/**
 * Created by Eric on 1/21/2016.
 */
public class NewsfeedFragment extends android.support.v4.app.Fragment{
    private List<Message> _allMessages;
    private List<DayRecord> _allDayRecords;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private LinearLayout _gymstatusLayout;
    private CardView _gymstatus_cv;
    private TextView _gymstatus_header;
    private TextView _gymstatus_body;
    private MaterialCab cab;

    private static final String TAG = "NewsfeedFragment";

    public static NewsfeedFragment newInstance() {
        NewsfeedFragment fragment = new NewsfeedFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    public NewsfeedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.newsfeed, container, false);
        //TODO: Note that the gym status bar is currently not active. Code is here but wont do anythig right now
        //TODO: until I make the design decision.
        _gymstatusLayout = (LinearLayout) rootView.findViewById(R.id.gymstatus_layout);
        _gymstatus_cv = (CardView) rootView.findViewById(R.id.gymstatus_cv);
        _gymstatus_cv.setBackgroundColor(fetchAccentColor());

        _gymstatus_body = (TextView) _gymstatus_cv.findViewById(R.id.gymstatus_body);
        _gymstatus_header = (TextView) _gymstatus_cv.findViewById(R.id.gymstatus_header);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        _allMessages = StatsContent.getInstance().getAllMessagesReverse(false);
        _allDayRecords = StatsContent.getInstance().getAllDayRecords(false);

        mAdapter = new NewsfeedAdapter(_allMessages, _allDayRecords, this);
        mRecyclerView.setAdapter(mAdapter);

        cab = ((AllinOneActivity) getActivity()).getCab();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Todo: Decide if I want to add the notification bar when user has no gyms
        /*
        List<Gym> gymlocations = LocationListFragment.getGymLocations(getContext());
        boolean hasNoGym = true;
        for (Gym g : gymlocations){
            if (!g.isEmpty){
                hasNoGym=true;
                break;
            }
        }
        if (hasNoGym){
            _gymstatus_header.setText("No Gyms Selected");
            _gymstatus_body.setText("Click on GYMS tab to find your gym");
            _gymstatusLayout.setVisibility(View.VISIBLE);
        }else{
            _gymstatusLayout.setVisibility(View.GONE);
        }*/
    }

    @Override
    public void onPause(){
        super.onPause();
        getCab().finish();
    }
    private String getDayComments(){
        if (_allDayRecords==null || _allDayRecords.size()<1){
            return "No available dayrecords";
        }
        DayRecord latestDate = _allDayRecords.get(_allDayRecords.size()-1); //Todo: Right function to find or sort dayrecord by date
        Date systemDate = new Date();
        if (latestDate.equalsDate(systemDate)){
            if (latestDate.beenToGym()){
                return "You've been to the gym today";
            }
            else return "You have not been to the gym today";
        }
        return "Could not retrieve today's status";
    }

    private String getGymDay(){
        DayRecord latestDate = _allDayRecords.get(_allDayRecords.size()-1);
        if (latestDate.isGymDay()){
            return getResources().getString(R.string.gym_day);
        }else{
            return getResources().getString(R.string.rest_day);
        }
    }

    private int fetchAccentColor() {
        TypedValue typedValue = new TypedValue();

        TypedArray a = getActivity().obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorAccent });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    public MaterialCab getCab(){
        return cab;
    }

}

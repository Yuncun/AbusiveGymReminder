package com.pipit.agc.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pipit.agc.R;
import com.pipit.agc.data.MsgAndDayRecords;
import com.pipit.agc.model.DayRecord;
import com.pipit.agc.model.Message;
import com.pipit.agc.receiver.AlarmManagerBroadcastReceiver;
import com.pipit.agc.util.Constants;
import com.pipit.agc.util.ReminderOracle;
import com.pipit.agc.util.SharedPrefUtil;
import com.pipit.agc.util.StatsContent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Eric on 1/23/2016.
 */
public class DevTestingFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "DevTestingFragment";

    private MsgAndDayRecords datasource;
    private Button _simulateMidnight;
    private Button _testReceiverAddButton;
    private Button _deleteButton;
    private Button _addDay;
    private Button _startGymVisit;
    private Button _showStartScreen;

    public static DevTestingFragment newInstance(int sectionNumber) {
        DevTestingFragment fragment = new DevTestingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.test_db_fragment, container, false);
        datasource = MsgAndDayRecords.getInstance();
        setButtons(contentView);

        TextView expl_txt = (TextView) contentView.findViewById(R.id.test_db_txt);
        expl_txt.setText(R.string.test_db_txt);

        return contentView;
    }

    public void setButtons(View view) {
        _simulateMidnight = (Button) view.findViewById(R.id.add);
        _simulateMidnight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "doDayLogging from test button");
                //Warning: May cause user to receive a message in a few hours
                AlarmManagerBroadcastReceiver.doDayLogging(getContext());
            }
        });
        _deleteButton = (Button) view.findViewById(R.id.delete);
        _deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DayRecord today = (StatsContent.getInstance().getToday(false));
                Log.d("Eric", "Today: " + today.getId());
                datasource.deleteDayRecord(today);
                Toast.makeText(getContext(), "Attempting to delete day ID:" + today.getId(), Toast.LENGTH_SHORT);
                Log.d("Eric", "Today: " + today.getId());
            }
        });
        _testReceiverAddButton = (Button) view.findViewById(R.id.receivertestadd);
        _testReceiverAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReminderOracle.doLeaveMessageBasedOnPerformance(getContext(), true);
            }
        });
        _addDay = (Button) view.findViewById(R.id.addday);
        _addDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Copy+pasted from AlarmManagerBroadcastReceiver
                MsgAndDayRecords datasource;
                datasource = MsgAndDayRecords.getInstance();
                datasource.openDatabase();
                DayRecord day = new DayRecord();
                day.setComment("You have not been to the gym");
                day.setDate(new Date());
                day.setHasBeenToGym(false);
                Calendar cal = Calendar.getInstance();
                day.setIsGymDay(SharedPrefUtil.getGymStatusFromDayOfWeek(getContext(), cal.get(Calendar.DAY_OF_WEEK)));
                DayRecord dayRecord = datasource.createDayRecord(day);
                datasource.closeDatabase();
                Toast.makeText(getActivity(), "new day added!", Toast.LENGTH_LONG);
            }
        });

        final String gymVisitStart = "Start visit timer";
        final String gymVisitEnd = "End visit timer";
        _startGymVisit = (Button) view.findViewById(R.id.startVisit);

        MsgAndDayRecords datasource;
        datasource = MsgAndDayRecords.getInstance();
        datasource.openDatabase();
        DayRecord today = datasource.getLastDayRecord();
        datasource.closeDatabase();

        if (today.isCurrentlyVisiting()){
            _startGymVisit.setText(gymVisitEnd);
        }
        else{
            _startGymVisit.setText(gymVisitStart);
        }
        _startGymVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MsgAndDayRecords datasource;
                datasource = MsgAndDayRecords.getInstance();
                datasource.openDatabase();
                DayRecord today = datasource.getLastDayRecord();

                //Click to end
                if (today.isCurrentlyVisiting()){
                    if (today.endCurrentVisit()){
                        datasource.updateLatestDayRecordVisits(today.getSerializedVisitsList());
                        _startGymVisit.setText(gymVisitStart);
                    }
                }
                //Click to start
                else{
                    today.startCurrentVisit();
                    datasource.updateLatestDayRecordVisits(today.getSerializedVisitsList());
                    _startGymVisit.setText(gymVisitEnd);

                }
                datasource.closeDatabase();
                Log.d(TAG, "Today's visits" + today.printVisits());
            }
        });
        _showStartScreen = (Button) view.findViewById(R.id.showStartScreen);
        _showStartScreen.setText("Show Intro screen");
        _showStartScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            /* This action forgets the key sharedpreferences except for gyms */
                SharedPrefUtil.setFirstTime(v.getContext(), true);
                SharedPrefUtil.putListToSharedPref(v.getContext(), Constants.TAKEN_MESSAGE_IDS, new ArrayList<String>());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        datasource.openDatabase();
    }

    @Override
    public void onPause() {
        datasource.closeDatabase();
        super.onPause();
    }


}
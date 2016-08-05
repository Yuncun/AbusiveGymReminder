package com.pipit.agc.fragment;

import android.os.Bundle;
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
import com.pipit.agc.data.MsgDBHelper;
import com.pipit.agc.model.DayRecord;
import com.pipit.agc.model.Message;
import com.pipit.agc.receiver.AlarmManagerBroadcastReceiver;
import com.pipit.agc.util.ReminderOracle;
import com.pipit.agc.util.SharedPrefUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Eric on 1/23/2016.
 */
public class TestDBFragmentMessages extends ListFragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "TestDBFragmentMessages";

    private MsgAndDayRecords datasource;
    private TextView currentTime;
    private TextView resetTime;
    private Button _simulateMidnight;
    private Button _testReceiverAddButton;
    private Button _deleteButton;
    private Button _addDay;
    private Button _startGymVisit;

    private Button _upgradeDbButton;

    public static TestDBFragmentMessages newInstance(int sectionNumber) {
        TestDBFragmentMessages fragment = new TestDBFragmentMessages();
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
        datasource.openDatabase();
        List<Message> values = datasource.getAllMessages();
        ArrayAdapter<Message> adapter = new ArrayAdapter<Message>(getActivity(),
                android.R.layout.simple_list_item_1, values);
        setButtons(contentView);
        setListAdapter(adapter);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        currentTime = (TextView) contentView.findViewById(R.id.currentTime);
        currentTime.setText("Messages DB");

        return contentView;
    }


    public void setButtons(View view) {
        _simulateMidnight = (Button) view.findViewById(R.id.add);
        _simulateMidnight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "doDayLogging from test button");
                AlarmManagerBroadcastReceiver.doDayLogging(getContext());
            }
        });
        _deleteButton = (Button) view.findViewById(R.id.delete);
        _deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayAdapter<Message> adapter = (ArrayAdapter<Message>) getListAdapter();
                if (adapter.getCount() > 0) {
                    Message message = (Message) adapter.getItem(0);
                    datasource.deleteMessage(message);
                    adapter.remove(message);
                }
                adapter.notifyDataSetChanged();

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
                if (datasource==null){
                    MsgAndDayRecords.initializeInstance(new MsgDBHelper(getActivity()));
                    datasource = MsgAndDayRecords.getInstance();
                }
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

        final String gymVisitStart = "Start";
        final String gymVisitEnd = "End";
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
    }

    @Override
    public void onResume() {
        super.onResume();
        datasource.openDatabase();
    }

    @Override
    public void onPause() {
        MsgAndDayRecords.getInstance().closeDatabase();
        super.onPause();
    }


}
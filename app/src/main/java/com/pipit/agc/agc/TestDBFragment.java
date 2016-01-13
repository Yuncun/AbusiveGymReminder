package com.pipit.agc.agc;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.pipit.agc.agc.data.DayRecord;
import com.pipit.agc.agc.data.DayRecordsSource;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class TestDBFragment extends ListFragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "TestDBFragment";

    private DayRecordsSource datasource;
    private TextView currentTime;
    private TextView resetTime;
    private Button _addButton;
    private Button _deleteButton;

    public static TestDBFragment newInstance(int sectionNumber) {
        TestDBFragment fragment = new TestDBFragment();
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
        datasource = DayRecordsSource.getInstance();
        datasource.openDatabase();
        List<DayRecord> values = datasource.getAllDayRecords();
        ArrayAdapter<DayRecord> adapter = new ArrayAdapter<DayRecord>(getActivity(),
                android.R.layout.simple_list_item_1, values);
        setButtons(contentView);
        setListAdapter(adapter);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        currentTime = (TextView) contentView.findViewById(R.id.currentTime);
        currentTime.setText("Current Time " + calendar.getTime().toString());

        calendar.set(Calendar.HOUR_OF_DAY, Constants.DAY_RESET_HOUR);
        calendar.set(Calendar.MINUTE, Constants.DAY_RESET_MINUTE);
        resetTime = (TextView) contentView.findViewById(R.id.dayResetTime);
        resetTime.setText("Reset Time " + calendar.getTime().toString());

        return contentView;
    }


    public void setButtons(View view) {
        _addButton = (Button) view.findViewById(R.id.add);
        _addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayAdapter<DayRecord> adapter = (ArrayAdapter<DayRecord>) getListAdapter();
                String[] comments = new String[]{"Red", "Green", "Blue"};
                String mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                int nextInt = new Random().nextInt(3);
                DayRecord dayRecord = datasource.createDayRecord(comments[nextInt] + mLastUpdateTime);
                adapter.add(dayRecord);
                adapter.notifyDataSetChanged();
            }
        });
        _deleteButton = (Button) view.findViewById(R.id.delete);
        _deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayAdapter<DayRecord> adapter = (ArrayAdapter<DayRecord>) getListAdapter();
                if (adapter.getCount() > 0) {
                    DayRecord dayRecord = (DayRecord) adapter.getItem(0);
                    datasource.deleteDayRecord(dayRecord);
                    adapter.remove(dayRecord);
                }
                adapter.notifyDataSetChanged();

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
        DayRecordsSource.getInstance().closeDatabase();
        super.onPause();
    }


}

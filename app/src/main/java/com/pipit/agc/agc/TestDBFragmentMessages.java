package com.pipit.agc.agc;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.data.Message;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by Eric on 1/23/2016.
 */
public class TestDBFragmentMessages extends ListFragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "TestDBFragmentMessages";

    private DBRecordsSource datasource;
    private TextView currentTime;
    private TextView resetTime;
    private Button _addButton;
    private Button _deleteButton;
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
        datasource = DBRecordsSource.getInstance();
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
        _addButton = (Button) view.findViewById(R.id.add);
        _addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayAdapter<Message> adapter = (ArrayAdapter<Message>) getListAdapter();
                String[] comments = new String[]{"You suck", "This is why you're fat", "Obesity kills`"};
                String mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                int nextInt = new Random().nextInt(3);
                Message message = datasource.createMessage(comments[nextInt], new Date());
                adapter.add(message);
                adapter.notifyDataSetChanged();
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

    }

    @Override
    public void onResume() {
        super.onResume();
        datasource.openDatabase();
    }

    @Override
    public void onPause() {
        DBRecordsSource.getInstance().closeDatabase();
        super.onPause();
    }


}
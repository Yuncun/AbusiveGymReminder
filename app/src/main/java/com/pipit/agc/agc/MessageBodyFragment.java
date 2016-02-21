package com.pipit.agc.agc;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.pipit.agc.agc.data.Message;
import com.pipit.agc.agc.data.DBRecordsSource;

/**
 * Displays message
 */

public class MessageBodyFragment extends Fragment {
    public final static String TAG = "MessageBodyFragment";
    private static final String ARG_PARAM1 = "id";
    private DBRecordsSource datasource;
    private Message _msg;

    private String _id;

    public MessageBodyFragment() {
        // Required empty public constructor
    }

    public static MessageBodyFragment newInstance(String param1) {
        MessageBodyFragment fragment = new MessageBodyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            _id = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_message_body, container, false);
        TextView header = (TextView) rootView.findViewById(R.id.header);
        TextView body = (TextView) rootView.findViewById(R.id.body);

        if (_id==null){
            return rootView;
        }
        datasource = DBRecordsSource.getInstance();
        datasource.openDatabase();
        _msg = datasource.getMessageById(_id);
        if (_msg==null){
            //Log.e(TAG, "Message ID " + _id + " was not found in database");
            header.setText("null message");
        }
        header.setText(_msg.getHeader());
        header.setText(_msg.getBody());
        return rootView;
    }


}

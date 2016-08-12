package com.pipit.agc.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pipit.agc.R;
import com.pipit.agc.model.Message;
import com.pipit.agc.data.MsgAndDayRecords;

/**
 * Displays message
 * TODO: Needs a bit of work to look nicer.
 */

public class MessageBodyFragment extends Fragment {
    public final static String TAG = "MessageBodyFragment";
    private static final String ARG_PARAM1 = "id";
    private MsgAndDayRecords datasource;
    private Message _msg;

    private static final int SWIPE_MIN_DISTANCE = 5;
    private static final int SWIPE_THRESHOLD_VELOCITY = 300;

    private GestureDetector mGestureDetector;
    private int mActiveFeature = 0;

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
        RelativeLayout background = (RelativeLayout) rootView.findViewById(R.id.msgbackground);
        LinearLayout background_container = (LinearLayout) rootView.findViewById(R.id.message_layout);
        TextView header = (TextView) rootView.findViewById(R.id.header);
        TextView body = (TextView) rootView.findViewById(R.id.body);
        TextView date = (TextView) rootView.findViewById(R.id.date);
        TextView reason = (TextView) rootView.findViewById(R.id.reason);
        LinearLayout drawer = (LinearLayout) rootView.findViewById(R.id.bottomdrawer);
        final ScrollView scrolly = (ScrollView) rootView.findViewById(R.id.scrolly);

        //Set Heights
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int statusbarheight = 25; //TODO: Get this dynamically
        int messageHeight = metrics.heightPixels-statusbarheight;
        LinearLayout.LayoutParams rel_btn = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, messageHeight);
        background_container.setLayoutParams(rel_btn);
        final int drawerHeight = messageHeight/6;
        LinearLayout.LayoutParams drawer_h = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, drawerHeight);
        drawer.setLayoutParams(drawer_h);

        scrolly.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //If the user swipes
                if (mGestureDetector != null && mGestureDetector.onTouchEvent(event)) {
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    int scrollY = scrolly.getScrollY();
                    int featureHeight = v.getMeasuredHeight();
                    //mActiveFeature = ((scrollY + (featureHeight / 2)) / featureHeight);
                    //int scrollTo = mActiveFeature * featureHeight;
                    int scrollTo = 0;
                    if (scrollY > drawerHeight / 2){
                        scrollTo = featureHeight;
                    }
                    else if (scrollTo < drawerHeight / 2){
                       scrollTo = 0;
                    }
                    scrolly.smoothScrollTo(0, scrollTo);

                    return true;
                }
                else if (event.getAction() == MotionEvent.ACTION_DOWN){
                    int scrollY = scrolly.getScrollY();
                    return true;
                }
                else
                {
                    return false;
                }
            }
        });

        if (_id==null){
            return rootView;
        }
        datasource = MsgAndDayRecords.getInstance();
        datasource.openDatabase();
        _msg = datasource.getMessageById(_id);
        if (_msg==null){
            //Log.e(TAG, "Message ID " + _id + " was not found in database");
            header.setText("null message");
        }
        header.setText(_msg.getHeader());
        header.setTextSize(48);
        body.setText(_msg.getBody());
        body.setTextSize(36);
        date.setText(_msg.getIntelligentDateString());
        header.setTextColor(ContextCompat.getColor(getContext(), R.color.schemeone_mediumblue));
        background_container.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.basewhite));

        if (_msg.getReason()== Message.HIT_YESTERDAY) {
            reason.setText(getContext().getResources().getString(R.string.reason_hit_gym_yesterday));
        }
        if (_msg.getReason()== Message.MISSED_YESTERDAY) {
            reason.setText(getContext().getResources().getString(R.string.reason_missed_gym));
        }
        if (_msg.getReason()== Message.HIT_TODAY) {
            reason.setText(getContext().getResources().getString(R.string.reason_hit_gym));
        }
        if (_msg.getReason()== Message.WELCOME){
            reason.setText("Welcome");
        }
        else{
            reason.setText(getContext().getText(R.string.new_msg));
        }

        drawer.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.schemefour_teal));
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        /*Mark as read*/
        if (!_msg.getRead()){
            MsgAndDayRecords datasource = MsgAndDayRecords.getInstance();
            datasource.openDatabase();
            datasource.markMessageRead(_msg.getId(), true);
            datasource.closeDatabase();
            _msg.setRead(true);
        }
        return rootView;
    }


}

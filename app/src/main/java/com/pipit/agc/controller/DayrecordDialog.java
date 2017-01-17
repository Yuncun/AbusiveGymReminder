package com.pipit.agc.controller;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pipit.agc.R;
import com.pipit.agc.adapter.VisitsListAdapter;
import com.pipit.agc.data.MsgAndDayRecords;
import com.pipit.agc.model.DayRecord;
import com.pipit.agc.util.Constants;
import com.pipit.agc.util.SharedPrefUtil;
import com.pipit.agc.util.StatsContent;
import com.pipit.agc.util.Util;

/**
 * Handles creation and showing of the dayrecord dialog (when you click a day).
 * Created by Eric on 7/24/2016.
 */
public class DayrecordDialog implements View.OnClickListener, VisitsListAdapter.TimePickerCallback {
    private final String TAG = "DayRecordDialog";
    private DayRecord today;
    private Context context;
    private DayrecordObserver observer;
    private MaterialDialog dialog;

    public interface DayrecordObserver{
        void update();
    }

    public DayrecordDialog(DayRecord d, Context context) {
        today = d;
        this.context = context;
    }

    public void setObserver(DayrecordObserver o){
        observer = o;
    }

    @Override
    public void onClick(View v) {
        if (v != null) v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

        dialog = new MaterialDialog.Builder(context)
                .title(today.getDateString(Constants.DATE_FORMAT_TWO))
                .customView(R.layout.day_dialog_layout, true)
                .show();

        View dv = dialog.getCustomView();
        formatTimePickersInit(dv);
        formatWasGymDayText(dv);
        formatWentToGymText(dv);
        formatTimeSpentList(dv);
        formatToggleButton(dv);
    }

    /**
     * This button lets the user end a current visit
     * @param dv
     */
    private void formatToggleButton(View dv){
        final Button button = (Button) dv.findViewById(R.id.endvisit);
        if (!today.isCurrentlyVisiting()){
            button.setVisibility(View.GONE);
        }else{
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        MsgAndDayRecords datasource;
                        datasource = MsgAndDayRecords.getInstance();
                        datasource.openDatabase();
                        today.endCurrentVisit();
                        datasource.updateDayRecordVisitsById(today.getId(), today.getSerializedVisitsList());
                        datasource.closeDatabase();
                        Toast.makeText(context, "Updated visits", Toast.LENGTH_SHORT).show();
                        button.setVisibility(View.GONE);
                    }catch(Exception e){
                        Toast.makeText(context, "Failed to update date", Toast.LENGTH_SHORT).show();
                        SharedPrefUtil.updateMainLog(context, "Failed to update visits of date - Id " + today.getId());
                    }
                }
            });
        }
    }

    /**
     * Formats the textviews related to "Was gym day?".
     * @param dv - Alert dialog view
     */
    private void formatWasGymDayText(View dv){
        if (dv==null) return;
        TextView wasgymday = (TextView) dv.findViewById(R.id.was_gym_day);
        wasgymday.setText("Gym Day");
        TextView wasgymdayresult = (TextView) dv.findViewById(R.id.was_gym_day_result);
        wasgymdayresult.setText(today.isGymDay() ? "Yes" : "No");
        wasgymdayresult.setTextColor(today.isGymDay() ?
                Util.getStyledColor(context, R.attr.explicitHitColor) :
                Util.getStyledColor(context, R.attr.missColor));
        LinearLayout wasgymdaylayout = (LinearLayout) dv.findViewById(R.id.was_gym_day_layout);
        wasgymdaylayout.setOnClickListener(toggleWasGymDayListener);
    }

    /**
     * Formats textviews related to "Went to Gym"? in the Dialog
     * @param dv Alert dialog view
     */
    private void formatWentToGymText(View dv){
        TextView wenttogym = (TextView) dv.findViewById(R.id.went_to_gym);
        wenttogym.setText("Went to Gym");
        TextView wenttogymresult = (TextView) dv.findViewById(R.id.went_to_gym_result);
        wenttogymresult.setText(today.beenToGym() ? "Yes" : "No");
        wenttogymresult.setTextColor(today.beenToGym() ?
                Util.getStyledColor(context, R.attr.explicitHitColor) :
                Util.getStyledColor(context, R.attr.missColor));
        LinearLayout wenttogymlayout = (LinearLayout) dv.findViewById(R.id.went_to_gym_layout);
        wenttogymlayout.setOnClickListener(toggleWentToGymListener);
    }

    /**
     * Creates the "time spent" section.
     * Contains an editable list of visits.
     * @param dv
     */
    private void formatTimeSpentList(View dv){
        TextView timespent = (TextView) dv.findViewById(R.id.timespent);
        timespent.setText("Total Time");
        TextView timespentresult = (TextView) dv.findViewById(R.id.timespent_result);
        final int totalmins = today.calculateTotalVisitTime();
        timespentresult.setText(Integer.toString(totalmins) + " min");
        timespentresult.setTextColor(totalmins > 0 ?
                Util.getStyledColor(context, R.attr.explicitHitColor) :
                Util.getStyledColor(context, R.attr.missColor));

        TextView visits = (TextView) dv.findViewById(R.id.visit_instances);
        visits.setText("Visits");
        RecyclerView visitslist = (RecyclerView) dv.findViewById(R.id.timespent_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        visitslist.setLayoutManager(mLayoutManager);
        visitslist.setAdapter(new VisitsListAdapter(today.getVisits(), this));
    }

    /**
     * The intial function for formatting the timepicker view
     * @param dv
     */
    private void formatTimePickersInit(View dv){
        //Start hidden, show when needed
        dv.findViewById(R.id.timesection).setVisibility(View.GONE);

        TextView backbutton =  (TextView) dv.findViewById(R.id.savebutton);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Back", Toast.LENGTH_SHORT).show();
                showDefault();
            }
        });
    }

    private View.OnClickListener toggleWasGymDayListener  = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try{
                today.setIsGymDay(!today.isGymDay());
                StatsContent.getInstance().updateDayRecord(today);
                Toast.makeText(context, "Updated History", Toast.LENGTH_SHORT).show();
                StatsContent.getInstance().refreshDayRecords();
                formatWasGymDayText(dialog.getCustomView());
                //v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                if (observer!=null) observer.update();
            }catch (Exception e){
                Toast.makeText(context, "Error: Unable to update dayrecord", Toast.LENGTH_SHORT).show();
                Log.e("DayRecord", "Failed updatedayrecord " + e.toString());
            }
        }
    };

    private View.OnClickListener toggleWentToGymListener  = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try{
                today.setHasBeenToGym(!today.beenToGym());
                StatsContent.getInstance().updateDayRecord(today);
                Toast.makeText(context, "Updated History", Toast.LENGTH_SHORT).show();
                StatsContent.getInstance().refreshDayRecords();
                formatWentToGymText(dialog.getCustomView());
                //v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                if (observer!=null) observer.update();
            }catch (Exception e){
                Toast.makeText(context, "Error: Unable to update dayrecord", Toast.LENGTH_SHORT).show();
                Log.e("DayRecord", "Failed updatedayrecord " + e.toString());
            }
        }
    };



    /**
     * TIME PICKER CODE
     *
     * This is invoked when we start to edit a visit.
     * It will transact the layout to show the timepicker layout that is hidden by default
     */
    @Override
    public void showTimePickers(final DayRecord.Visit visit){
        View rootview = dialog.getCustomView();
        View timesection = rootview.findViewById(R.id.timesection);
        TextView oldtimetxt = (TextView) rootview.findViewById(R.id.oldtimetxt);
        final TextView timetxt = (TextView) rootview.findViewById(R.id.newtimetext);
        TabLayout tabs = (TabLayout) rootview.findViewById(R.id.tptabs);

        //Save an "oldvisit" that keeps the old visit.
        final DayRecord.Visit oldvisit = new DayRecord.Visit();
        oldvisit.in = visit.in; //Note: DateTimes are immutable, so clone is not necessary
        oldvisit.out =  visit.out;

        //Hide the main dialog
        rootview.findViewById(R.id.sectionone).setVisibility(View.GONE);
        //Show the timepickers
        timesection.setVisibility(View.VISIBLE);
        //Set the undo button behavior.
        TextView undo =  (TextView) rootview.findViewById(R.id.undobutton);
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "uNdo", Toast.LENGTH_SHORT).show();
                visit.in = oldvisit.in;
                visit.out = oldvisit.out;
                timetxt.setText("New Visit: " + visit.print());
            }
        });

        timetxt.setText("New Visit: " + visit.print());
        oldtimetxt.setText("Old Visit: " + oldvisit.print());

        final TimePicker fromtp = (TimePicker) timesection.findViewById(R.id.fromTime);
        final TimePicker totp = (TimePicker) timesection.findViewById(R.id.toTime);
        final LinearLayout fromtp_layout = (LinearLayout) timesection.findViewById(R.id.tp_one);
        final LinearLayout totp_layout = (LinearLayout) timesection.findViewById(R.id.tp_two);
        fromtp_layout.setVisibility(View.VISIBLE); //"From" time is initially set as visible

        fromtp.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                visit.in.hourOfDay().setCopy(hourOfDay);
                visit.in.minuteOfHour().setCopy(minute);
                timetxt.setText("New Visit: " + visit.print());
            }
        });

        totp.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                visit.out.hourOfDay().setCopy(hourOfDay);
                visit.out.minuteOfHour().setCopy(minute);
                timetxt.setText("New Visit: " + visit.print());
            }
        });

        tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition()==0){
                    //Time start tab
                    fromtp_layout.setVisibility(View.VISIBLE);
                }else
                if (tab.getPosition()==1){
                    //Time end
                    totp_layout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition()==0){
                    //Time start tab
                    fromtp_layout.setVisibility(View.GONE);
                }else
                if (tab.getPosition()==1){
                    //Time end
                    totp_layout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    /**
     * Go back to main screen from the timepicker menu
     */
    @Override
    public void showDefault(){
        View rootview = dialog.getCustomView();
        rootview.findViewById(R.id.sectionone).setVisibility(View.VISIBLE);
        rootview.findViewById(R.id.timesection).setVisibility(View.GONE);
    }



}


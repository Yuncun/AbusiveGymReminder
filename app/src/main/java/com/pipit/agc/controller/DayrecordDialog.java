package com.pipit.agc.controller;

import android.content.Context;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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

import org.joda.time.LocalDateTime;

/**
 * Handles creation and showing of the dayrecord dialog (when you click a day).
 * Also handles visit editing. The visit edit view is just a view that is hidden under the
 * normal view. We toggle visibility to change between views.
 *
 * Created by Eric on 7/24/2016.
 */
public class DayrecordDialog implements View.OnClickListener, VisitsListAdapter.TimePickerCallback, VisitsListAdapter.DayDialogCallback {
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

        ImageView imv = (ImageView) dv.findViewById(R.id.daydialog_overflow);
        imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v);
            }
        });


        TextView visits = (TextView) dv.findViewById(R.id.visit_instances);
        visits.setText("Visits");
        RecyclerView visitslist = (RecyclerView) dv.findViewById(R.id.timespent_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        visitslist.setLayoutManager(mLayoutManager);
        VisitsListAdapter adapter = new VisitsListAdapter(today, this);
        adapter.setDayDialogCallBack(this);
        visitslist.setAdapter(adapter);
    }

    /**
     * The intial function for formatting the timepicker view
     * @param dv
     */
    private void formatTimePickersInit(View dv){
        //Start hidden, show when needed+
        dv.findViewById(R.id.timesection).setVisibility(View.GONE);
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
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
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
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
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
     * This also contains all the logic to handle saving and cancelling
     *
     * @param visit - Reference to the visit to edit
     * @param position - Position of visit relative to DayRecord's list
     *                 This is used to identify which visit we need to delete from list
     * @param thisIsNewRecord - false if we are editing an existing record. True if this is a new visit.
     */
    public void showTimePickers(final DayRecord.Visit visit, final int position, final boolean thisIsNewRecord) {
        View rootview = dialog.getCustomView();
        View timesection = rootview.findViewById(R.id.timesection);
        TextView oldtimetxt = (TextView) rootview.findViewById(R.id.oldtimetxt);
        final TextView newtime_valone = (TextView) rootview.findViewById(R.id.newtimetxt_valone);
        final TextView newtime_valtwo = (TextView) rootview.findViewById(R.id.newtimetxt_valtwo);
        final TextView timetxt = (TextView) rootview.findViewById(R.id.newtimetext);
        final TextView tp_timespent = (TextView) rootview.findViewById(R.id.totaltimespent);
        final TabLayout tabs = (TabLayout) rootview.findViewById(R.id.tptabs);

        //Save an "oldvisit" that keeps the old visit.
        final DayRecord.Visit oldvisit = new DayRecord.Visit();
        oldvisit.in = visit.in; //Note: DateTimes are immutable, so clone is not necessary
        oldvisit.out = visit.out;

        //Hide the main dialog, show the new dialog
        rootview.findViewById(R.id.sectionone).setVisibility(View.GONE);
        //Show the timepickers
        timesection.setVisibility(View.VISIBLE);

        timetxt.setText("New Visit: ");
        newtime_valone.setText(visit.printin());
        newtime_valtwo.setText(visit.printout());
        if (thisIsNewRecord){
            oldtimetxt.setText("Old Visit: N/A");
        }else {
            oldtimetxt.setText("Old Visit: " + oldvisit.print());
        }
        tp_timespent.setText(visit.getVisitTimeMinutes() + " min");
        tp_timespent.setTextColor(Util.getStyledColor(context, R.attr.explicitHitColor));

        final TimePicker tp_in = (TimePicker) timesection.findViewById(R.id.fromTime);
        final TimePicker tp_out = (TimePicker) timesection.findViewById(R.id.toTime);
        setTp(tp_in, visit.in);
        setTp(tp_out, visit.out);

        final LinearLayout fromtp_layout = (LinearLayout) timesection.findViewById(R.id.tp_one);
        final LinearLayout totp_layout = (LinearLayout) timesection.findViewById(R.id.tp_two);
        fromtp_layout.setVisibility(View.VISIBLE); //"From" time is initially set as visible
        totp_layout.setVisibility(View.GONE);

        tp_in.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                visit.in = visit.in.hourOfDay().setCopy(hourOfDay);
                visit.in = visit.in.minuteOfHour().setCopy(minute);
                newtime_valone.setText(visit.printin());
                tp_timespent.setText(visit.getVisitTimeMinutes()+" min");
                //"In" time must be earlier than "Out", so preset "Out" if necessary
                if (visit.in.compareTo(visit.out) > 0) {
                    setTp(tp_out, visit.in);
                }
            }
        });

        tp_out.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                visit.out = visit.out.hourOfDay().setCopy(hourOfDay);
                visit.out = visit.out.minuteOfHour().setCopy(minute);
                newtime_valtwo.setText(visit.printout());
                tp_timespent.setText(visit.getVisitTimeMinutes() + " min");
                if (visit.out.compareTo(visit.in) < 0) {
                    setTp(tp_in, visit.out);
                }
            }
        });

        tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    //Time start tab
                    fromtp_layout.setVisibility(View.VISIBLE);
                } else if (tab.getPosition() == 1) {
                    //Time end
                    totp_layout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    //Time start tab
                    fromtp_layout.setVisibility(View.GONE);
                } else if (tab.getPosition() == 1) {
                    //Time end
                    totp_layout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        Button cancel = (Button) rootview.findViewById(R.id.cancelbutton);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visit.in = oldvisit.in;
                visit.out = oldvisit.out;
                TabLayout.Tab tab = tabs.getTabAt(0); //Next time, show up at the "IN" timepicker
                tab.select();
                showDefault();
            }
        });

        Button save = (Button) rootview.findViewById(R.id.savebutton);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (visit.in.compareTo(visit.out)>=0){
                    //"in" must be before "out"
                    Toast.makeText(context, "Saved! " + visit.print(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!thisIsNewRecord && today.getVisits().size()>position){
                    today.removeVisit(position);
                }
                today.addVisitAndMergeSubsets(visit);
                Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show();
                formatTimeSpentList(dialog.getCustomView());
                final int totalmins = today.calculateTotalVisitTime();

                //Update "timespent" calculation
                TextView timespentresult = (TextView) dialog.getCustomView().findViewById(R.id.timespent_result);
                timespentresult.setText(Integer.toString(totalmins) + " min");
                redrawTimeSpent();

                //Update "went to gym", if we just made a new record
                if (!today.beenToGym()){
                    today.setHasBeenToGym(true);
                    StatsContent.getInstance().updateDayRecord(today);
                    formatWentToGymText(dialog.getCustomView());
                }

                //Update database
                MsgAndDayRecords datasource;
                datasource = MsgAndDayRecords.getInstance();
                datasource.openDatabase();
                datasource.updateDayRecordVisitsById(today.getId(), today.getSerializedVisitsList());
                datasource.closeDatabase();

                TabLayout.Tab tab = tabs.getTabAt(0);
                tab.select();
                //Todo:Before updating, add an argument to update so we can go back to where we were in weekview
                //observer.update();
                showDefault();
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

    private void setTp(TimePicker tp, LocalDateTime dt){
        if (Build.VERSION.SDK_INT >= 23){
            tp.setHour(dt.getHourOfDay());
            tp.setMinute(dt.getMinuteOfHour());
        }
        else {
            tp.setCurrentHour(dt.getHourOfDay());
            tp.setCurrentMinute(dt.getMinuteOfHour());
        }
    }

    /**
     * Callback used when visits are updated, so we can update timespent text
     */
    public void redrawTimeSpent(){
        TextView timespentresult = (TextView) dialog.getCustomView().findViewById(R.id.timespent_result);
        final int totalmins = today.calculateTotalVisitTime();
        timespentresult.setText(Integer.toString(totalmins) + " min");
        timespentresult.setTextColor(totalmins > 0 ?
                Util.getStyledColor(context, R.attr.explicitHitColor) :
                Util.getStyledColor(context, R.attr.missColor));
    }

    /**
     * Callback to toggle "been to gym" text.
     * This is useful when we remove all the visits by hand.
     * @param tf
     */
    public void setGymVisited(boolean tf){
        today.setHasBeenToGym(tf);
        StatsContent.getInstance().updateDayRecord(today);
        formatWentToGymText(dialog.getCustomView());
    }

    public void showPopup(View v) {
        final Context context = v.getContext();
        PopupMenu popup = new PopupMenu(context, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.daydialog_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                DayRecord.Visit visit = new DayRecord.Visit();
                visit.in = new LocalDateTime();
                visit.out = new LocalDateTime();
                showTimePickers(visit, today.getVisits().size(), true);
                return false;
            }
        });
        popup.show();
    }

}


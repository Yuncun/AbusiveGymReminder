package com.pipit.agc.controller;

import android.content.Context;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pipit.agc.R;
import com.pipit.agc.model.DayRecord;
import com.pipit.agc.util.Constants;
import com.pipit.agc.util.StatsContent;
import com.pipit.agc.util.Util;

/**
 * Created by Eric on 7/24/2016.
 */
public class DayrecordClickListener implements View.OnClickListener {
    private DayRecord today;
    private Context context;
    private DayrecordObserver observer;
    private MaterialDialog dialog;


    public interface DayrecordObserver{
        void update();
    }

    public DayrecordClickListener(DayRecord d, Context context) {
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
        formatWasGymDayText(dv);
        formatWentToGymText(dv);
        formatTimesListText(dv);
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
     * Format textviews related to "Time Spent" in the Dialog
     * Currently displays just one string containing all the times visited
     * Todo: In the future, consider making the list of times editable
     * @param dv Alert dialog view
     */
    private void formatTimesListText(View dv){
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
        TextView visitsresult = (TextView) dv.findViewById(R.id.visit_instances_result);
        visitsresult.setText(today.printVisits());
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

}


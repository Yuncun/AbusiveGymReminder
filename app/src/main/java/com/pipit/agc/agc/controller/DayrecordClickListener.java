package com.pipit.agc.agc.controller;

import android.content.Context;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pipit.agc.agc.R;
import com.pipit.agc.agc.model.DayRecord;
import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.util.StatsContent;
import com.pipit.agc.agc.util.Util;

/**
 * Created by Eric on 7/24/2016.
 */
public class DayrecordClickListener implements View.OnClickListener {
    DayRecord today;
    Context context;

    public DayrecordClickListener(DayRecord d, Context context) {
        today = d;
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        if (v != null) v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(today.getDateString(Constants.DATE_FORMAT_TWO))
                .customView(R.layout.day_dialog_layout, true)
                .show();

        View dv = dialog.getCustomView();
        TextView wasgymday = (TextView) dv.findViewById(R.id.was_gym_day);
        wasgymday.setText("Gym Day");
        TextView wasgymdayresult = (TextView) dv.findViewById(R.id.was_gym_day_result);
        wasgymdayresult.setText(today.isGymDay() ? "Yes" : "No");
        wasgymdayresult.setTextColor(today.isGymDay() ?
                Util.getStyledColor(context, R.attr.explicitHitColor) :
                Util.getStyledColor(context, R.attr.missColor));
        wasgymdayresult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    today.setIsGymDay(!today.isGymDay());
                    StatsContent.getInstance().updateDayRecord(today);
                    Toast.makeText(context, "Updated statsrecord", Toast.LENGTH_SHORT).show();
                    //Todo: Call observer to update
                    StatsContent.getInstance().refreshDayRecords();
                } catch (Exception e) {
                    Toast.makeText(context, "Error: Unable to update dayrecord", Toast.LENGTH_SHORT).show();
                    Log.e("DayRecord", "Failed updatedayrecord " + e.toString());
                }
            }
        });

        //Went to Gym?
        TextView wenttogym = (TextView) dv.findViewById(R.id.went_to_gym);
        wenttogym.setText("Went to Gym");
        TextView wenttogymresult = (TextView) dv.findViewById(R.id.went_to_gym_result);
        wenttogymresult.setText(today.beenToGym() ? "Yes" : "No");
        wenttogymresult.setTextColor(today.beenToGym() ?
                Util.getStyledColor(context, R.attr.explicitHitColor) :
                Util.getStyledColor(context, R.attr.missColor));
        wenttogymresult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    today.setHasBeenToGym(!today.beenToGym());
                    StatsContent.getInstance().updateDayRecord(today);
                    Toast.makeText(context, "Updated statsrecord", Toast.LENGTH_SHORT).show();
                    StatsContent.getInstance().refreshDayRecords();
                    //Todo: Call observer to update
                }catch (Exception e){
                    Toast.makeText(context, "Error: Unable to update dayrecord", Toast.LENGTH_SHORT).show();
                    Log.e("DayRecord", "Failed updatedayrecord " + e.toString());
                }
            }
        });

        //Time Spent
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
}


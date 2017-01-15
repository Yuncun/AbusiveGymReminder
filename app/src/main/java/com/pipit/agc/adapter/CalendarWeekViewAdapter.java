package com.pipit.agc.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.pipit.agc.R;
import com.pipit.agc.controller.DayrecordDialog;
import com.pipit.agc.model.DayRecord;
import com.pipit.agc.util.Util;
import com.pipit.agc.views.CircleView;
import com.pipit.agc.views.WeekViewSwipeable;

import java.util.List;

/**
 * Created by Eric on 7/23/2016.
 */
class CalendarWeekViewAdapter extends WeekViewAdapter<DayRecord>{
    private DayrecordDialog.DayrecordObserver observer;

    public CalendarWeekViewAdapter(Context context, List<DayRecord> allDayRecords, WeekViewSwipeable layout){
        super(context, allDayRecords, layout);
    }

    protected void drawCircleDays(final Context context, CircleView cv, TextView rfd, View weekitem, final int index ){
        cv.setShowSubtitle(false);
        //Check if it is a day for which we have records
        if (index >= _allDayRecords.size()){

            cv.setFillColor(ContextCompat.getColor(context, R.color.grey_darker));
            return;
        }else if (index < 0){

            cv.setFillColor(ContextCompat.getColor(context, R.color.grey_darker));
            return;
        }

        rfd.setVisibility(View.VISIBLE);
        DayrecordDialog dcl = new DayrecordDialog(_allDayRecords.get(index), context);
        if (observer!=null){
            dcl.setObserver(observer);
        }
        weekitem.setOnClickListener(dcl);

        if (_allDayRecords.get(index).beenToGym()){
                /*HIT DAY*/
            rfd.setText(context.getText(R.string.hit));
            cv.setStrokeColor(Util.getStyledColor(context,
                    R.attr.explicitHitColor));
            cv.setTitleColor(ContextCompat.getColor(context, R.color.basewhite));
        }
        else{
                /* For days we haven't gone to gym, we want to say "MISS" if it was a gym day
                    and "REST" if it wasnt a rest day. */
            if (_allDayRecords.get(index).isGymDay()){
                if (index==_allDayRecords.size()-1){
                        /* CURRENT DAY STATE */
                    //The message for today; don't say "missed"
                    rfd.setText("?");
                }
                else{
                        /*MISSED A GYM DAY STATE */
                    rfd.setText(context.getText(R.string.miss));
                    cv.setStrokeColor(Util.getStyledColor(context,
                            R.attr.missColor));
                    cv.setTitleColor(ContextCompat.getColor(context, R.color.basewhite));
                }
            }else{
                    /* REST DAY STATE */
                cv.setStrokeColor(Util.getStyledColor(context,
                        R.attr.implicitHitColor));
                rfd.setText(context.getText(R.string.rest));
            }
        }
        if (index==_allDayRecords.size()-1){
            cv.setStrokeColor(ContextCompat.getColor(context, R.color.schemefour_yellow));
            rfd.setTextColor(ContextCompat.getColor(context, R.color.schemefour_yellow));
            rfd.setText("Today");
        }
    }

    public void setObserver(DayrecordDialog.DayrecordObserver d){
        observer = d;
    }
}
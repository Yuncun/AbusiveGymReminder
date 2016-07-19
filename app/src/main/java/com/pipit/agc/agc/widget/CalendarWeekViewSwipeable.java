package com.pipit.agc.agc.widget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pipit.agc.agc.R;
import com.pipit.agc.agc.adapter.MySparkAdapter;
import com.pipit.agc.agc.adapter.WeekViewAdapter;
import com.pipit.agc.agc.data.MsgAndDayRecords;
import com.pipit.agc.agc.model.DayRecord;

import java.util.List;

/**
 * Created by Eric on 7/9/2016.
 */
public class CalendarWeekViewSwipeable extends WeekViewSwipeable {

    private MySparkAdapter sparkAdapter;

    public CalendarWeekViewSwipeable(Context context, AttributeSet attrs) {
        super(context, attrs);
        MsgAndDayRecords datasource = MsgAndDayRecords.getInstance();
        datasource.openDatabase();
        List<DayRecord> _allPreviousDays =  datasource.getAllDayRecords();
        MsgAndDayRecords.getInstance().closeDatabase();
        setAdapter(new MyWeekViewAdapter(context, _allPreviousDays, this));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                List<DayRecord> daysForWeek = ((WeekViewAdapter) viewPager.getAdapter()).getDaysForFocusedWeek(position);
                updateSparkLineData(daysForWeek);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }

    class MyWeekViewAdapter extends WeekViewAdapter<DayRecord>{
        public MyWeekViewAdapter(Context context, List<DayRecord> allDayRecords, WeekViewSwipeable layout){
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
            weekitem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Get latset since this isn't commonly called
                    MsgAndDayRecords datasource;
                    datasource = MsgAndDayRecords.getInstance();
                    datasource.openDatabase();
                    DayRecord today = datasource.getLastDayRecord();
                    datasource.closeDatabase();

                    MaterialDialog dialog = new MaterialDialog.Builder(context)
                            .title(today.getDateString())
                            .content("Visits: \n" + _allDayRecords.get(index).printVisits())
                            .show();
                }
            });

            if (_allDayRecords.get(index).beenToGym()){
                /*HIT DAY*/
                rfd.setText(context.getText(R.string.hit));
                cv.setStrokeColor(ContextCompat.getColor(context, R.color.schemethree_teal));
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
                        cv.setStrokeColor(ContextCompat.getColor(context, R.color.schemethree_red));
                        cv.setTitleColor(ContextCompat.getColor(context, R.color.basewhite));
                    }
                }else{
                    /* REST DAY STATE */
                    rfd.setText(context.getText(R.string.rest));
                }
            }
            if (index==_allDayRecords.size()-1){
                cv.setStrokeColor(ContextCompat.getColor(context, R.color.schemefour_yellow));
                rfd.setTextColor(ContextCompat.getColor(context, R.color.schemefour_yellow));
                rfd.setText("Today");
            }
        }
    }

    public void attachSparklineAdapter(MySparkAdapter sparky){
        sparkAdapter = sparky;
    }

    public void unattachSparklineAdapter(){
        sparkAdapter=null;
    }

    public void updateSparkLineData(List<DayRecord> days){
        if (sparkAdapter==null) return;

        float[] times = new float[days.size()];
        for (int i = 0 ; i < days.size(); i++){
            if (days.get(i) == null || days.get(i).calculateTotalVisitTime() <= 0){
                times[i] = 0;
            }
            else{
                times[i] = (float) days.get(i).calculateTotalVisitTime();
            }
        }

        if (times.length < 2 && times.length > 0){
            times = new float[] {times[0], times[0]};
            sparkAdapter.setGraphWidth(18.0f); //One data point should not appear too large
        } else {
            sparkAdapter.setGraphWidth(6.0f);
        }
        sparkAdapter.update(times);
    }




}

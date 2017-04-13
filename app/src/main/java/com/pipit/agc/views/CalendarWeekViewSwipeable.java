package com.pipit.agc.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import com.pipit.agc.adapter.MySparkAdapter;
import com.pipit.agc.adapter.WeekViewAdapter;
import com.pipit.agc.model.DayRecord;

import java.util.List;

/**
 * Implementation of a WeekViewSwipeable
 *
 * Created by Eric on 7/9/2016.
 */
public class CalendarWeekViewSwipeable extends WeekViewSwipeable {

    private MySparkAdapter sparkAdapter;

    public CalendarWeekViewSwipeable(Context context, AttributeSet attrs) {
        super(context, attrs);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (viewPager!=null && viewPager.getAdapter()!=null){
                    List<DayRecord> daysForWeek = ((WeekViewAdapter) viewPager.getAdapter()).getDaysForFocusedWeek(position);
                    updateSparkLineData(daysForWeek);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

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
            if (days.get(i) == null || days.get(i).getTotalVisitsMinutes() <= 0){
                times[i] = 0;
            }
            else{
                times[i] = (float) days.get(i).getTotalVisitsMinutes();
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

package com.pipit.agc.agc.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pipit.agc.agc.R;
import com.pipit.agc.agc.adapter.WeekViewAdapter;
import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.model.DayRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eric on 3/12/2016.
 */
public class WeekViewSwipeable extends LinearLayout {

    public AgcViewPager viewPager;

    public WeekViewSwipeable(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.WeekCalendarView, 0, 0);

        a.recycle();

        setOrientation(LinearLayout.HORIZONTAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = inflater.inflate(R.layout.week_calendar_vp, this, true);

        DBRecordsSource datasource = DBRecordsSource.getInstance();
        datasource.openDatabase();
        List<DayRecord> _allPreviousDays = datasource.getAllDayRecords();
        DBRecordsSource.getInstance().closeDatabase();

        viewPager = (AgcViewPager) root.findViewById(R.id.weekvp);
        viewPager.setAdapter(new WeekViewAdapter(context, _allPreviousDays));
        viewPager.setCurrentItem(WeekViewAdapter.getNumberOfWeeks(_allPreviousDays)-1);

        //viewPager.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.max(height, 1)));

    }

    public WeekViewSwipeable(Context context) {
        this(context, null);
    }

    /**
     * Set the last day of week
     * @param offset - Day of week int (Sunday == 1, Friday == 6)
     */
    public void setDayOfWeekEnd(int offset){
        Resources r = getResources();
        String name = this.getContext().getPackageName();
        for (int i = 1; i < 8; i++){
            int viewid = r.getIdentifier("day_" + i, "id", name);
            View weekitem = findViewById(viewid);
            TextView tv = (TextView) weekitem.findViewById(R.id.calendar_day_name);
            tv.setText(getDayOfWeekText((i + offset) % 7));
        }
    }

    /**
     * We leave it the responsibility of the user to give strings not exceeding
     * the size of the circle.
     * @param txt
     */
    public void setCalendarInfo(List<String> txt){
        Resources r = getResources();
        String name = this.getContext().getPackageName();
        for (int i= 1 ; i < 8 ; i++){
            int viewid = r.getIdentifier("day_" + i, "id", name);
            View weekitem = findViewById(viewid);
            TextView tv = (TextView) weekitem.findViewById(R.id.calendar_day_info);
            tv.setText(txt.get(i - 1));
        }
    }

    /**
     * Used to populate and style the WeekCalendarView that shows attendance over last seven days
     * @return
     */
    public void styleFromDayrecordsData(Context context, List<DayRecord> _allDayRecords){
        ArrayList<String> hitlist = new ArrayList<String>();
        Resources r = context.getResources();
        String name = this.getContext().getPackageName();
        int k = _allDayRecords.size()-7; //Dayrecord of seven days past;
        for (int i= 0 ; i < 7 ; i++){
            /*Get identifier*/
            int j = i+1; //I'm as confused as you are
            int viewid = r.getIdentifier("day_" + j, "id", name);
            View weekitem = findViewById(viewid);
            TextView tv = (TextView) weekitem.findViewById(R.id.calendar_day_info);
            if (k+i<0) {
                /* NO INFO STATE */
                /* HIDE VISIBILITY */
                tv.setText(r.getString(R.string.noinfo));
                weekitem.setVisibility(View.GONE);
                continue;
            }
            if (_allDayRecords.get(k+i).beenToGym()){
                /*HIT DAY*/
                tv.setText(r.getString(R.string.hit));
                tv.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.schemethree_teal), PorterDuff.Mode.SRC_ATOP);
                tv.setTextColor(ContextCompat.getColor(context, R.color.basewhite));
            }
            else{
                /* For days we haven't gone to gym, we want to say "MISS" if it was a gym day
                    and "REST" if it was a rest day. */
                if (_allDayRecords.get(k+i).isGymDay()){
                    if (i==6){
                        /* CURRENT DAY STATE */
                        //The message for today; don't say "missed"
                        tv.setText("?");
                    }
                    else{
                        /*MISSED A GYM DAY STATE */
                        tv.setText(r.getString(R.string.miss));
                        tv.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.schemethree_red), PorterDuff.Mode.SRC_ATOP);
                        tv.setTextColor(ContextCompat.getColor(context, R.color.basewhite));
                    }
                }else{
                    /* REST DAY STATE */
                    tv.setText(r.getString(R.string.rest));
                }
            }
        }
    }

    public View getDayViewFromPosition(int pos){
        if (pos<1 || pos>7){ return null; }
        Resources r = getResources();
        String name = this.getContext().getPackageName();
        int viewid = r.getIdentifier("day_" + pos, "id", name);
        View weekitem = findViewById(viewid);
        return weekitem;
    }

    public void showLastDayMarker(){
        findViewById(R.id.day_7).findViewById(R.id.record_for_day).setVisibility(VISIBLE);
    }

    public void showLastDayMarker(int color){
        findViewById(R.id.day_7).setBackgroundColor(color);
        showLastDayMarker();
    }

    private static String getDayOfWeekText(int n){
        switch(n){
            case 0:
                return "Sat";
            case 1:
                return "Sun";
            case 2:
                return "Mon";
            case 3:
                return "Tue";
            case 4:
                return "Wed";
            case 5:
                return "Thu";
            case 6:
                return "Fri";
            case 7:
                return "Sat";
            default:
                return Integer.toString(n);
        }
    }

}



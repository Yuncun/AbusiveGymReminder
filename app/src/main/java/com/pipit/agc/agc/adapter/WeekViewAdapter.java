package com.pipit.agc.agc.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pipit.agc.agc.R;
import com.pipit.agc.agc.model.DayRecord;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Eric on 5/1/2016.
 */
public class WeekViewAdapter extends PagerAdapter {

    private Context mContext;
    private List<DayRecord> _allDayRecords;

    public class DayInfo{
        public boolean future;
        public boolean present;
        public DayRecord dayrecord;
        public Calendar date;

    }

    public WeekViewAdapter(Context context) {
        mContext = context;
    }

    public WeekViewAdapter(Context context, List<DayRecord> allDayRecords){
        mContext = context;
        _allDayRecords = allDayRecords;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.week_calendar, null, true);
        styleFromDayrecordsData(mContext, _allDayRecords, position, v);
        setDayOfWeekText(0, v);
        collection.addView(v);

        return v;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return getNumberOfWeeks(_allDayRecords);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Slide " + position;
    }

    /**
     * Set the last day of week
     * @param offset - Day of week int (Sunday == 1, Friday == 6)
     */
    public void setDayOfWeekText(int offset, View root){
        Resources r = mContext.getResources();
        String name = mContext.getPackageName();
        for (int i = 1; i < 8; i++){
            int viewid = r.getIdentifier("day_" + i, "id", name);
            View weekitem = root.findViewById(viewid);
            TextView tv = (TextView) weekitem.findViewById(R.id.calendar_day_name);
            tv.setText(getDayOfWeekText((i + offset) % 7));
        }
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

    /**
     * Given a list of days, how many weeks exist between the first
     * and last days inclusive?
     */
    public static int getNumberOfWeeks(List<DayRecord> dayrecords){
        if (dayrecords == null || dayrecords.size() == 0){
            return 1;
        }
        int n = dayrecords.size() / 7;
        n++;
        int rem = dayrecords.size() % 7;

        Calendar c = Calendar.getInstance();
        if (c.get(Calendar.DAY_OF_WEEK) < rem){
            n++;
        }

        //This always gives us an "extra" week - so seven days starting on Sunday may give us two weeks
        return n;
    }

    /**
    * Used to populate and style the WeekCalendarView that shows attendance over last seven days
    * @return
    */
    public void styleFromDayrecordsData(Context context, List<DayRecord> _allDayRecords, int page, View root){
        Resources r = root.getContext().getResources();
        String name = context.getPackageName();
        int a = 7*page;

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1*_allDayRecords.size());
        int dowOfFirstDay = c.get(Calendar.DAY_OF_WEEK);
       // dowOfFirstDay; //AT this point, if the first day was a Sunday, dowOfFirstDay==0. Monday, 1, etc.

        for (int i = 0 ; i < 7 ; i++){

            int index = i + a - dowOfFirstDay;

            //The indexed day versus today's date
            int offset = index - _allDayRecords.size() + 1;
            Calendar indexedDay = Calendar.getInstance();
            indexedDay.add(Calendar.DATE, offset);
            int dow = indexedDay.get(Calendar.DAY_OF_WEEK);
            int date = indexedDay.get(Calendar.DATE);


            /*Get identifier*/
            int j = i+1; //I'm as confused as you are
            int viewid = r.getIdentifier("day_" + j, "id", name);
            View weekitem = root.findViewById(viewid);
            TextView tv = (TextView) weekitem.findViewById(R.id.calendar_day_info);
            TextView rfd = (TextView) weekitem.findViewById(R.id.record_for_day);
            tv.setText(Integer.toString(date));

            if (index >= _allDayRecords.size()){
                tv.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.grey_lighter), PorterDuff.Mode.SRC_ATOP);
                //tv.getBackground().setAlpha(128);
                continue;
            }else if (index < 0){
                tv.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.grey_lighter), PorterDuff.Mode.SRC_ATOP);
                //tv.getBackground().setAlpha(128);
                continue;
            }
            rfd.setVisibility(View.VISIBLE);
            if (_allDayRecords.get(index).beenToGym()){
                /*HIT DAY*/
                rfd.setText(r.getString(R.string.hit));
                tv.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.schemethree_teal), PorterDuff.Mode.SRC_ATOP);
                tv.setTextColor(ContextCompat.getColor(context, R.color.basewhite));
            }
            else{
                /* For days we haven't gone to gym, we want to say "MISS" if it was a gym day
                    and "REST" if it was a rest day. */
                if (_allDayRecords.get(index).isGymDay()){
                    if (index==_allDayRecords.size()-1){
                        /* CURRENT DAY STATE */
                        //The message for today; don't say "missed"
                        rfd.setText("?");
                    }
                    else{
                        /*MISSED A GYM DAY STATE */
                        rfd.setText(r.getString(R.string.miss));
                        tv.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.schemethree_red), PorterDuff.Mode.SRC_ATOP);
                        tv.setTextColor(ContextCompat.getColor(context, R.color.basewhite));
                    }
                }else{
                    /* REST DAY STATE */
                    rfd.setText(r.getString(R.string.rest));
                }
            }
            if (index==_allDayRecords.size()-1){
                tv.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.schemefour_yellow), PorterDuff.Mode.SRC_ATOP);
                rfd.setTextColor(ContextCompat.getColor(context, R.color.schemefour_yellow));
                rfd.setText("Today");
            }
        }
    }

}
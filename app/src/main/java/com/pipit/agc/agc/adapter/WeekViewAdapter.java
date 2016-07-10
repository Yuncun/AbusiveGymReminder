package com.pipit.agc.agc.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pipit.agc.agc.R;
import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.model.DayRecord;
import com.pipit.agc.agc.widget.CircleView;
import com.pipit.agc.agc.widget.WeekViewSwipeable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Eric on 5/1/2016.
 */
public class WeekViewAdapter extends PagerAdapter {
    private static final String TAG = "WeekViewAdapter";

    private Context mContext;
    protected List<DayRecord> _allDayRecords;
    private WeekViewSwipeable mlayout;
    public int targetWidth;

    public WeekViewAdapter(Context context) {
        mContext = context;
    }

    public WeekViewAdapter(Context context, List<DayRecord> allDayRecords, WeekViewSwipeable layout){
        mlayout = layout;
        mContext = context;
        _allDayRecords = allDayRecords;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.week_calendar, null, true);
        resizeToFit(v);
        styleFromDayrecordsData(mContext, _allDayRecords, position, v);
        //mlayout.updateSparkLineData(getDaysForFocusedWeek(position));
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

    public static String getDayOfWeekText(int n){
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
    public static int getNumberOfWeeks(List<?> dayrecords){
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
     *
     * @param page
     * @return Days of the week, including NULL if we have no info on that day
     */
    public List<DayRecord> getDaysForFocusedWeek(int page){
        int a = 7*page;

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1*(_allDayRecords.size()-1));
        int dowOfFirstDay = c.get(Calendar.DAY_OF_WEEK);
        //We use this calculate the buffer; if Sunday is first day, we can use the first element. But if
        //Wed is the first day, we need to wait 3 elements before using the first dayRecord.
        //Now, DOWofFirstDAY is in Calendar format, i.e. 1-7, we need to decrement here.
        dowOfFirstDay--;

        List<DayRecord> week = new ArrayList<>();
        int indexStart = 0 + a - dowOfFirstDay;
        int indexEnd = 7 + a - dowOfFirstDay;

        while (indexStart < 0){
            indexStart++;
            week.add(null);
        }

        int k = 0;
        while(indexEnd > _allDayRecords.size()){
            k++;
            indexEnd--;
        }

        for (int i = indexStart; i < indexEnd; i++){
            week.add(_allDayRecords.get(i));
        }

        while(k>0){
            k--;
        }

        return week;
    }

    /**
     * Since our view is being instantiated in the ViewPager, it is necessary to size the cirlces dyamically
     * @param wv
     */
    public void resizeToFit(View wv){
        if (wv==null || mlayout==null) return;
        Resources r = wv.getResources();
        String name = wv.getContext().getPackageName();

        int width = mlayout.getSavedWidth();
        int navwidth = mlayout.getSavedNavWidth();
        if (width<=0) width = mlayout.getWidth();
        if (navwidth<=0) navwidth = WeekViewSwipeable.nav_button_default_width;

        Log.d("Eric", "Layout width" + width + " navWidth" + navwidth);

        // Calculate the expected dimen of each circle
        int cvwidth = (width -
                navwidth*2)/7 -
                (WeekViewSwipeable.circleMarginDefaultLeft + WeekViewSwipeable.circleMarginDefaultRight) -
                16;

        if (cvwidth<=0) return;

        for (int i = 1; i < 8; i++){
            int viewid = r.getIdentifier("day_" + i, "id", name);
            View weekitem = wv.findViewById(viewid);

            CircleView cv = (CircleView) weekitem.findViewById(R.id.calendar_day_info);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(cvwidth, cvwidth);

            layoutParams.setMargins(WeekViewSwipeable.circleMarginDefaultLeft,
                    WeekViewSwipeable.circleMarginDefaultTop,
                    WeekViewSwipeable.circleMarginDefaultRight,
                    WeekViewSwipeable.circleMarginDefaultBottom);
            cv.setLayoutParams(layoutParams);
        }
    }

    /**
    * Used to populate and style the WeekCalendarView that shows attendance over last seven days
    * @return
    */
    public void styleFromDayrecordsData(final Context context, final List<DayRecord> _allDayRecords, int page, View root){
        Resources r = root.getContext().getResources();
        String name = context.getPackageName();
        int a = 7*page;

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1*(_allDayRecords.size()-1));
        int dowOfFirstDay = c.get(Calendar.DAY_OF_WEEK);
        //We use this calculate the buffer; if Sunday is first day, we can use the first element. But if
        //Wed is the first day, we need to wait 3 elements before using the first dayRecord.
        //Now, DOWofFirstDAY is in Calendar format, i.e. 1-7, we need to decrement here.
        dowOfFirstDay--;


        for (int i = 0 ; i < 7 ; i++){

            //i == which card we are on
            //a == offset from the page.
            //dowOfFirstDay == 0-6 where 0 is Sunday.
            //Index is the index of the dayRecord that corresponds to the given i card.
            //If it negative, then we have no record for it.
            final int index = i + a - dowOfFirstDay;

            /*Get identifier*/
            int j = i+1; //I'm as confused as you are
            int viewid = r.getIdentifier("day_" + j, "id", name);
            View weekitem = root.findViewById(viewid);
            CircleView cv = (CircleView) weekitem.findViewById(R.id.calendar_day_info);
            TextView rfd = (TextView) weekitem.findViewById(R.id.record_for_day);

            //This snippet assigns the date numbers to the weekview
            //This code uses Sunday as the last day of the week and fills in gaps
            int offset = index - (_allDayRecords.size()-1);
            Calendar indexedDay = Calendar.getInstance();
            indexedDay.add(Calendar.DATE, offset);
            int dow = indexedDay.get(Calendar.DAY_OF_WEEK);
            int date = indexedDay.get(Calendar.DATE);
            cv.setTitleText(Integer.toString(date));

            drawCircleDays(context, cv, rfd, weekitem, index);
        }
    }

    /**
     * This is called for every day of each weekview. It is the key function that binds adapter data
     * to each day in your weekview. Override and set your own behavior for the given circleview, etc.
     * @param context
     * @param cv - Circleview of the given day. Set your custom text/click behavior here
     * @param rfd - "Record for Day" - This textview sits below the circleview
     * @param weekitem - The entire day view (parent of circleview)
     * @param index - index relative to adapter data
     */
    protected void drawCircleDays(final Context context, CircleView cv, TextView rfd, View weekitem, final int index ){

    }

}
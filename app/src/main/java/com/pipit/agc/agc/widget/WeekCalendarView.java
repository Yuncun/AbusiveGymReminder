package com.pipit.agc.agc.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.Circle;
import com.pipit.agc.agc.R;
import com.pipit.agc.agc.model.DayRecord;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Eric on 3/12/2016.
 * @Deprecated
 **/
public class WeekCalendarView extends LinearLayout {
    private static final String TAG = "WeekCalendarView";

    private static final int circleMarginDefaultLeft = 4;
    private static final int circleMarginDefaultRight = 4;
    private static final int circleMarginDefaultTop = 2;
    private static final int circleMarginDefaultBottom = 0;

    private float circleMargin;

    public WeekCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.WeekCalendarView, 0, 0);
        circleMargin = a.getDimension(R.styleable.WeekCalendarView_paddingBetweenCircles, 4.0f);
        a.recycle();

        setOrientation(LinearLayout.HORIZONTAL);

        setDayOfWeekEnd(0);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.week_calendar, this, true);
    }

    public WeekCalendarView(Context context) {
        this(context, null);
    }

    @Override public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d(TAG, " onMeasure ");
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        for (int i = 0; i<7; i++){
            LinearLayout ll = getDayViewFromPosition(i);
            CircleView cv = (CircleView) ll.findViewById(R.id.calendar_day_info);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(40, 40);

            layoutParams.setMargins(circleMarginDefaultLeft, circleMarginDefaultTop, circleMarginDefaultRight, circleMarginDefaultBottom);
            //cv.setLayoutParams(layoutParams);

        }

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
     *
     * @param pos from 1-7
     * @return
     */
    public LinearLayout getDayViewFromPosition(int pos){
        if (pos<0 || pos>6){ return null; }
        pos++;
        Resources r = getResources();
        String name = this.getContext().getPackageName();
        int viewid = r.getIdentifier("day_" + pos, "id", name);
        LinearLayout weekitem = (LinearLayout) findViewById(viewid);
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



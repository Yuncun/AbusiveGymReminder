package com.pipit.agc.agc.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pipit.agc.agc.R;
import com.pipit.agc.agc.adapter.WeekViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eric on 3/12/2016.
 */

public class WeekViewSwipeable extends LinearLayout {
    private static final String TAG = "WeekViewSwipeable";
    public static final String PERSISTEDWIDTH = "weekviewwidth";
    public static final String NAV_WIDTH = "navbuttonwidth";

    public static final int circleMarginDefaultLeft = 4;
    public static final int circleMarginDefaultRight = 4;
    public static final int circleMarginDefaultTop = 2;
    public static final int circleMarginDefaultBottom = 0;
    public static final int nav_button_default_width = 36;

    public int circleMargin;

    public AgcViewPager viewPager;
    private WeekViewAdapter wvadapter;
    public ImageButton leftNav;
    public ImageButton rightNav;
    private int _navwidth = nav_button_default_width;
    protected boolean navEnabled = true;

    public WeekViewSwipeable(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.WeekCalendarView, 0, 0);

        circleMargin = (int) a.getDimension(R.styleable.WeekCalendarView_paddingBetweenCircles, 4.0f);
        a.recycle();

        setOrientation(LinearLayout.HORIZONTAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View root = inflater.inflate(R.layout.week_calendar_vp, this, true);
        viewPager = (AgcViewPager) root.findViewById(R.id.weekvp);

        leftNav = (ImageButton) root.findViewById(R.id.left_nav);
        rightNav = (ImageButton) root.findViewById(R.id.right_nav);

        leftNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tab = viewPager.getCurrentItem();
                if (tab > 0) {
                    tab--;
                    viewPager.setCurrentItem(tab);
                } else if (tab == 0) {
                    viewPager.setCurrentItem(tab);
                }
            }
        });

        rightNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tab = viewPager.getCurrentItem();
                tab++;
                viewPager.setCurrentItem(tab);
            }
        });

        final WeekViewSwipeable ll = this;
        setAdapter(new WeekViewAdapter(getContext(), new ArrayList(), ll));
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
            tv.setText(WeekViewAdapter.getDayOfWeekText((i + offset) % 7));
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

    public void showLastDayMarker(){
        findViewById(R.id.day_7).findViewById(R.id.record_for_day).setVisibility(VISIBLE);
    }

    public int getSavedNavWidth(){
        return _navwidth;
    }

    public void setNavEnabled(boolean f){
        navEnabled=f;
        if (!f){
            leftNav.setVisibility(GONE);
            rightNav.setVisibility(GONE);
       }
    }

    public void setAdapter(WeekViewAdapter wva){
        wvadapter = wva;
        viewPager.setAdapter(wva);
        viewPager.setCurrentItem(wvadapter.getStartPosition());
    }
}



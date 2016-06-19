package com.pipit.agc.agc.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pipit.agc.agc.R;
import com.pipit.agc.agc.adapter.MySparkAdapter;
import com.pipit.agc.agc.adapter.WeekViewAdapter;
import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.model.DayRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Eric on 3/12/2016.
 */
public class WeekViewSwipeable extends LinearLayout {
    private static String TAG = "WeekViewSwipeable";

    public static final int circleMarginDefaultLeft = 4;
    public static final int circleMarginDefaultRight = 4;
    public static final int circleMarginDefaultTop = 2;
    public static final int circleMarginDefaultBottom = 0;

    public int circleMargin;


    public AgcViewPager viewPager;
    private MySparkAdapter sparkAdapter;
    private WeekViewAdapter wvadapter;
    public ImageButton leftNav;
    public ImageButton rightNav;
    private  List<DayRecord> _allPreviousDays;
    private boolean listenForLayoutUpdate;

    public WeekViewSwipeable(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.WeekCalendarView, 0, 0);

        circleMargin = (int) a.getDimension(R.styleable.WeekCalendarView_paddingBetweenCircles, 4.0f);
        a.recycle();

        setOrientation(LinearLayout.HORIZONTAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = inflater.inflate(R.layout.week_calendar_vp, this, true);

        DBRecordsSource datasource = DBRecordsSource.getInstance();
        datasource.openDatabase();
        _allPreviousDays = datasource.getAllDayRecords();
        DBRecordsSource.getInstance().closeDatabase();

        listenForLayoutUpdate = true;
        viewPager = (AgcViewPager) root.findViewById(R.id.weekvp);
        wvadapter = new WeekViewAdapter(context, _allPreviousDays, this);
        viewPager.setAdapter(wvadapter);
        viewPager.setCurrentItem(WeekViewAdapter.getNumberOfWeeks(_allPreviousDays) - 1);
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

        // Images right navigatin
        rightNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tab = viewPager.getCurrentItem();
                tab++;
                viewPager.setCurrentItem(tab);
            }
        });

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

        //I found that sparkline doesn't show line with only one value
        if (times.length < 2){
            times = new float[] {times[0], times[0]};
            sparkAdapter.setGraphWidth(18.0f); //One data point should not appear too large
        } else {
            sparkAdapter.setGraphWidth(6.0f);
        }
        
        sparkAdapter.update(times);
        //Log.d(TAG, "updating sparkline " + Arrays.toString(times));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Log.d("Eric", "WIDTH IS READY " + getWidth());
        if (listenForLayoutUpdate){
            wvadapter = new WeekViewAdapter(getContext(), _allPreviousDays, this);
            viewPager.setAdapter(wvadapter);
            listenForLayoutUpdate = false;
        }
    }


    public void attachSparklineAdapter(MySparkAdapter sparky){
        sparkAdapter = sparky;
    }

    public void unattachSparklineAdapter(){
        sparkAdapter=null;
    }


    public void showLastDayMarker(){
        findViewById(R.id.day_7).findViewById(R.id.record_for_day).setVisibility(VISIBLE);
    }

    public void showLastDayMarker(int color){
        findViewById(R.id.day_7).setBackgroundColor(color);
        showLastDayMarker();
    }


}



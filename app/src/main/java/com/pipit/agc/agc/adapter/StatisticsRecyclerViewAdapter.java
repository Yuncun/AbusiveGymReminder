package com.pipit.agc.agc.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pipit.agc.agc.R;
import com.pipit.agc.agc.data.MySQLiteHelper;
import com.pipit.agc.agc.fragment.StatisticsFragment;
import com.pipit.agc.agc.fragment.StatisticsFragment.OnListFragmentInteractionListener;
import com.pipit.agc.agc.model.DayRecord;
import com.pipit.agc.agc.util.SharedPrefUtil;
import com.pipit.agc.agc.util.StatsContent;
import com.pipit.agc.agc.util.StatsContent.Stat;
import com.pipit.agc.agc.widget.WeekCalendarView;
import com.pipit.agc.agc.widget.WeekViewSwipeable;
import com.robinhood.spark.SparkView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Stat} and makes a call to the
 * specified {@link StatisticsFragment.OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class StatisticsRecyclerViewAdapter extends RecyclerView.Adapter<StatisticsRecyclerViewAdapter.ViewHolder> {
    private final int TODAY_STATS_VIEWTYPE = 0;
    private final int WEEK__STATS_VIEWTYPE = 1;
    private final int MONTH_STATS_VIEWTYPE = 2;
    boolean d1 = true;

    private final StatsContent mStats;
    private StatisticsFragment mFrag;
    private final OnListFragmentInteractionListener mListener;

    public StatisticsRecyclerViewAdapter(StatsContent stats, OnListFragmentInteractionListener listener, StatisticsFragment frag) {
        mStats = stats;
        mListener = listener;
        mFrag = frag;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType){
            case 0:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.daily_stats_card, parent, false);
                return new DayViewHolder(view);
            case 1:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.weekly_stats_card, parent, false);
                return new WeeklyViewHolder(view);
            case 2:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.misc_stats_card, parent, false);
                return new MiscStatsViewHolder(view);
            default:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.stats_row_item, parent, false);
            }


        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type){
            case 0:
                //daily stat card;
                DayViewHolder dv = ((DayViewHolder) holder);
                holder.mTitleView.setText("Today");
                DayRecord today = mStats.getToday(true);
                if (today.isGymDay()){
                    dv.gymstate_circle.setText("GYM\nDAY");
                    setTextCircleColor(dv.gymstate_circle, mFrag.getContext(), R.color.schemethree_red);
                    dv.gymstate_circle.setTextColor(ContextCompat.getColor(mFrag.getContext(), R.color.basewhite));
                }else{
                    setTextCircleColor(dv.gymstate_circle, mFrag.getContext(), R.color.schemethree_teal);
                    dv.gymstate_circle.setTextColor(ContextCompat.getColor(mFrag.getContext(), R.color.basewhite));
                    dv.gymstate_circle.setText("REST\nDAY");
                }
                if (today.beenToGym()){
                    SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
                    String s = SharedPrefUtil.getLastVisitString(mFrag.getContext(), sdf);
                    String prefix = "Gym visit recorded today at ";
                    if (s==null || s.equals("")){
                        dv.gymstate_text.setText("Gym visit recorded today");
                    }else{
                        dv.gymstate_text.setText(prefix + s);
                    }
                }else{
                    dv.gymstate_text.setText("No gym visit recorded today");
                    String s = SharedPrefUtil.getLastVisitString(mFrag.getContext(), null);
                    if (s!=null){
                        dv.lastvisit_text.setText("Last visit recorded at " + s);
                        dv.lastvisit_text.setVisibility(View.VISIBLE);
                    }
                }
                dv.gymstate_text.setTextSize(30);
                break;

            case 1:
                //weekly_stats_card;
                holder.mTitleView.setText("Last seven days");
                final WeeklyViewHolder wv = (WeeklyViewHolder) holder;

                /* Text */
                wv.stat_circle_1.setText(mStats.STAT_MAP.get(StatsContent.DAYS_PLANNED_WEEK).get() + "");
                wv.stat_circle_2.setText(mStats.STAT_MAP.get(StatsContent.MISSED_GYMDAYS_WEEK).get() + "");
                wv.stat_circle_3.setText(mStats.STAT_MAP.get(StatsContent.DAYS_HIT_WEEK).get() + "");

                /* Colors */
                setTextCircleColor(wv.stat_circle_1, mFrag.getContext(), R.color.grey);
                wv.stat_circle_1.setTextColor(ContextCompat.getColor(mFrag.getContext(), R.color.schemethree_teal));
                setTextCircleColor(wv.stat_circle_2, mFrag.getContext(), R.color.grey);
                wv.stat_circle_2.setTextColor(ContextCompat.getColor(mFrag.getContext(), R.color.schemethree_teal));
                setTextCircleColor(wv.stat_circle_3, mFrag.getContext(), R.color.grey);
                wv.stat_circle_3.setTextColor(ContextCompat.getColor(mFrag.getContext(), R.color.schemethree_teal));

                wv.stat_text_1.setText(mStats.STAT_MAP.get(StatsContent.DAYS_PLANNED_WEEK).details);
                wv.stat_text_2.setText(mStats.STAT_MAP.get(StatsContent.MISSED_GYMDAYS_WEEK).details);
                wv.stat_text_3.setText(mStats.STAT_MAP.get(StatsContent.DAYS_HIT_WEEK).details);

                float[] data = {};

                wv.sparkgraph.setAdapter(new MySparkAdapter(data));
                wv.sparkgraph.setAnimateChanges(true);
                wv.sparkgraph.setScrubEnabled(true);
                wv.sparkgraph.setCornerRadius(10.0f);
                wv.sparkgraph.setLineColor(ContextCompat.getColor(mFrag.getContext(), R.color.schemethree_teal));
                wv.sparkgraph.setScrubListener(new SparkView.OnScrubListener() {
                    @Override
                    public void onScrubbed(Object value) {
                        /*)
                        if (d1 == true) {
                            ((MySparkAdapter) wv.sparkgraph.getAdapter()).update(data2);
                            d1 = false;
                        } else {
                            ((MySparkAdapter) wv.sparkgraph.getAdapter()).update(data);
                            d1 = true;
                        }
                        */
                    }
                });
                wv.calendar.attachSparklineAdapter((MySparkAdapter) wv.sparkgraph.getAdapter());
                wv.calendar.updateSparkLineData(((WeekViewAdapter) wv.calendar.viewPager.getAdapter()).getDaysForFocusedWeek(wv.calendar.viewPager.getAdapter().getCount() - 1));

                //vw.calendar.setDayOfWeekText(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
                //wv.calendar.showLastDayMarker();
                //wv.calendar.styleFromDayrecordsData(mFrag.getContext(), mStats.getAllDayRecords(false));
                break;
            case 2:
                holder.mTitleView.setText("Streaks");
                MiscStatsViewHolder mv = (MiscStatsViewHolder) holder;
                /* Text */
                mv.stat_circle_1.setText(mStats.STAT_MAP.get(StatsContent.CURRENT_STREAK).get()+"");
                mv.stat_circle_2.setText(mStats.STAT_MAP.get(StatsContent.LONGEST_STREAK).get()+"");

                mv.stat_text_1.setText(mStats.STAT_MAP.get(StatsContent.CURRENT_STREAK).details);
                mv.stat_text_2.setText(mStats.STAT_MAP.get(StatsContent.LONGEST_STREAK).details);

                //mv.stat_card_1.setBackgroundColor(ContextCompat.getColor(mFrag.getContext(), R.color.lightgreen));
                mv.stat_circle_1.setTextColor(ContextCompat.getColor(mFrag.getContext(), R.color.basewhite));
                setTextCircleColor(mv.stat_circle_1, mFrag.getContext(), R.color.schemefour_teal);
                mv.stat_circle_2.setTextColor(ContextCompat.getColor(mFrag.getContext(), R.color.basewhite));
                setTextCircleColor(mv.stat_circle_2, mFrag.getContext(), R.color.schemefour_teal);

                break;
            default:
                holder.mTitleView.setText("Default");
                break;
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public class DayViewHolder extends StatisticsRecyclerViewAdapter.ViewHolder{
        public final TextView gymstate_circle;
        public final TextView gymstate_text;
        public final TextView lastvisit_text;

        public DayViewHolder(View view){
            super(view);
            gymstate_circle = (TextView) view.findViewById(R.id.gymstate_circle);
            gymstate_text = (TextView) view.findViewById(R.id.gymstate_text);
            lastvisit_text = (TextView) view.findViewById(R.id.last_visit_txt);
        }
    }

    public class WeeklyViewHolder extends StatisticsRecyclerViewAdapter.ViewHolder{
        public final TextView stat_circle_1;
        public final TextView stat_text_1;
        public final TextView stat_circle_2;
        public final TextView stat_text_2;
        public final TextView stat_circle_3;
        public final TextView stat_text_3;
        public final SparkView sparkgraph;

        public final WeekViewSwipeable calendar;

        public WeeklyViewHolder(View view){
            super(view);
            stat_circle_1 = (TextView) view.findViewById(R.id.stat_circle_1);
            stat_circle_2 = (TextView) view.findViewById(R.id.stat_circle_2);
            stat_circle_3 = (TextView) view.findViewById(R.id.stat_circle_3);

            stat_text_1 = (TextView) view.findViewById(R.id.stat_name_1);
            stat_text_2 = (TextView) view.findViewById(R.id.stat_name_2);
            stat_text_3 = (TextView) view.findViewById(R.id.stat_name_3);

            calendar = (WeekViewSwipeable) view.findViewById(R.id.calendar_component);
            sparkgraph = (SparkView) view.findViewById(R.id.sparkview);

        }
    }

    public class MiscStatsViewHolder extends StatisticsRecyclerViewAdapter.ViewHolder {
        public final TextView stat_circle_1;
        public final TextView stat_text_1;
        public final TextView stat_circle_2;
        public final TextView stat_text_2;
        public final RelativeLayout stat_card_1;
        public final RelativeLayout stat_card_2;

        public MiscStatsViewHolder(View view){
            super(view);
            stat_card_1 = (RelativeLayout) view.findViewById(R.id.stat_card_1);
            stat_card_2 = (RelativeLayout) view.findViewById(R.id.stat_card_2);

            stat_circle_1 = (TextView) view.findViewById(R.id.stat_circle_1);
            stat_circle_2 = (TextView) view.findViewById(R.id.stat_circle_2);

            stat_text_1 = (TextView) view.findViewById(R.id.stat_text_1);
            stat_text_2 = (TextView) view.findViewById(R.id.stat_text_2);
        }
    }

        public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public Stat mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.title_name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }

    public int getItemViewType (int position) {
        return position;
    }

    public static void setTextCircleColor(TextView tv, Context context, int my_color){
        tv.getBackground().setColorFilter(ContextCompat.getColor(context, my_color), PorterDuff.Mode.SRC_ATOP);
    }

    public static void setStrokeColor(TextView tv, Context context, int my_color){
        GradientDrawable drawable = (GradientDrawable) tv.getBackground();
        drawable.setStroke(6, ContextCompat.getColor(context, my_color)); // set stroke width and stroke color
    }
}

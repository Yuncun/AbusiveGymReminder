package com.pipit.agc.agc.adapter;

import android.content.Context;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pipit.agc.agc.R;
import com.pipit.agc.agc.fragment.StatisticsFragment;
import com.pipit.agc.agc.model.DayRecord;
import com.pipit.agc.agc.util.SharedPrefUtil;
import com.pipit.agc.agc.util.StatsContent;
import com.pipit.agc.agc.util.StatsContent.Stat;
import com.pipit.agc.agc.widget.CircleView;
import com.pipit.agc.agc.widget.WeekViewSwipeable;
import com.robinhood.spark.SparkView;

import java.text.SimpleDateFormat;
import java.util.List;

public class StatisticsRecyclerViewAdapter extends RecyclerView.Adapter<StatisticsRecyclerViewAdapter.ViewHolder> {
    private final StatsContent mStats;
    private StatisticsFragment mFrag;

    public StatisticsRecyclerViewAdapter(StatsContent stats, StatisticsFragment frag) {
        mStats = stats;
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
                dv.gymstate_circle.setShowSubtitle(false);
                if (today.isGymDay()){
                    dv.gymstate_circle.setTitleText("GYM\nDAY");
                    dv.gymstate_circle.setStrokeColor(ContextCompat.getColor(mFrag.getContext(), R.color.schemethree_darkerred));
                    //dv.gymstate_circle.setTitleColor(ContextCompat.getColor(mFrag.getContext(), R.color.schemethree_red));
                }else{
                    dv.gymstate_circle.setTitleText("REST\nDAY");
                    dv.gymstate_circle.setStrokeColor(ContextCompat.getColor(mFrag.getContext(), R.color.schemethree_darkerteal));
                    //dv.gymstate_circle.setTitleColor(ContextCompat.getColor(mFrag.getContext(), R.color.schemethree_teal));
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
                        dv.lastvisit_text.setText("Last visit: " + s);
                        dv.lastvisit_text.setVisibility(View.VISIBLE);
                    }
                }
                dv.gymstate_text.setTextSize(30);

                //TEST
                //dv.rootlayout.setVisibility(View.GONE);

                break;

            case 1:
                //weekly_stats_card;
                holder.mTitleView.setText("Last seven days");
                final WeeklyViewHolder wv = (WeeklyViewHolder) holder;

                float[] data = {};
                wv.sparkgraph.setAdapter(new MySparkAdapter(data));
                wv.sparkgraph.setAnimateChanges(true);
                wv.sparkgraph.setScrubEnabled(true);
                wv.sparkgraph.setCornerRadius(5.0f);
                wv.sparkgraph.setLineColor(ContextCompat.getColor(mFrag.getContext(), R.color.schemethree_darkerteal));
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
                Paint baseLinePaint = wv.sparkgraph.getBaseLinePaint();
                DashPathEffect dashPathEffect = new DashPathEffect(new float[] {10 , 20}, 0);
                baseLinePaint.setPathEffect(dashPathEffect);

                wv.calendar.attachSparklineAdapter((MySparkAdapter) wv.sparkgraph.getAdapter());
                List<DayRecord> daysForWeek = ((WeekViewAdapter) wv.calendar.viewPager.getAdapter()).getDaysForFocusedWeek(wv.calendar.viewPager.getAdapter().getCount() - 1);
                wv.calendar.updateSparkLineData(daysForWeek);

                //vw.calendar.setDayOfWeekText(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
                //wv.calendar.showLastDayMarker();
                //wv.calendar.styleFromDayrecordsData(mFrag.getContext(), mStats.getAllDayRecords(false));
                break;
            case 2:
                holder.mTitleView.setText("Streaks");
                MiscStatsViewHolder mv = (MiscStatsViewHolder) holder;
                /* Text */
                mv.stat_circle_1.setTitleText(mStats.STAT_MAP.get(StatsContent.CURRENT_STREAK).get() + "");
                mv.stat_circle_2.setTitleText(mStats.STAT_MAP.get(StatsContent.LONGEST_STREAK).get() + "");

                mv.stat_text_1.setText(mStats.STAT_MAP.get(StatsContent.CURRENT_STREAK).details);
                mv.stat_text_2.setText(mStats.STAT_MAP.get(StatsContent.LONGEST_STREAK).details);

                //Todo:Use dimens
                mv.stat_circle_1.setTitleSize(80f);
                mv.stat_circle_2.setTitleSize(80f);
                mv.stat_circle_1.setShowSubtitle(false);
                mv.stat_circle_2.setShowSubtitle(false);

                mv.stat_circle_1.setStrokeColor(ContextCompat.getColor(mFrag.getContext(), R.color.schemethree_darkerteal));
                mv.stat_circle_1.setTitleColor(ContextCompat.getColor(mFrag.getContext(), R.color.basewhite));
                mv.stat_circle_2.setStrokeColor(ContextCompat.getColor(mFrag.getContext(), R.color.schemethree_darkerteal));
                mv.stat_circle_2.setTitleColor(ContextCompat.getColor(mFrag.getContext(), R.color.basewhite));
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
        public final CircleView gymstate_circle;
        public final TextView gymstate_text;
        public final TextView lastvisit_text;
        public final CardView rootlayout;

        public DayViewHolder(View view){
            super(view);
            rootlayout = (CardView) view.findViewById(R.id.dailystatscard);
            gymstate_circle = (CircleView) view.findViewById(R.id.gymstate_circle);
            gymstate_text = (TextView) view.findViewById(R.id.gymstate_text);
            lastvisit_text = (TextView) view.findViewById(R.id.last_visit_txt);
        }
    }

    public class WeeklyViewHolder extends StatisticsRecyclerViewAdapter.ViewHolder{

        public final SparkView sparkgraph;

        public final WeekViewSwipeable calendar;

        public WeeklyViewHolder(View view){
            super(view);
            calendar = (WeekViewSwipeable) view.findViewById(R.id.calendar_component);
            sparkgraph = (SparkView) view.findViewById(R.id.sparkview);


        }
    }

    public class MiscStatsViewHolder extends StatisticsRecyclerViewAdapter.ViewHolder {
        public final CircleView stat_circle_1;
        public final CircleView stat_circle_2;
        public final TextView stat_text_1;
        public final TextView stat_text_2;
        public final RelativeLayout stat_card_1;
        public final RelativeLayout stat_card_2;

        public MiscStatsViewHolder(View view){
            super(view);
            stat_card_1 = (RelativeLayout) view.findViewById(R.id.stat_card_1);
            stat_card_2 = (RelativeLayout) view.findViewById(R.id.stat_card_2);

            stat_circle_1 = (CircleView) view.findViewById(R.id.stat_circle_1);
            stat_circle_2 = (CircleView) view.findViewById(R.id.stat_circle_2);

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

}

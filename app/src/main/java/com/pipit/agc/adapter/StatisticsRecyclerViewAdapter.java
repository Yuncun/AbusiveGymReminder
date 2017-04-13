package com.pipit.agc.adapter;

import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pipit.agc.R;
import com.pipit.agc.controller.DayrecordDialog;
import com.pipit.agc.fragment.StatisticsFragment;
import com.pipit.agc.model.DayRecord;
import com.pipit.agc.util.Constants;
import com.pipit.agc.util.SharedPrefUtil;
import com.pipit.agc.util.StatsContent;
import com.pipit.agc.util.StatsContent.Stat;
import com.pipit.agc.util.Util;
import com.pipit.agc.views.CalendarWeekViewSwipeable;
import com.pipit.agc.views.CircleView;
import com.robinhood.spark.SparkView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import iSoron.HistoryChart;

public class StatisticsRecyclerViewAdapter extends RecyclerView.Adapter<StatisticsRecyclerViewAdapter.ViewHolder>
    implements DayrecordDialog.DayrecordObserver {
    private static final String TAG = "StatsAdapter";
    private final StatsContent mStats;
    private StatisticsFragment mFrag;

    private HistoryChart mHistoryChart;

    public StatisticsRecyclerViewAdapter( StatisticsFragment frag) {
        mStats = StatsContent.getInstance();
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
                        .inflate(R.layout.monthly_stats_card, parent, false);
                return new MonthViewHolder(view);
            case 3:
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
        List<DayRecord> allPreviousDays =  StatsContent.getInstance().getAllDayRecords(false);
        int type = getItemViewType(position);
        switch (type){
            case 0:
                //daily stat card;
                final DayViewHolder dv = ((DayViewHolder) holder);
                holder.mTitleView.setText(mFrag.getContext().getString(R.string.today));
                DayRecord today = mStats.getToday(true);

                //There are two possible states:
                //  1) We are currently not visiting a gym
                //  2) We are currently visiting a gym
                if (!today.isCurrentlyVisiting()) {
                    dv.gymstate_circle_layout.setVisibility(View.VISIBLE);


                    if (today.beenToGym() || today.getTotalVisitsMinutes()>0) {
                        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
                        String s = SharedPrefUtil.getLastVisitString(mFrag.getContext(), sdf);
                        String prefix = mFrag.getContext().getString(R.string.recordedtodayat);
                        if (s == null || s.equals("")) {
                            dv.gymstate_text.setText(mFrag.getContext().getString(R.string.lastvisit));
                        } else {
                            dv.gymstate_text.setText(prefix + " " + s);
                        }
                    } else {
                        dv.gymstate_text.setText(mFrag.getContext().getString(R.string.norecordtoday));
                        String s = SharedPrefUtil.getLastVisitString(mFrag.getContext(), null);
                        if (s != null) {
                            dv.lastvisit_text.setText((mFrag.getContext().getString(R.string.lastvisit)) + " " + s);
                            dv.lastvisit_text.setVisibility(View.VISIBLE);
                        }
                    }
                }else{
                    dv.gymstate_text.setText(R.string.youareatthegym);
                    dv.gymstate_text.setTextColor(Util.getStyledColor(mFrag.getContext(),
                            R.attr.colorAccent));

                }
                dv.gymstate_text.setTextSize(30);

                dv.gymstate_circle.setShowSubtitle(false);
                if (today.isGymDay() && !today.beenToGym()) {
                    dv.gymstate_circle.setTitleText(mFrag.getContext().getString(R.string.gymday));
                    dv.gymstate_circle.setStrokeColor(Util.getStyledColor(mFrag.getContext(),
                            R.attr.missColor));
                } else {
                    dv.gymstate_circle.setTitleText(mFrag.getContext().getString(R.string.restday));
                    dv.gymstate_circle.setStrokeColor(Util.getStyledColor(mFrag.getContext(),
                            R.attr.explicitHitColor));
                }
                break;

            case 1:
                //weekly_stats_card;
                holder.mTitleView.setText(mFrag.getContext().getString(R.string.lastsevendays));
                final WeeklyViewHolder wv = (WeeklyViewHolder) holder;
                CalendarWeekViewAdapter cwva = new CalendarWeekViewAdapter(mFrag.getActivity(), allPreviousDays, wv.calendar);
                cwva.setObserver(this);
                wv.calendar.setAdapter(cwva);

                float[] data = {};
                wv.sparkgraph.setAdapter(new MySparkAdapter(data));
                wv.sparkgraph.setAnimateChanges(true);
                wv.sparkgraph.setScrubEnabled(true);
                wv.sparkgraph.setCornerRadius(.125f);
                wv.sparkgraph.setLineColor(Util.getStyledColor(mFrag.getContext(),
                        R.attr.explicitHitColor));
                wv.sparkgraph.setScrubListener(new SparkView.OnScrubListener() {
                    @Override
                    public void onScrubbed(Object value) {
                        //Todo: Show time?
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
                holder.mTitleView.setText(mFrag.getContext().getString(R.string.history));
                MonthViewHolder mv = (MonthViewHolder) holder;

                /*Populate*/
                int[] checkmarks = new int[allPreviousDays.size()];
                int i = checkmarks.length-1;
                for (DayRecord d : allPreviousDays){
                    if (d.isGymDay()){
                        if (d.beenToGym()){checkmarks[i] = 2;}
                        else{checkmarks[i] = 1;}
                    }else{
                        if (d.beenToGym()){checkmarks[i] = 2;}
                        else{checkmarks[i] = 0;}
                    }
                    i--;
                }
                mv.historyChart.setIsEditable(true);
                mv.historyChart.setCheckmarks(checkmarks);
                mv.historyChart.setController(historyGraphController);
                mHistoryChart = mv.historyChart;
                break;
            case 3:
                holder.mTitleView.setText(mFrag.getContext().getString(R.string.streaks));
                MiscStatsViewHolder msv = (MiscStatsViewHolder) holder;
                /* Text */
                msv.stat_circle_1.setTitleText(mStats.STAT_MAP.get(StatsContent.CURRENT_STREAK).get() + "");
                msv.stat_circle_2.setTitleText(mStats.STAT_MAP.get(StatsContent.LONGEST_STREAK).get() + "");

                msv.stat_text_1.setText(mStats.STAT_MAP.get(StatsContent.CURRENT_STREAK).details);
                msv.stat_text_2.setText(mStats.STAT_MAP.get(StatsContent.LONGEST_STREAK).details);

                msv.stat_subtext_1.setVisibility(View.GONE);
                msv.stat_subtext_2.setText(mStats.STAT_MAP.get(StatsContent.WEEK_OF_RECORD_STREAK).details);

                //Todo:Use dimens
                msv.stat_circle_1.setTitleSize(80f);
                msv.stat_circle_2.setTitleSize(80f);
                msv.stat_circle_1.setShowSubtitle(false);
                msv.stat_circle_2.setShowSubtitle(false);

                msv.stat_circle_1.setStrokeColor(Util.getStyledColor(mFrag.getContext(),
                        R.attr.explicitHitColor));
                msv.stat_circle_1.setTitleColor(ContextCompat.getColor(mFrag.getContext(), R.color.basewhite));
                msv.stat_circle_2.setStrokeColor(Util.getStyledColor(mFrag.getContext(),
                        R.attr.explicitHitColor));
                msv.stat_circle_2.setTitleColor(ContextCompat.getColor(mFrag.getContext(), R.color.basewhite));
                break;
            default:
                holder.mTitleView.setText("Default");
                break;
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    public class DayViewHolder extends StatisticsRecyclerViewAdapter.ViewHolder{
        public final LinearLayout gymstate_circle_layout;
        public final CircleView gymstate_circle;
        public final TextView gymstate_text;
        public final TextView lastvisit_text;
        public final CardView rootlayout;

        public DayViewHolder(View view){
            super(view);
            rootlayout = (CardView) view.findViewById(R.id.dailystatscard);
            gymstate_circle_layout = (LinearLayout) view.findViewById(R.id.gymstate_circle_layout);
            gymstate_circle = (CircleView) view.findViewById(R.id.gymstate_circle);
            gymstate_text = (TextView) view.findViewById(R.id.gymstate_text);
            lastvisit_text = (TextView) view.findViewById(R.id.last_visit_txt);
        }
    }

    public class WeeklyViewHolder extends StatisticsRecyclerViewAdapter.ViewHolder{

        public final SparkView sparkgraph;
        public final CalendarWeekViewSwipeable calendar;

        public WeeklyViewHolder(View view){
            super(view);
            calendar = (CalendarWeekViewSwipeable) view.findViewById(R.id.calendar_component);
            sparkgraph = (SparkView) view.findViewById(R.id.sparkview);
        }
    }

    public class MonthViewHolder extends StatisticsRecyclerViewAdapter.ViewHolder{

        public final HistoryChart historyChart;

        public MonthViewHolder(View view){
            super(view);
            historyChart = (HistoryChart) view.findViewById(R.id.historyChart);
            historyChart.setColor(R.color.schemethree_darkerteal);
            historyChart.setIsBackgroundTransparent(false);
        }
    }

    private HistoryChart.Controller historyGraphController = new HistoryChart.Controller() {
        @Override
        public void onToggleCheckmark(long timestamp, int offset) {
            List<DayRecord> days = new ArrayList( StatsContent.getInstance().getAllDayRecords(false));
            int index = days.size() - 1 - offset;
            if (index>=days.size() || index < 0) return;
            DayrecordDialog dcl = new DayrecordDialog(days.get(index), mFrag.getContext());
            dcl.setObserver(getSelf());
            dcl.onClick(null);
        }
    };

    public class MiscStatsViewHolder extends StatisticsRecyclerViewAdapter.ViewHolder {
        public final CircleView stat_circle_1;
        public final CircleView stat_circle_2;
        public final TextView stat_text_1;
        public final TextView stat_text_2;
        public final TextView stat_subtext_1;
        public final TextView stat_subtext_2;
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

            stat_subtext_1 = (TextView) view.findViewById(R.id.stat_subtext_1);
            stat_subtext_2 = (TextView) view.findViewById(R.id.stat_subtext_2);
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

    public StatisticsRecyclerViewAdapter getSelf(){
        return this;
    }

    /**
     * For DayRecordObserver interface. This is called as an observer when a refresh is needed.
     * Currently only used for when user updates gym history.
     */
    public void update(){
        if (mHistoryChart!=null){
            mHistoryChart.postInvalidate();
        }
        notifyDataSetChanged();
    }
}

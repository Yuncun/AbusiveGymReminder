package com.pipit.agc.agc.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pipit.agc.agc.R;
import com.pipit.agc.agc.fragment.StatisticsFragment;
import com.pipit.agc.agc.fragment.StatisticsFragment.OnListFragmentInteractionListener;
import com.pipit.agc.agc.model.DayRecord;
import com.pipit.agc.agc.util.StatsContent;
import com.pipit.agc.agc.util.StatsContent.Stat;
import com.pipit.agc.agc.widget.WeekCalendarView;

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
                holder.mTitleView.setText("Today");
                DayRecord today = mStats.getToday(false);
                if (today.isGymDay()){
                    ((DayViewHolder) holder).gymstate_circle.setText("GYM\nDAY");
                }else{
                    ((DayViewHolder) holder).gymstate_circle.setText("REST\nDAY");
                }

                if (today.beenToGym()){
                    ((DayViewHolder) holder).gymstate_text.setText("Gym visit recorded today");
                }else{
                    ((DayViewHolder) holder).gymstate_text.setText("No gym visit recorded today");
                }
                ((DayViewHolder) holder).gymstate_text.setTextSize(30);

                break;

            case 1:
                //weekly_stats_card;
                holder.mTitleView.setText("Last seven days");
                ((WeeklyViewHolder) holder).stat_circle_1.setText(mStats.STAT_MAP.get(StatsContent.DAYS_PLANNED_WEEK).get()+"");
                ((WeeklyViewHolder) holder).stat_circle_2.setText(mStats.STAT_MAP.get(StatsContent.MISSED_GYMDAYS_WEEK).get()+"");
                ((WeeklyViewHolder) holder).stat_circle_3.setText(mStats.STAT_MAP.get(StatsContent.DAYS_HIT_WEEK).get()+"");

                ((WeeklyViewHolder) holder).stat_text_1.setText(mStats.STAT_MAP.get(StatsContent.DAYS_PLANNED_WEEK).details);
                ((WeeklyViewHolder) holder).stat_text_2.setText(mStats.STAT_MAP.get(StatsContent.MISSED_GYMDAYS_WEEK).details);
                ((WeeklyViewHolder) holder).stat_text_3.setText(mStats.STAT_MAP.get(StatsContent.DAYS_HIT_WEEK).details);

                ((WeeklyViewHolder) holder).calendar.setDayOfWeekEnd(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
                ((WeeklyViewHolder) holder).calendar.showLastDayMarker();
                List<String> txtlist = mStats.getGymVisitListForWeek(mFrag.getContext());
                ((WeeklyViewHolder) holder).calendar.setCalendarInfo(txtlist);
                //Set the color
                for (int i = 1; i < 8 ; i++){
                    View dayview = ((WeeklyViewHolder) holder).calendar.getDayViewFromPosition(i);
                    TextView circle = (TextView) dayview.findViewById(R.id.calendar_day_info);
                    String s = txtlist.get(i-1);
                    if (s.equals(mFrag.getResources().getString(R.string.reason_missed_gym_yesterday))){
                        circle.setBackgroundColor(mFrag.getResources().getColor(R.color.light_red, mFrag.getActivity().getTheme()));
                        circle.setTextColor(mFrag.getResources().getColor(R.color.light_red, mFrag.getActivity().getTheme()));
                    }
                    else if (s.equals(mFrag.getResources().getString(R.string.noinfo))){
                        dayview.setVisibility(View.GONE);
                    }
                }
                break;
            default:
                holder.mTitleView.setText("Default");
                break;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public class DayViewHolder extends StatisticsRecyclerViewAdapter.ViewHolder{
        public final TextView gymstate_circle;
        public final TextView gymstate_text;

        public DayViewHolder(View view){
            super(view);
            gymstate_circle = (TextView) view.findViewById(R.id.gymstate_circle);
            gymstate_text = (TextView) view.findViewById(R.id.gymstate_text);
        }
    }

    public class WeeklyViewHolder extends StatisticsRecyclerViewAdapter.ViewHolder{
        public final TextView stat_circle_1;
        public final TextView stat_text_1;
        public final TextView stat_circle_2;
        public final TextView stat_text_2;
        public final TextView stat_circle_3;
        public final TextView stat_text_3;

        public final WeekCalendarView calendar;

        public WeeklyViewHolder(View view){
            super(view);
            stat_circle_1 = (TextView) view.findViewById(R.id.stat_circle_1);
            stat_circle_2 = (TextView) view.findViewById(R.id.stat_circle_2);
            stat_circle_3 = (TextView) view.findViewById(R.id.stat_circle_3);

            stat_text_1 = (TextView) view.findViewById(R.id.stat_name_1);
            stat_text_2 = (TextView) view.findViewById(R.id.stat_name_2);
            stat_text_3 = (TextView) view.findViewById(R.id.stat_name_3);

            calendar = (WeekCalendarView) view.findViewById(R.id.calendar_component);
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

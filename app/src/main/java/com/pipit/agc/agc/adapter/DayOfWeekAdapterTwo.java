package com.pipit.agc.agc.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pipit.agc.agc.fragment.DayPickerFragmentTwo;
import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.R;
import com.pipit.agc.agc.util.SharedPrefUtil;
import com.pipit.agc.agc.util.Util;
import com.pipit.agc.agc.fragment.DayOfWeekPickerFragment;
import com.pipit.agc.agc.model.DayRecord;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Eric on 2/3/2016.
 */
public class DayOfWeekAdapterTwo extends RecyclerView.Adapter<DayOfWeekAdapterTwo.DOWViewHolder>{
    private final Context context;
    private final Fragment mFrag;
    public int count = 7;
    private int _screenheight;
    public static String TAG = "DayPickerAdapter";
    private HashSet<Integer> weeklySchedule; //Contains 0-7 days that are gym days

    public static class DOWViewHolder extends RecyclerView.ViewHolder {
        TextView comment;
        TextView statuscircle;
        LinearLayout cvlayout;

        DOWViewHolder(View itemView) {
            super(itemView);
            comment = (TextView) itemView.findViewById(R.id.comment);
            statuscircle = (TextView) itemView.findViewById(R.id.stat_circle);
            cvlayout = (LinearLayout) itemView.findViewById(R.id.cvlayout);
        }
    }

    public DayOfWeekAdapterTwo( HashSet<Integer> weeklySchedule, Fragment frag) {
        this.mFrag = frag;
        this.context = frag.getContext();
        this.weeklySchedule = weeklySchedule;
    }

    @Override
    public DOWViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dayofweek_layout_two, parent, false);
        DOWViewHolder vh = new DOWViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(final DOWViewHolder holder, final int position) {
        holder.comment.setText(getDayOfWeekText(position + 1));

        final String gymDay = context.getResources().getString(R.string.gym_day);
        final String restDay = context.getResources().getString(R.string.rest_day);
        if (weeklySchedule.contains(position)){
            holder.cvlayout.setBackgroundColor(ContextCompat.getColor(context, R.color.schemefour_lighterteal));
            holder.comment.setTextColor(ContextCompat.getColor(context, R.color.black));
            //holder.statuscircle.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.schemethree_teal), PorterDuff.Mode.SRC_ATOP);
            holder.statuscircle.setText(gymDay);
        } else{
            holder.cvlayout.setBackgroundColor(ContextCompat.getColor(context, R.color.basewhite));
            holder.comment.setTextColor(ContextCompat.getColor(context, R.color.black));
            //holder.statuscircle.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.schemethree_red), PorterDuff.Mode.SRC_ATOP);
            holder.statuscircle.setText(restDay);
        }

        holder.cvlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        /*Remove or Add the date to the list*/
                String datestr = (Integer.toString(position));

                SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
                List<String> dates = (SharedPrefUtil.getListFromSharedPref(prefs, Constants.SHAR_PREF_PLANNED_DAYS));
                if (dates.contains(datestr)) {
                    //The clicked date was previously a Gym Day, and we need to toggle it off
                    dates.remove(datestr);
                    Log.d(TAG, "Removed day " + datestr + " from weekly gym days");
                    holder.statuscircle.setText(restDay);
                    holder.cvlayout.setBackgroundColor(ContextCompat.getColor(context, R.color.basewhite));
                    holder.comment.setTextColor(ContextCompat.getColor(context, R.color.black));
                    //holder.statuscircle.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.schemethree_red), PorterDuff.Mode.SRC_ATOP);
                    if (position == Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1) {
                        if (mFrag instanceof DayOfWeekPickerFragment){
                            ((DayPickerFragmentTwo) mFrag).toggleCurrentGymDayData(false);
                        }
                    }
                } else {
                    dates.add(datestr);
                    Log.d(TAG, "Added day " + datestr + " to weekly gym days");
                    holder.statuscircle.setText(gymDay);
                    holder.cvlayout.setBackgroundColor(ContextCompat.getColor(context, R.color.schemefour_lighterteal));
                    holder.comment.setTextColor(ContextCompat.getColor(context, R.color.basewhite));
                    //holder.statuscircle.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.schemethree_teal), PorterDuff.Mode.SRC_ATOP);
                    if (position == Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1) {
                        if (mFrag instanceof DayOfWeekPickerFragment) {
                            ((DayPickerFragmentTwo) mFrag).toggleCurrentGymDayData(true);
                        }
                    }

                }
                SharedPrefUtil.putListToSharedPref(prefs.edit(), Constants.SHAR_PREF_PLANNED_DAYS, dates);
                updateData(null, null, new HashSet<Integer>(Util.listOfStringsToListOfInts(dates)));
            }
        });


    }

    @Override
    public int getItemCount() {
        return count;
    }


    private String getDayOfWeekText(int n){
        switch(n){
            case 1:
                return "Sunday";
            case 2:
                return "Monday";
            case 3:
                return "Tuesday";
            case 4:
                return "Wednesday";
            case 5:
                return "Thursday";
            case 6:
                return "Friday";
            case 7:
                return "Saturday";
        }
        return null;
    }

    /*
        Also styles the rowview
     */
    private void setDayStatus(int dayOfWeek, View rowview){


    }

    public void updateData(List<DayRecord> allPreviousDays, HashSet<String> exceptionDays,
                           HashSet<Integer> weeklySchedule){
        if (weeklySchedule!=null){
            this.weeklySchedule=weeklySchedule;
        }
        notifyDataSetChanged();
    }

}

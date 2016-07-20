package com.pipit.agc.agc.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.R;
import com.pipit.agc.agc.util.SharedPrefUtil;
import com.pipit.agc.agc.util.Util;
import com.pipit.agc.agc.fragment.DayOfWeekPickerFragment;
import com.pipit.agc.agc.model.DayRecord;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Eric on 2/3/2016.
 */
public class DayOfWeekAdapter extends RecyclerView.Adapter<DayOfWeekAdapter.CardViewHolder>{
    private final Context context;
    private final Fragment mFrag;
    public int count = 7;
    private int _screenheight;
    public static String TAG = "DayPickerAdapter";
    private HashSet<Integer> weeklySchedule; //Contains 0-7 days that are gym days

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView comment;
        TextView statuscircle;
        LinearLayout cvlayout;

        CardViewHolder(View itemView) {
            super(itemView);
            comment = (TextView) itemView.findViewById(R.id.comment);
            cv = (CardView)  itemView.findViewById(R.id.cv);
            statuscircle = (TextView) itemView.findViewById(R.id.stat_circle);
            cvlayout = (LinearLayout) itemView.findViewById(R.id.cvlayout);
        }
    }

    public DayOfWeekAdapter( HashSet<Integer> weeklySchedule, Fragment frag, int sizeToFill) {
        this.mFrag = frag;
        this.context = frag.getContext();
        this._screenheight=sizeToFill;
        this.weeklySchedule = weeklySchedule;
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dayofweek_layout, parent, false);
        CardViewHolder vh = new CardViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(final CardViewHolder holder, final int position) {
        holder.comment.setText(getDayOfWeekText(position + 1));

        /*Set listview height to show 7 days*/
        if (_screenheight<1){
            _screenheight = Math.round(Util.getScreenHeightMinusStatusBar(context));
        }
        int txtheight = (int) (_screenheight / 7);
        holder.cv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, txtheight));


        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_NOW);
        final String gymDay = context.getResources().getString(R.string.gym_day);
        final String restDay = context.getResources().getString(R.string.rest_day);
        if (weeklySchedule.contains(position)){
            holder.cvlayout.setBackgroundColor(ContextCompat.getColor(context, R.color.schemethree_darkerteal));
            holder.comment.setTextColor(ContextCompat.getColor(context, R.color.black));
            //holder.statuscircle.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.schemethree_teal), PorterDuff.Mode.SRC_ATOP);
            holder.statuscircle.setText(gymDay);
        } else{
            holder.cvlayout.setBackgroundColor(ContextCompat.getColor(context, R.color.basewhite));
            holder.comment.setTextColor(ContextCompat.getColor(context, R.color.black));
            //holder.statuscircle.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.schemethree_red), PorterDuff.Mode.SRC_ATOP);
            holder.statuscircle.setText(restDay);
        }

        holder.cv.setOnClickListener(new View.OnClickListener() {
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
                            ((DayOfWeekPickerFragment) mFrag).toggleCurrentGymDayData(false);
                        }
                    }
                } else {
                    dates.add(datestr);
                    Log.d(TAG, "Added day " + datestr + " to weekly gym days");
                    holder.statuscircle.setText(gymDay);
                    holder.cvlayout.setBackgroundColor(ContextCompat.getColor(context, R.color.schemethree_darkerteal));
                    holder.comment.setTextColor(ContextCompat.getColor(context, R.color.basewhite));
                    //holder.statuscircle.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.schemethree_teal), PorterDuff.Mode.SRC_ATOP);
                    if (position == Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1) {
                        if (mFrag instanceof DayOfWeekPickerFragment) {
                            ((DayOfWeekPickerFragment) mFrag).toggleCurrentGymDayData(true);
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

    /* Re size the font so the specified text fits in the text box
    * assuming the text box is the specified width.
    *
    *  Written by StackOverflow user speedplane
    *  MIT license
    */
    private void refitText(TextView txtv, int targetheight)
    {
        if (targetheight <= 0)
            return;
        Paint mTestPaint = new Paint();
        mTestPaint.set(txtv.getPaint());
        float hi = 100;
        float lo = 2;
        final float threshold = 0.5f; // How close we have to be

        while((hi - lo) > threshold) {
            float size = (hi+lo)/2;
            mTestPaint.setTextSize(size);
            if(mTestPaint.measureText((String) txtv.getText()) >= targetheight)
                hi = size; // too big
            else
                lo = size; // too small
        }
        // Use lo so that we undershoot rather than overshoot
        txtv.setTextSize(TypedValue.COMPLEX_UNIT_PX, lo);
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

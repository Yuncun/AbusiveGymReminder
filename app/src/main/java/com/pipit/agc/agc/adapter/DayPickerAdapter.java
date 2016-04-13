package com.pipit.agc.agc.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.R;
import com.pipit.agc.agc.util.Util;
import com.pipit.agc.agc.model.DayRecord;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Eric on 1/9/2016.
 */
public class DayPickerAdapter extends ArrayAdapter<String> {
    private final Context context;
    private List<DayRecord> allPreviousDays;
    public int count;
    private static int _screenheight;
    public static String TAG = "DayPickerAdapter";
    private HashSet<String> exceptionDays; //Days in this set are inverted on  top of the weekly schedule
    private HashSet<Integer> weeklySchedule; //Contains 0-7 days that are gym days

    public DayPickerAdapter(Context context, List<DayRecord> allPreviousDays, HashSet<String> exceptionDays,
                            HashSet<Integer> weeklySchedule) {
        super(context, R.layout.dayrowlayout);
        this.context = context;
        this.count = 14;
        this.count+=allPreviousDays.size();
        this.allPreviousDays = allPreviousDays;
        this._screenheight=-1;
        this.weeklySchedule = weeklySchedule;
        this.exceptionDays = exceptionDays;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.dayrowlayout, parent, false);
        TextView dayOfMonthTV = (TextView) rowView.findViewById(R.id.txt);
        TextView commentTV = (TextView) rowView.findViewById(R.id.comment);
        TextView dayOfWeekTV = (TextView) rowView.findViewById(R.id.mon_to_fri);

        if (position<allPreviousDays.size()){
            //The Past
            DayRecord d = allPreviousDays.get(position);
            String primaryText = "";
            if (d.beenToGym()){
                primaryText = "WENT TO GYM";
            }else{
                if (d.isGymDay()){
                    primaryText = "MISSED GYM";
                }else{
                    primaryText = "NO GYM - REST DAY";
                }
            }

            Date date = allPreviousDays.get(position).getDate();
            String dayOfWeekTxt;
            String dayOfMonthTxt;
            try{
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int dayOfWeek=cal.get(Calendar.DAY_OF_WEEK);
                dayOfWeekTxt=getDayOfWeekText(dayOfWeek);
                dayOfMonthTxt=Integer.toString(day);
            }catch(Exception e){
                dayOfWeekTxt="?";
                dayOfMonthTxt="?";
            }
            dayOfWeekTV.setText(dayOfWeekTxt);
            dayOfMonthTV.setText(dayOfMonthTxt);
            if (position==allPreviousDays.size()-1){
                primaryText+=" (Today)";
            }
            if (position==allPreviousDays.size()-2){
                primaryText+=" (Yesterday)";
            }
            commentTV.setText(primaryText);
        }
        else{
            int diff = position-(allPreviousDays.size()-1);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, diff);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int dayOfWeek=cal.get(Calendar.DAY_OF_WEEK);

            dayOfWeekTV.setText(getDayOfWeekText(dayOfWeek));
            dayOfMonthTV.setText(Integer.toString(day));

            String primaryText = getDayMessage(dayOfWeek, cal.getTime(), rowView);
            commentTV.setText(primaryText);

        }

        /*Set listview height to show 7 days*/
        if (_screenheight<1){
            _screenheight = Math.round(Util.getScreenHeightMinusStatusBar(getContext()));
        }

        //The weight of the day number is 3/4 of the total height
        int txtheight = (int) (.66 * (_screenheight / 7));
        dayOfMonthTV.setHeight(txtheight);
        refitText(dayOfMonthTV, txtheight);
        return rowView;
    }

    @Override
    public int getCount() {
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
        }
        return null;
    }

    /*
        Also styles the rowview
     */
    private String getDayMessage(int dayOfWeek, Date date, View rowview){
        String primaryText;
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_NOW);
        String datestr = sdf.format(date);
        String gymDay = getContext().getResources().getString(R.string.gym_day);
        String restDay = getContext().getResources().getString(R.string.rest_day);
        if (weeklySchedule.contains(dayOfWeek)){
            primaryText = gymDay;
        } else{
            primaryText = restDay;
        }

        if (exceptionDays.contains(datestr)){
            //Flip the day
            if (primaryText.equals(restDay)) primaryText = gymDay;
            else if (primaryText.equals(gymDay)) primaryText = restDay;
        }

        if (primaryText.equals(gymDay)){
            rowview.setBackgroundColor(ContextCompat.getColor(context, R.color.lightgreen));
        }else{
            rowview.setBackgroundColor(ContextCompat.getColor(context, R.color.basewhite));
        }
        return primaryText;
    }

    public void updateData(List<DayRecord> allPreviousDays, HashSet<String> exceptionDays,
                           HashSet<Integer> weeklySchedule){
        if (allPreviousDays!=null){
            this.allPreviousDays=allPreviousDays;
        }
        if (exceptionDays!=null){
            this.exceptionDays=exceptionDays;
        }
        if (weeklySchedule!=null){
            this.weeklySchedule=weeklySchedule;
        }
        this.notifyDataSetChanged();
    }
}
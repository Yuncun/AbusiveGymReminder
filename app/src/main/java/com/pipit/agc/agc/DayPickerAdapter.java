package com.pipit.agc.agc;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pipit.agc.agc.data.DayRecord;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Eric on 1/9/2016.
 */
public class DayPickerAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<DayRecord> allPreviousDays;
    public int count;
    private static int _screenheight;
    public static String TAG = "DayPickerAdapter";

    public DayPickerAdapter(Context context, List<DayRecord> allPreviousDays) {
        super(context, R.layout.dayrowlayout);
        this.context = context;
        this.count = 14;
        this.count+=allPreviousDays.size();
        this.allPreviousDays = allPreviousDays;
        this._screenheight=-1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.dayrowlayout, parent, false);
        TextView dayOfMonthTV = (TextView) rowView.findViewById(R.id.day_of_week);
        TextView commentTV = (TextView) rowView.findViewById(R.id.comment);
        TextView dayOfWeekTV = (TextView) rowView.findViewById(R.id.mon_to_fri);

        if (position<allPreviousDays.size()){
            //The Past
            String primaryText = allPreviousDays.get(position).getComment();
            Date date = allPreviousDays.get(position).getDate();
            String dayOfWeekTxt;
            String dayOfMonthTxt;
            try{
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
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

            String primaryText = "GYM DAY";
            commentTV.setText(primaryText);
        }

        /*Set listview height to show 7 days*/
        if (_screenheight<1){
            _screenheight = Math.round(Util.getScreenHeightMinusStatusBar(getContext()));
            Log.d(TAG, "Screen height minus action bar is " + _screenheight );
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

        //int targetWidth = targetWidth - this.getPaddingLeft() - this.getPaddingRight();
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
}